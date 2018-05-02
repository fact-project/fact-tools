package fact.io.hdureader;

import fact.io.hdureader.zfits.BitQueue;
import fact.io.hdureader.zfits.ByteWiseHuffmanTree;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This reader can interpret the bytes in the heap of a bintable that has been stored in the ZFits format.
 * Some specification of that format can be found on arxiv : https://arxiv.org/pdf/1506.06045v1.pdf
 * <p>
 * This class implements the iterable and iterator interfaces which allow for such nice things as
 * <p>
 * for(OptionalTypesMap p : heapReader){
 * assertTrue(p.containsKey("Data"));
 * assertTrue(p.size() == 9);
 * }
 * <p>
 * <p>
 * The next() and the getNextRow() methods deliver a map with the data from one row in the heap.
 * <p>
 * Reading the heap in a ZFits bintable works as follows:
 * <p>
 * The heap data starts at the offset stored in the 'ZHEAPTR' variable in the header.
 * This is wrong according to the FITS standard. It should be the 'THEAP' variable.
 * <p>
 * Each heap contains one TileHeader for each row with one BlockHeader for each column in the row.
 * Information about the data in the columns can be read from the 'ZFORMn' keywords in the header.
 * <p>
 * Data in the tiles is in Little Endian byte order.
 * <p>
 * The data from one tile is read completely into one bytebuffer.
 *
 * @see ZFITSHeapReader#getNextRow()
 * <p>
 * <p>
 * Then for each column the appropriate compression type is parsed from the block header.
 * @see BlockHeader#fromBuffer
 * <p>
 * When a column is huffman compressed, the definition of the huffmann tree will be stored in the first couple of bytes
 * in that row. This is documented exactly nowhere!
 * <p>
 * First we start with
 * <p>
 * [(int) numberOfCompressedBytes, (long) numberOfShortsAfterDecompression, (long) numberOfSymbolsUsedInCompression]
 * <p>
 * after that for each symbol (numberOfSymbolsUsedInCompression)
 * <p>
 * [(short) symbol, (byte) codeLengthInBits, the codeword stored in codeLengthInBits-rounded-up-to-bytes bytes (max 4 bytes)]
 * <p>
 * From these symbols and codewords the tree can be build see the ByteWiseHuffmanTree for details.
 * @see ByteWiseHuffmanTree
 * <p>
 * <p>
 * Created by mackaiver on 14/11/16.
 */
public final class ZFITSHeapReader implements Reader {

    private final DataInputStream stream;
    private final DataInputStream catalogStream;
    private int catalogPosition = 0;
    private final int zshrink;
    private final int zTileLen;
    private final List<BinTable.TableColumn> columns;
    private final Integer numberOfRowsInTable;
    private int numberOfRowsRead = 0;


    private Map<String, Buffer> tileCache = null;
    private Set<String> compressedBlocks = null;
    private int curRowInCache = 0;
    private int maxRowsInCache = 0;

    /**
     * Check whether there is another row to return from this heap
     *
     * @return true iff another row can be read.
     */
    @Override
    public boolean hasNext() {
        return numberOfRowsRead < numberOfRowsInTable;
    }


    private static Logger log = LoggerFactory.getLogger(ZFITSHeapReader.class);

    private ZFITSHeapReader(BinTable binTable) {
        this.numberOfRowsInTable = binTable.numberOfRowsInTable;
        this.stream = binTable.heapDataStream;
        this.columns = binTable.columns;
        this.catalogStream = binTable.tableDataStream;
        this.zshrink = binTable.header.getInt("ZSHRINK").orElse(1);
        this.zTileLen = binTable.header.getInt("ZTILELEN").orElse(1);
    }


    /**
     * Creates a ZFITSHeapReader for a given binary table. This reader is iterable and provides a @see next() method which
     * will yield the column data for each row.
     *
     * @param binTable the binary table which contains a zfits heap.
     * @return the ZFITSHeapReader for the given table.
     */
    public static ZFITSHeapReader forTable(BinTable binTable) {
        return new ZFITSHeapReader(binTable);
    }

