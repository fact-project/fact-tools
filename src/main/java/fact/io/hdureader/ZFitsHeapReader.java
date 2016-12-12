package fact.io.hdureader;

import fact.io.hdureader.zfits.BitQueue;
import fact.io.hdureader.zfits.ByteWiseHuffmanTree;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mackaiver on 14/11/16.
 */
public final class ZFitsHeapReader implements Iterator<OptionalTypesMap>, Iterable<OptionalTypesMap> {

    private final DataInputStream stream;
    private final List<BinTable.TableColumn> columns;
    private final Integer numberOfRowsInTable;
    private int numberOfRowsRead = 0;

    private static Logger log = LoggerFactory.getLogger(ZFitsHeapReader.class);



    private ZFitsHeapReader(BinTable binTable) {
        this.numberOfRowsInTable = binTable.numberOfRowsInTable;
        this.stream = binTable.heapDataStream;
        this.columns = binTable.columns;
    }

    public static ZFitsHeapReader forTable(BinTable binTable){
        return new ZFitsHeapReader(binTable);
    }


    @Override
    public boolean hasNext() {
        return numberOfRowsRead < numberOfRowsInTable;
    }

    @Override
    public OptionalTypesMap next() {
        try {
            return getNextRow();
        } catch (IOException e) {
            throw new RuntimeException("IO Error occured. " + e.getMessage());
        }
    }

    @Override
    public Iterator<OptionalTypesMap> iterator() {
        return this;
    }

    public OptionalTypesMap<String, Serializable> getNextRow() throws IOException {
        OptionalTypesMap<String, Serializable> map = new OptionalTypesMap<>();

        //Tile header in this row
        //see figure 6 of zfits paper
        TileHeader tile = TileHeader.fromStream(stream);

        //The tile header itself has 16 bytes.
        //Hence we subtract 16 bytes from the total number of bytes in this tile.
        byte[] tileData = new byte[Math.toIntExact(tile.size - 16)];
        stream.readFully(tileData);

        ByteBuffer tileBuffer = ByteBuffer.wrap(tileData).order(ByteOrder.LITTLE_ENDIAN);

        //iterate over columns in the row
        for (BinTable.TableColumn column : columns) {

            //sometimes there is no data. We do nothing then.
            if (column.repeatCount == 0){
                continue;
            }

            BlockHeader block = new BlockHeader(tileBuffer);


            if (block.compression == BlockHeader.Compression.HUFFMAN) {

                short[] shorts = huffmanDecompression(tileBuffer);
                map.put(column.name, shorts);

            } else if( block.compression == BlockHeader.Compression.HUFFMAN_AND_SMOOTHING){

                short[] shorts = huffmanDecompression(tileBuffer);
                unsmooth(shorts);
                map.put(column.name, shorts);

            }else {
                if(column.repeatCount == 1){
                    map.put(column.name, readSingleValueFromStream(column, tileBuffer));
                } else {

                    int offsetAfterReadingBytes = tileBuffer.position() + column.repeatCount * column.type.byteSize;
                    ByteBuffer bufferView = ByteBuffer.wrap(    tileBuffer.array(),
                                                                tileBuffer.position(),
                                                                offsetAfterReadingBytes)
                                                        .order(ByteOrder.LITTLE_ENDIAN);

                    tileBuffer.position(offsetAfterReadingBytes);
                    map.put(column.name, readArrayFromStream(column, bufferView));

                }
            }

//            log.info("Tilesize - 16 : {}  BlockSize {}, BytesRead {}", tile.size - 16, block.size, bytesRead);
        }

        numberOfRowsRead++;
        return map;
    }

    /**
     * Unsmooth the data according to "the zfits standard".
     * See the first equation on page three of https://arxiv.org/pdf/1506.06045v1.pdf
     *
     * @param data the data to unsmooth
     */
    private void unsmooth(short[] data){
        for (int i = 2; i < data.length; i++) {
            data[i] = (short) (data[i] + (data[i-1] +  data[i-2])/2 );
        }
    }

