package fact.io.hdureader;

import org.dmg.pmml.adapters.IntegerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * A bitwise trie data structure. See https://en.wikipedia.org/wiki/Trie
 *
 * Created by mackaiver on 14/11/16.
 */
public class BitWiseTrie {
    private static Logger log = LoggerFactory.getLogger(BitWiseTrie.class);

    private BitWiseTrie[] children = new BitWiseTrie[2];

    //the value to store in the leaf. In a huffman tree this would be the decompressed symbol.
    public Integer value = null;

    public int depth = 0;

    public boolean isLeaf(){
        return (children[0] == null) && (children[1] == null);
    }

    public static void insert(BitWiseTrie rootNode, Integer codeWord, int symbol){
        BitWiseTrie node = rootNode;

        int depth = 0;
        //iterate over bits in integer.
        int n = 1;
        while (n < codeWord) {

            int bit = (codeWord & n) > 0 ? 1: 0;

            if(node.children[bit] == null){
                break;
            } else {
                node = node.children[bit];
            }

            n <<= 1;
            depth ++;
        }

        while (n < codeWord) {
            int bit = (codeWord & n) > 0 ? 1: 0;

            node.children[bit] = new BitWiseTrie();
            node = node.children[bit];
            node.depth = depth;

            n <<= 1;
            depth++;
        }

        node.value = symbol;
    }

    public static BitWiseTrie find(BitWiseTrie rootNode, Integer codeWord){
        BitWiseTrie node = rootNode;
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
}