    /**
     * Skips the given number of rows.
     *
     * @param amount The amount of rows to skip.
     * @throws IOException
     */
    @Override
    public void skipRows(int amount) throws IOException {
        int resultingRow = amount+numberOfRowsRead;
        if (resultingRow >= this.numberOfRowsInTable) {
            throw new IOException("Not enough rows in table, need "+(amount+numberOfRowsRead)+" have "+numberOfRowsInTable);
        }
        
        // check if resulting row is in the current catalog remainer
        int remainer = zTileLen - numberOfRowsRead%zTileLen;
        if (amount<remainer) { // no need to deal with the catalog as we just work within one
            for (int i = 0; i < amount; i++) {
                getNextRow();
            }
            return;
        }
        if (remainer!=zTileLen) {
            // go the the start of the next catalog and adjust the amount to skip left
            for (int i = 0; i < remainer; i++) {
                getNextRow();
            }
            amount -= remainer;
        }


        int rowCatalogPosition = numberOfRowsRead / zTileLen;
        int rowCatalogPositionFinished = resultingRow / zTileLen;
        // check if we can't skip using the catalog due to being in the same catalog position
        if (rowCatalogPosition == rowCatalogPositionFinished) {
            for (int i = 0; i < amount; i++) {
                getNextRow();
            }
            return;
        }

        // check if the current catalog position is further then the rowCatalogPosition
        if (catalogPosition > rowCatalogPosition) {
            // align them the difference if always maximal 1
            for (int i = 0; i < zTileLen; i++) {
                getNextRow();
            }
            rowCatalogPosition += 1;
        }
        // check if we can't skip using the catalog due to being in the same catalog position
        if (rowCatalogPosition == rowCatalogPositionFinished) {
            for (int i = 0; i < amount; i++) {
                getNextRow();
            }
            return;
        }

        // move the current catalog position to the rowCatalogPosition
        int skipBytes = (rowCatalogPosition - catalogPosition) * columns.size() * (16);
        this.catalogStream.skipBytes(skipBytes);
        this.catalogPosition = rowCatalogPosition;

        // get current row position
        this.catalogStream.skipBytes(8); // go directly to the offset
        long rowOffset = this.catalogStream.readLong() - 16; // read the offset - 16 for the header
        this.catalogStream.skipBytes(columns.size() * (16) - 8 - 8); // go to the next catalog start
        this.catalogPosition += 1;

        // go to the finishing position catalog
        int diffCatalogs = rowCatalogPositionFinished-catalogPosition;
        skipBytes = diffCatalogs * columns.size() * (16) + 8; // go directly to the offset
        this.catalogStream.skipBytes(skipBytes);
        long finalRowOffset = this.catalogStream.readLong() - 16; // read the offset - 16 for the header

        // readjust the catalogStream
        this.catalogStream.skipBytes(columns.size() * (16) - 8 - 8); // go to the next catalog start
        this.catalogPosition += diffCatalogs+1;

        // skip the bytes in the data stream
        long skipManyBytes = finalRowOffset - rowOffset;
        while(skipManyBytes!=0) {
            long skipped = this.stream.skip(skipManyBytes);
            skipManyBytes -= skipped;
        }
        this.numberOfRowsRead += diffCatalogs * this.zTileLen;

        int remainingRows = resultingRow % zTileLen;
        for (int i=0; i<remainingRows; i++) {
            getNextRow();
        }
    }

    /**
     * Get the data from the next row. The columns in the row can be accessed by their name in the resulting
     * map that is returned by this method. The resulting map comes with convenience methods for accessing data of
     * predefined types.
     *
     * @return a map containing the the data from the rows columns.
     * @throws NoSuchElementException iff hasNext() is false
     */
    public OptionalTypesMap<String, Serializable> getNextRow() throws IOException {
        return getNextRow(false);
    }


    /**
     * Get the data from the next row. The columns in the row can be accessed by their name in the resulting
     * map that is returned by this method. The resulting map comes with convenience methods for accessing data of
     * predefined types.
     * <p>
     * Some zfits file apparently contain a wrong tile header in the heap storing the ZDrsCellOffsets.
     * I haven't figures out to which files this applies. Files from 2013 seem to have that problem.
     * But also newer ones. Some files from 2016 are fine however.
     * <p>
     * The old ZFits implementation by M.Bulinski ignored the problem by default.
     *
     * @param ignoreWrongTileHeader if true, ignores wrong tileheader information.
     * @return a map containing the the data from the rows columns.
     * @throws NoSuchElementException iff hasNext() is false
     */
    public OptionalTypesMap<String, Serializable> getNextRow(boolean ignoreWrongTileHeader) throws IOException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        // check if we currently still have rows in the cache if not get the next tile
        if (maxRowsInCache == curRowInCache) {
            this.getNextTileIntoCache(ignoreWrongTileHeader);
        }
        OptionalTypesMap<String, Serializable> map = new OptionalTypesMap<>();