    /**
     * This methods starts by building a huffman tree from the symbol table in the data.
     * Once build it will be used to map code words to the symbols.
     * Here goes the code from huffman.h in FACT++ which describes how the symbol table is written to the data.
     * apparently this is the only documentation about that.
     *
     *     void WriteCodeTable(std::string &out) const {
     *           out.append((char*)&count, sizeof(size_t));
     *
     *           for (uint32_t i=0; i<MAX_SYMBOLS; i++)
     *           {
     *              const Code &n = lut[i];
     *              if (n.numbits==0)
     *                  continue;
     *
     *               // Write the 2 byte symbol.
     *               out.append((char*)&i, sizeof(uint16_t));
     *               if (count==1)
     *                  return;
     *
     *               // Write the 1 byte code bit length.
     *               out.append((char*)&n.numbits, sizeof(uint8_t));
     *
     *               // Write the code bytes.
     *               uint32_t numbytes = numbytes_from_numbits(n.numbits);
     *               out.append((char*)&n.bits, numbytes);
     *           }
     *       }
     *
     *
     * The raw data is stored as shorts. So one symbol is two bytes long.
     * @param buffer the coplet tile buffer (with blockheaders, but without the tile headers.)
     * @throws IOException in case the buffer ends before all bytes are read
     */
    private short[] huffmanDecompression(ByteBuffer buffer) throws IOException {
        //start reading the huffman tree definition
        long compressedBytes = buffer.getInt(); //size is stored in num of shorts

        //its given as number of int16. nobody knows why. it says nowhere.
        long numberOfShorts = buffer.getLong();
        short[] symbols = new short[Math.toIntExact(numberOfShorts)];

        long numberOfSymbols = buffer.getLong();

        ByteWiseHuffmanTree huffmanTree = constructHuffmanTreeFromBytes(buffer, numberOfSymbols);

//        int position = buffer.position();

        BitQueue q = new BitQueue();
        ByteWiseHuffmanTree currentNode = huffmanTree;

        int depth = 0;
        int index = 0;
        try {
            while (index < numberOfShorts) {

                if(q.queueLength < 16) {
                    if (buffer.remaining() > 2) {
                        q.addShort(buffer.getShort());
                    }
                    if (buffer.remaining() == 1) {
                        q.addByte(buffer.get());
                        q.addByte((byte) 0);
                    }
                    if (buffer.remaining() == 0) {
                        q.addShort((short) 0);
                    }
                }

                ByteWiseHuffmanTree node = currentNode.children[q.peekByte()];
                if (node.isLeaf) {
                    symbols[index] = node.payload.symbol;
                    index++;
//                    log.info("Numbits in node: {}  for symbol {}", node.payload.codeLengthInBits, node.payload.symbol);
                    q.remove(node.payload.codeLengthInBits - depth*8);

                    //reset traversal to root node.
                    depth = 0;
                    currentNode = huffmanTree;

                } else {
                    currentNode = node;
                    depth++;
                    q.remove(8);
                }
            }
        } catch (BufferUnderflowException e){
            log.error("compressed size was: {}, number of symbols was {}", compressedBytes, numberOfSymbols);
        }
        return symbols;
    }


    private ByteWiseHuffmanTree constructHuffmanTreeFromBytes(ByteBuffer buffer, long numberOfSymbols){
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

//            log.info("Sym: '{}', '{}', '{}', '{}', as int: {}", symbol, codeLengthInBits, codeLengthInBytes, String.format("%" + codeLengthInBits + "s", Integer.toBinaryString(bits)).replace(' ', '0'), bits);

            // add the symbol to the huffmanntree
            ByteWiseHuffmanTree.insert(huffmanTree, bits, codeLengthInBits, new ByteWiseHuffmanTree.Symbol(symbol, codeLengthInBits, codeLengthInBytes, bits) );
        }
        return huffmanTree;
    }




    private static class TileHeader{
        private final long size;
        private final int numberOfRows;
        private final String definitionString;

        private TileHeader(byte[] bytes) {
            definitionString = new String(bytes, 0, 4, StandardCharsets.US_ASCII);
            ByteBuffer buffer = ByteBuffer.wrap(bytes, 4, 16 - 4).order(ByteOrder.LITTLE_ENDIAN);
            numberOfRows = buffer.getInt();
            size = buffer.getLong();
        }

        static TileHeader fromStream(DataInputStream stream) throws IOException {
            byte[] tileHeaderBytes = new byte[16];
            stream.readFully(tileHeaderBytes);
            return new TileHeader(tileHeaderBytes);
        }
    }



    private static class BlockHeader{
        enum Compression{
            RAW,
            SMOOTHING,
            HUFFMAN,
            HUFFMAN_AND_SMOOTHING,
        }

        private final long size;
        private final String order;
        private final byte numberOfProcessings;
        private final Compression compression;

        BlockHeader(ByteBuffer buffer) {
            size = buffer.getLong();
            order = new String(new byte[]{buffer.get()}, StandardCharsets.US_ASCII);
            numberOfProcessings = buffer.get();

            byte processingType = 0;
            for (int n = 0; n < numberOfProcessings; n++) {
                processingType += buffer.get();
                //skip weird zero byte here:
                buffer.get();
            }

            switch (processingType){
                case 1:
                    this.compression = Compression.SMOOTHING;
                    break;
                case 2:
                    this.compression = Compression.HUFFMAN;
                    break;
                case 3:
                    this.compression = Compression.HUFFMAN_AND_SMOOTHING;
                    break;
                default:
                    this.compression = Compression.RAW;
                    break;
            }

        }

    }

    private Serializable readSingleValueFromStream(BinTable.TableColumn c, ByteBuffer buffer) throws IOException {

        Serializable b = null;
        switch (c.type){
            case BOOLEAN:
                b = buffer.get() > 0;
                break;
            case CHAR:
                b =  buffer.asCharBuffer().toString();
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

    private Serializable readArrayFromStream(BinTable.TableColumn c, ByteBuffer buffer) throws IOException {

        if(c.type == BinTable.ColumnType.BOOLEAN){
            boolean[] bools = new boolean[c.repeatCount];
            for (int i = 0; i < c.repeatCount; i++) {
                bools[i] = buffer.get() > 0;
            }
            return bools;
        }

        switch (c.type){
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
