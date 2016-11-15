package fact.io.hdureader;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import fact.io.zfits.ZFitsUtil;
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
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by mackaiver on 14/11/16.
 */
public final class ZFitsHeapReader {
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


    final DataInputStream stream;

    private static Logger log = LoggerFactory.getLogger(ZFitsHeapReader.class);

    ZFitsHeapReader(DataInputStream stream) {
        this.stream = stream;
    }


    public MapMapper<String, Serializable> getNextRow() throws IOException {
        MapMapper<String, Serializable> map = new MapMapper<>();

        //read first tile header in this row
        //see figure 6 of zfits paper
        TileHeader tile = TileHeader.fromStream(stream);
        //read bytes until all bytes in this tile have been read.
        //The tileheader itself has 16 bytes.
        //Hence we subtract 16 bytes from the total number of bytes in this tile.
        long bytesRead = 0;
        while (bytesRead < tile.size - 16) {

            BlockHeader block = BlockHeader.fromStream(stream);
            if(block.numberOfProcessings > 1 || !block.order.equals("R") ){
                throw new NotImplementedException("Ich glaub es hackt!");
            }

            //read one row into memory (because now comes the little endian?)
            byte[] row = new byte[Math.toIntExact(block.size)];
            stream.readFully(row);

            decompressRow(row);

            bytesRead += block.size;

            log.info("Tilesize - 16 : {}  BlockSize {}, BytesRead {}", tile.size - 16, block.size, bytesRead);
        }


        return map;
    }

    /**
     * Here goes the code from huffman.h in FACT++
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
     * @param row
     * @throws IOException
     */
    private void decompressRow(byte[] row) throws IOException {
        //first 8 bytes is the size of the decompressed data
        ByteBuffer buffer = ByteBuffer.wrap(row).order(ByteOrder.LITTLE_ENDIAN);
        BitWiseTrie huffmanTrie = new BitWiseTrie();

        byte processingType = buffer.get();
        if (processingType != 2) {
            throw new NotImplementedException("Nein!");
        }

        //there is one zero byte here. I dont know why
        byte paddingByteMaybe = buffer.get();
        long compressedSize = buffer.getInt(); //size is stored in num of shorts
        //start reading the huffman tree definition
        //its given as number of int16. nobody knows why. it says nowhere.
        long unCompressedSize = buffer.getLong()*2;

        long numberOfSymbols = buffer.getLong();

        //read all entries and create the decoding tree
        for (long i = 0; i < numberOfSymbols; i++) {
            // first two bytes are the symbol
            short symbol = buffer.getShort();

            //for some uterly stupid reason we have to break here. See the return statement in the code above.
            if (numberOfSymbols == 1) { // only one entry
//                tree.insertSymbol(symbol, (byte) 0, 0);
                log.debug("Found just one symbol in table: {}", symbol);
                break;
            }

            //get the code length in bits.
            byte codeLengthInBits = buffer.get();//&0xFF;
            int codeLengthInBytes = (int) Math.ceil(codeLengthInBits / 8.0);

            if (codeLengthInBytes > 4) {
                throw new NotImplementedException("Nope.");
            }

            // read the lookout of the bits of the Huffman representation of the symbol
            byte[] huffmanCode = new byte[4];
            buffer.get(huffmanCode, 0, codeLengthInBytes);

            int bits = ByteBuffer.wrap(huffmanCode).order(ByteOrder.LITTLE_ENDIAN).getInt();
            // convert byte array to an int
//            int bits = ZFitsUtil.wrap(huffmanCode).getInt();
            log.info("Sym: '{}', '{}', '{}', '{}', as int: {}", symbol, codeLengthInBits, codeLengthInBytes, String.format("%" + codeLengthInBits + "s", Integer.toBinaryString(bits)).replace(' ', '0'), bits);
            BitWiseTrie.insert(huffmanTrie, bits, symbol);
//            tree.insertSymbol(symbol, codeLengthInBits, bits);
        }


//        System.out.println(huffmanTrie);

        int bitQueue = 0;
        int queueLength = 0;
        ArrayList<Integer> symbols = Lists.newArrayList();

        BitWiseTrie currentNode = huffmanTrie;
        int position = buffer.position();

        long bytesRead = 0;
        try {
            while (bytesRead < compressedSize - position) {
                if(queueLength < 8) {
                    bitQueue |= (buffer.get() & 0xFF) << queueLength;
                    bytesRead++;
                    queueLength += 8;
                }



                BitWiseTrie node = BitWiseTrie.find(currentNode, bitQueue);
                if (node.isLeaf()) {
                    symbols.add(node.value);

                    bitQueue >>= node.depth;
                    queueLength -= node.depth;
                    //reset traversal to root node.
                    currentNode = huffmanTrie;

                } else {
                    currentNode = node;
                    bitQueue >>= 8; //gow much
                    queueLength -= 8;
                }
            }
        } catch (BufferUnderflowException e){
            log.error("Bytes read: {}, compressed size was: {}, number of symbols was {}", bytesRead, compressedSize, numberOfSymbols);
        }

//        System.out.println(symbols);
    }

//
//    class BitQueue{
//
//        int queue = 0;
//        int queueLength = 0;
//
//        public void addByte(byte b){
//            queue |= b << queueLength;
//            queueLength += 8;
//        }
//
//        public void remove(int n){
//            queue   >>= n;
//            queueLength -= n;
//        }
//    }
//            if (bufferPosition+1==bufferSize) {
//                data = (short)(buffer.get(bufferPosition)&0x00FF);
//            } else {
//                // get 2 bytes to extract one byte from the curBit position
//                data = buffer.getShort(bufferPosition);
//            }
//            byte curByte = (byte) (data >> curBit);
//            int index = curByte&0xFF;
//
//            curTree = curTree.get(index);
//            if (!curTree.isLeaf()) {
//                bufferPosition++;
//                continue;
//            }
//
//            // write down symbol
//            outputBuffer.putShort(curTree.getSymbole());
//            if (outputBuffer.remaining()==0) {
//                break;
//            }
//            curBit += curTree.getNumBits();
//
//            curTree = this.tree;
//            if (curBit >= 8) {
//                curBit %= 8;
//                bufferPosition++;
//            }


}