        //iterate over columns in the row
        for (BinTable.TableColumn column : columns) {

            //sometimes there is no data. We do nothing then.
            if (column.repeatCount == 0) {
                continue;
            }
            Buffer curBuffer = tileCache.get(column.name);

            if (compressedBlocks.contains(column.name)) {
                ShortBuffer shortBuffer = (ShortBuffer) curBuffer;
                short[] data = new short[column.repeatCount];
                shortBuffer.get(data);
                map.put(column.name, data);
            } else {
                ByteBuffer byteBuffer = (ByteBuffer) curBuffer;
                if (column.repeatCount == 1) {
                    map.put(column.name, readSingleValueFromBuffer(column, byteBuffer));
                } else {
                    map.put(column.name, readArrayFromBuffer(column, byteBuffer));
                }
            }
        }
        curRowInCache++;
        numberOfRowsRead++;
        return map;
    }

    /**
     * Reads the next tile into the cache, to later read the data from it.
     *
     * @param ignoreWrongTileHeader
     * @throws IOException
     */
    private void getNextTileIntoCache(boolean ignoreWrongTileHeader) throws IOException {
        //this TileHeader
        //see figure 6 of zfits paper
        TileHeader tile = TileHeader.fromStream(stream, ignoreWrongTileHeader);

        //The tile header itself has 16 bytes.
        //Hence we subtract 16 bytes from the total number of bytes in this tile.
        byte[] tileData = new byte[Math.toIntExact(tile.size - 16)];
        stream.readFully(tileData);

        ByteBuffer tileBuffer = ByteBuffer.wrap(tileData).order(ByteOrder.LITTLE_ENDIAN);

        tileCache = new HashMap<>();
        compressedBlocks = new HashSet<>();

        //iterate over columns in the tile
        for (BinTable.TableColumn column : columns) {

            // sometimes there is no data. We do nothing then.
            if (column.repeatCount == 0) {
                continue;
            }

            // read the current block information
            BlockHeader block = BlockHeader.fromBuffer(tileBuffer);

            if (block.order.equals("C") && tile.numberOfRows != 1) {
                throw new NotImplementedException("Column ordering with more than 1 row not supported for now.");
            }
            int elementByteSize = column.type.byteSize * column.repeatCount;
            if (block.compression != BlockHeader.Compression.RAW && column.type != BinTable.ColumnType.SHORT) {
                throw new NotImplementedException("Current Reader doesn't support compression other types than short");
            }
            if (block.compression == BlockHeader.Compression.HUFFMAN) {
                short[] shorts = blockHuffmanDecompression(tileBuffer, tile.numberOfRows, elementByteSize);
                compressedBlocks.add(column.name);
                tileCache.put(column.name, ShortBuffer.wrap(shorts));

            } else if (block.compression == BlockHeader.Compression.HUFFMAN_AND_SMOOTHING) {
                short[] shorts = blockHuffmanDecompression(tileBuffer, tile.numberOfRows, elementByteSize);
                unsmooth(shorts);
                compressedBlocks.add(column.name);
                tileCache.put(column.name, ShortBuffer.wrap(shorts));

            } else if (block.compression == BlockHeader.Compression.RAW) {
                // convert to int, no block is bigger than 4GB
                int numberBytes = Math.toIntExact(block.getDataSize());
                byte[] cache = new byte[numberBytes];
                for (int i = 0; i < numberBytes; i++) {
                    cache[i] = tileBuffer.get();
                }
                tileCache.put(column.name, ByteBuffer.wrap(cache).order(ByteOrder.LITTLE_ENDIAN));
            } else {
                throw new NotImplementedException("Compression: '" + block.compression.name() + "' is not implemented");
            }
        }
        curRowInCache = 0;
        maxRowsInCache = tile.numberOfRows;
    }

    /**
     * Unsmooth the data according to "the zfits standard".
     * See the first equation on page three of https://arxiv.org/pdf/1506.06045v1.pdf
     *
     * @param data the data to unsmooth
     */
    private void unsmooth(short[] data) {
        for (int i = 2; i < data.length; i++) {
            data[i] = (short) (data[i] + (data[i - 1] + data[i - 2]) / 2);
        }
    }

    /**
     * Decompress a whole block of compressed huffman data
     *
     * @param buffer        The buffer containing the block data
     * @param numRows       The amount of rows inside the block
     * @param sizeSingleRow The decompressed size of a single Row
     * @return The decompressed block data
     */
    private short[] blockHuffmanDecompression(ByteBuffer buffer, int numRows, int sizeSingleRow) throws IOException {
        //read the sizes of the compressed rows
        int[] sizeCompressedRows = new int[numRows];
        for (int i = 0; i < numRows; i++) {
            sizeCompressedRows[i] = buffer.getInt();
        }

        //the finished product
        short[] result = new short[sizeSingleRow * numRows];

        for (int i = 0; i < numRows; i++) {
            // grab the row
            byte[] row = new byte[sizeCompressedRows[i]];
            buffer.get(row);
            ByteBuffer rowBuffer = ByteBuffer.wrap(row).order(ByteOrder.LITTLE_ENDIAN);

            // decompress it
            short[] decompressedRow = huffmanDecompression(rowBuffer);
            if (decompressedRow.length * 2 != sizeSingleRow) {
                throw new IOException("The decompressed row has the wrong size: " + decompressedRow.length * 2 + ", expected: " + sizeSingleRow);
            }
            System.arraycopy(decompressedRow, 0, result, i * (sizeSingleRow / 2), (sizeSingleRow / 2));
        }
        return result;
    }

    /**
     * This methods starts by building a huffman tree from the symbol table in the data.
     * Once build it will be used to map code words to the symbols.
     * Here goes the code from huffman.h in FACT++ which describes how the symbol table is written to the data.
     * apparently this is the only 'documentation' about that.
     * <p>
     * void WriteCodeTable(std::string &out) const {
     * out.append((char*)&count, sizeof(size_t));
     * <p>
     * for (uint32_t i=0; i<MAX_SYMBOLS; i++)
     * {
     * const Code &n = lut[i];
     * if (n.numbits==0)
     * continue;
     * <p>
     * // Write the 2 byte symbol.
     * out.append((char*)&i, sizeof(uint16_t));
     * if (count==1)
     * return;
     * <p>
     * // Write the 1 byte code bit length.
     * out.append((char*)&n.numbits, sizeof(uint8_t));
     * <p>
     * // Write the code bytes.
     * uint32_t numbytes = numbytes_from_numbits(n.numbits);
     * out.append((char*)&n.bits, numbytes);
     * }
     * }
     * <p>
     * <p>
     * The raw data is stored as shorts. So one symbol is two bytes long.
     *
     * @param rowBuffer the buffer only containing the huffman compressed row
     * @throws IOException in case the buffer ends before all bytes are read
     */
    private short[] huffmanDecompression(ByteBuffer rowBuffer) throws IOException {
        //start reading the huffman tree definition

        //its given as number of int16. nobody knows why. it says nowhere.
        long numberOfShorts = rowBuffer.getLong();
        short[] symbols = new short[Math.toIntExact(numberOfShorts)];

        long numberOfSymbols = rowBuffer.getLong();

        ByteWiseHuffmanTree huffmanTree = constructHuffmanTreeFromBytes(rowBuffer, numberOfSymbols);

        BitQueue q = new BitQueue();
        ByteWiseHuffmanTree currentNode = huffmanTree;


        int depth = 0;
        int index = 0;
        try {
            while (index < numberOfShorts) {

                if (q.queueLength < 16) {
                    if (rowBuffer.remaining() >= 2) {
                        q.addShort(rowBuffer.getShort());
                    } else if (rowBuffer.remaining() == 1) {
                        q.addByte(rowBuffer.get());
                        q.addByte((byte) 0);
                    } else if (rowBuffer.remaining() == 0) {
                        q.addShort((short) 0);
                    }
                }

                ByteWiseHuffmanTree node = currentNode.children[q.peekByte()];
                if (node.isLeaf) {
                    symbols[index] = node.payload.symbol;
                    index++;
                    q.remove(node.payload.codeLengthInBits - depth * 8);

                    //reset traversal to root node.
                    depth = 0;
                    currentNode = huffmanTree;

                } else {
                    currentNode = node;
                    depth++;
                    q.remove(8);
                }
            }
        } catch (BufferUnderflowException e) {
            log.error("compressed size was: {}, number of symbols was {}", rowBuffer.array().length, numberOfSymbols);
        }
        return symbols;
    }

    /**
     * Construct a huffmann tree from the symbol table stored for each huffmann compressed column
     *
     * @param buffer          the buffer containing the bytes in the column
     * @param numberOfSymbols the number of symbols this table contains
     * @return a ByteWiseHuffmanTree from the symbol table in the buffer
     */
    private ByteWiseHuffmanTree constructHuffmanTreeFromBytes(ByteBuffer buffer, long numberOfSymbols) {
        ByteWiseHuffmanTree huffmanTree = new ByteWiseHuffmanTree();
        //read all entries and create the decoding tree
        for (long i = 0; i < numberOfSymbols; i++) {
            //the first two bytes encode the actual symbol
            short symbol = buffer.getShort();

            //apparently this can happen. See the return statement in the code above.
            if (numberOfSymbols == 1) { // only one entry
                log.debug("Found just one symbol in table: {}", symbol);
                break;
            }

            //get the code length in bits.
            byte codeLengthInBits = buffer.get();

            //the code words are byte-aligned. So we can round up to the nearest byte
            int codeLengthInBytes = (int) Math.ceil(codeLengthInBits / 8.0);

            if (codeLengthInBytes > 4) {
                throw new NotImplementedException("Nope. Codelength > 4 bytes are not supported");
            }

            // read bits of the codeword representing the symbol
            byte[] huffmanCode = new byte[4];
            buffer.get(huffmanCode, 0, codeLengthInBytes);

            // convert byte array to an int
            int bits = ByteBuffer.wrap(huffmanCode).order(ByteOrder.LITTLE_ENDIAN).getInt();


            // add the symbol to the huffmanntree
            ByteWiseHuffmanTree.insert(huffmanTree, bits, codeLengthInBits, new ByteWiseHuffmanTree.Symbol(symbol, codeLengthInBits, codeLengthInBytes, bits));
        }
        return huffmanTree;
    }


    /**
     * According to https://arxiv.org/pdf/1506.06045v1.pdf the heap contains Tiles which in turn contain the column data.
     * One tile can contain many rows according to 'the spec' however we don't support this and
     * I haven't encountered this case yet
     */
    private static class TileHeader {
        private final long size;
        private final int numberOfRows;
        private final String definitionString;

        private TileHeader(byte[] bytes) {
            definitionString = new String(bytes, 0, 4, StandardCharsets.US_ASCII);
            ByteBuffer buffer = ByteBuffer.wrap(bytes, 4, 16 - 4).order(ByteOrder.LITTLE_ENDIAN);
            numberOfRows = buffer.getInt();
            size = buffer.getLong();
        }

        /**
         * Creates a TileHeader object. This method reads 16 bytes from the stream as described in
         * figure 6 in 'the spec'.
         * A TileHeader always should start with the letters TILE. If that is not the case the
         * this method will throw an IOException except when ignoreWrongHeader is set to true.
         * The latter is necessary for some broken ZFits files.
         *
         * @param stream            the DataInputStream that is reading the  bytes from the heap.
         * @param ignoreWrongHeader ignore when Tile does not start with 'TILE'.
         * @return a TileHeader object created from bytes in the stream.
         * @throws IOException when the tile header cannot be created
         */
        static TileHeader fromStream(DataInputStream stream, boolean ignoreWrongHeader) throws IOException {
            byte[] tileHeaderBytes = new byte[16];
            stream.readFully(tileHeaderBytes);

            TileHeader tileHeader = new TileHeader(tileHeaderBytes);
            if (!tileHeader.definitionString.equals("TILE")) {
                if (!ignoreWrongHeader)
                    throw new IOException("Tile header did not begin with word 'TILE'");
                log.warn("Tile header did not begin with word 'TILE'. Ignoring error.");
            }

            return tileHeader;
        }
    }


    /**
     * Each tile contains a number of blockheaders.
     * The blockheader describes how the data was compressed and how many bytes it contains.
     */
    private static class BlockHeader {
        enum Compression {
            RAW,
            SMOOTHING,
            HUFFMAN,
            HUFFMAN_AND_SMOOTHING,
        }

        private final long size;
        private final String order;
        private final Compression compression;
        private final long headerSize;

        private BlockHeader(long size, String order, Compression compression, long sizeHeader) {
            this.size = size;
            this.order = order;
            this.compression = compression;
            this.headerSize = sizeHeader;
        }

        /**
         * Returns the size of the data that are contained in this block
         *
         * @return the data size of this block
         */
        public long getDataSize() {
            return this.size - this.headerSize;
        }

        /**
         * Create a blockheader from the bytebuffer passed.
         *
         * @param buffer the buffer to read.
         * @return the blockheader form the bytes in the buffer
         * @throws IOException in case the ordering is wrong
         */
        static BlockHeader fromBuffer(ByteBuffer buffer) throws IOException {

            long size = buffer.getLong();
            String order = new String(new byte[]{buffer.get()}, StandardCharsets.US_ASCII);

            if (!(order.equals("R") || order.equals("C"))) {
                throw new IOException("Block header was not ordered by 'R' or 'C' but '" + order + "'");
            }

            byte numberOfProcessings = buffer.get();

            byte processingType = 0;
            for (int n = 0; n < numberOfProcessings; n++) {
                processingType += buffer.get();
                // skip weird zero byte here. This is not documented correctly in 'the spec'
                // it should be a short but they worte it wrongly in the spec (ignoring here)
                buffer.get();
            }

            Compression compression;
            switch (processingType) {
                case 1:
                    compression = Compression.SMOOTHING;
                    break;
                case 2:
                    compression = Compression.HUFFMAN;
                    break;
                case 3:
                    compression = Compression.HUFFMAN_AND_SMOOTHING;
                    break;
                default:
                    compression = Compression.RAW;
                    break;
            }
            // size(8 Bytes)+order(1 Bytes)+numberOfProcessings(1 Bytes)+Processings(2 Bytes*numberOfProcessings)
            long sizeHeader = 8 + 1 + 1 + (numberOfProcessings * 2);
            return new BlockHeader(size, order, compression, sizeHeader);
        }

    }

    private Serializable readSingleValueFromBuffer(BinTable.TableColumn c, ByteBuffer buffer) throws IOException {

        Serializable b = null;
        switch (c.type) {
            case BOOLEAN:
                b = buffer.get() > 0;
                break;
            case CHAR:
                b = buffer.asCharBuffer().toString();
                break;
            case BYTE:
                b = buffer.get();
                break;
            case SHORT:
                b = buffer.getShort();
                break;
            case INT:
                b = buffer.getInt();
                break;
            case LONG:
                b = buffer.getLong();
                break;
            case FLOAT:
                b = buffer.getFloat();
                break;
            case DOUBLE:
                b = buffer.getDouble();
                break;
        }
        return b;
    }

    private Serializable readArrayFromBuffer(BinTable.TableColumn c, ByteBuffer buffer) throws IOException {

        if (c.type == BinTable.ColumnType.BOOLEAN) {
            boolean[] bools = new boolean[c.repeatCount];
            for (int i = 0; i < c.repeatCount; i++) {
                bools[i] = buffer.get() > 0;
            }
            return bools;
        }

        switch (c.type) {
            case CHAR:
                char[] chars = new char[c.repeatCount];
                buffer.asCharBuffer().get(chars);
                return chars;
            case BYTE:
                byte[] b = new byte[c.repeatCount];
                buffer.get(b);
                return b;
            case SHORT:
                short[] shorts = new short[c.repeatCount];
                buffer.asShortBuffer().get(shorts);
                return shorts;
            case INT:
                int[] ints = new int[c.repeatCount];
                buffer.asIntBuffer().get(ints);
                return ints;
            case LONG:
                long[] longs = new long[c.repeatCount];
                buffer.asLongBuffer().get(longs);
                return longs;
            case FLOAT:
                float[] floats = new float[c.repeatCount];
                buffer.asFloatBuffer().get(floats);
                return floats;
            case DOUBLE:
                double[] doubles = new double[c.repeatCount];
                buffer.asDoubleBuffer().get(doubles);
                return doubles;
        }
        return null;
    }

}
