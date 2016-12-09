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
import java.util.List;

/**
 * Created by mackaiver on 14/11/16.
 */
public final class ZFitsHeapReader {

    private final DataInputStream stream;
    private final List<BinTable.TableColumn> columns;

    private static Logger log = LoggerFactory.getLogger(ZFitsHeapReader.class);



    private ZFitsHeapReader(BinTable binTable) {
        this.stream = binTable.heapDataStream;
        this.columns = binTable.columns;
    }

    public static ZFitsHeapReader forTable(BinTable binTable){
        return new ZFitsHeapReader(binTable);
    }

    public OptionalTypesMap<String, Serializable> getNextRow() throws IOException {
        OptionalTypesMap<String, Serializable> map = new OptionalTypesMap<>();

        //read first tile header in this row
        //see figure 6 of zfits paper
        TileHeader tile = TileHeader.fromStream(stream);

        //read bytes until all bytes in this tile have been read.
        //The tileheader itself has 16 bytes.
        //Hence we subtract 16 bytes from the total number of bytes in this tile.
        long bytesRead = 0;
        int columnIndex = 0;
        while (bytesRead < tile.size - 16) {

            BlockHeader block = BlockHeader.fromStream(stream);
            if(block.numberOfProcessings > 1 || !block.order.equals("R") ){
                throw new NotImplementedException("Ich glaub es hackt!");
            }

            //read one row into memory (because now comes the little endian?)
            byte[] row = new byte[Math.toIntExact(block.size)];
            stream.readFully(row);

            short[] shorts = decompressRow(row);
            map.put(columns.get(columnIndex).name, shorts);

            bytesRead += block.size;
//            log.info("Tilesize - 16 : {}  BlockSize {}, BytesRead {}", tile.size - 16, block.size, bytesRead);
        }


        return map;
    }

    /**
     * This mehtods starts by building a huffman tree from the symbol table in the data.
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
     * @param row byte of a full row (without blockheaders or tile headers.)
     * @throws IOException in case the buffer ends before all bytes are read
     */
    private short[] decompressRow(byte[] row) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(row).order(ByteOrder.LITTLE_ENDIAN);

        byte processingType = buffer.get();
        if (processingType != 2) {
            throw new NotImplementedException("Nein!");
        }

        //there is one zero byte here. I dont know why
        byte paddingByteMaybe = buffer.get();

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
                    short data  = buffer.getShort();
                    q.addShort(data);
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
        private final long size;
        private final String order;
        private final byte numberOfProcessings;

        private BlockHeader(byte[] bytes) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            size = buffer.getLong();
            order = new String(new byte[]{buffer.get()}, StandardCharsets.US_ASCII);
            numberOfProcessings = buffer.get();
        }

        static BlockHeader fromStream(DataInputStream stream) throws IOException {
            byte[] headerBytes = new byte[10];
            stream.readFully(headerBytes);
            return  new BlockHeader(headerBytes);
        }
    }

}
