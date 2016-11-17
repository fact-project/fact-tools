package fact.io.hdureader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A bitwise trie data structure. See https://en.wikipedia.org/wiki/Trie
 *
 * Created by mackaiver on 14/11/16.
 */
public class BitWiseTrie<T> {
    private static Logger log = LoggerFactory.getLogger(BitWiseTrie.class);

    private BitWiseTrie<T>[] children = new BitWiseTrie[2];

    //the value to store in the leaf. In a huffman tree this would be the decompressed symbol.
    public T payload = null;

//    public int depth = 0;

    public boolean isLeaf(){
        return (children[0] == null) && (children[1] == null);
    }


    public void insert(Integer codeWord, T payload){
        BitWiseTrie node = this;

//        int depth = 1;
        //iterate over bits in integer.
        int n = 1;
        while (n < codeWord) {

            int bit = (codeWord & n) > 0 ? 1: 0;

            if(node.children[bit] == null){
                break;
            } else {
                node = node.children[bit];
                n <<= 1;
//                depth ++;
            }
        }

        while (n < codeWord) {
            int bit = (codeWord & n) > 0 ? 1: 0;

            node.children[bit] = new BitWiseTrie<T>();
            node = node.children[bit];

//            depth++;
            n <<= 1;
//            node.depth = depth;

        }

        node.payload = payload;
    }

    public BitWiseTrie<T> find(Integer codeWord){
        BitWiseTrie<T> node = this;
//        log.info("codeword to find: {}", Integer.toBinaryString(codeWord));

        for (int n = 1; n < codeWord ; n <<= 1) {
            int bit = (codeWord & n) > 0 ? 1: 0;
//            log.info("traversing down bit: {}", Integer.toBinaryString(bit));
            if(node.children[bit] != null){
                node = node.children[bit];
            } else {
                return node;
            }
        }

        //can be null
        return node;
    }

    public static class Symbol {
        public final short symbol;
        //get the code length in bits.
        public final byte codeLengthInBits;
        public final int codeLengthInBytes;
        public final int bits;


        public Symbol(short symbol, byte codeLengthInBits, int codeLengthInBytes, int bits) {
            this.symbol = symbol;
            this.codeLengthInBits = codeLengthInBits;
            this.codeLengthInBytes = codeLengthInBytes;
            this.bits = bits;
        }
    }
}
