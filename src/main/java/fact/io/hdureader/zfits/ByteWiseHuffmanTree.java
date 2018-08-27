package fact.io.hdureader.zfits;

/**
 * A thing I call a ByteWiseHuffmann tree. Each nodes has up to 256 children. This works because
 * the huffmann codes in the zfits files are aligned to bytes. So once a codeword is longer than one byte we
 * traverse down a level. Each leaf contains a ByteWiseTree.Symbol object
 *
 * @see ByteWiseHuffmanTree.Symbol
 * <p>
 * This implementation is quite similar to the things T Bretz and M Bulinski did.
 * <p>
 * Created by mackaiver on 16/11/16.
 */
public class ByteWiseHuffmanTree {

    public Symbol payload = null;
    public ByteWiseHuffmanTree[] children;

    public boolean isLeaf = false;


    /**
     * Insert a symbol into this huffmann tree.
     *
     * @param rootNode         the rootnode of the tree to insert into.
     * @param bits             the bots of the codeword as int.
     * @param codeLengthInBits the length of the codeword to insert.
     * @param symbol           the symbol to store in the leaf.
     */
    public static void insert(ByteWiseHuffmanTree rootNode, int bits, int codeLengthInBits, Symbol symbol) {
        if (rootNode.children == null) {
            rootNode.children = new ByteWiseHuffmanTree[256];
        }


        // the alias symbol is bigger then 8 bits we need to extend it
        if (codeLengthInBits > 8) {
            if (rootNode.children[bits & 0xFF] == null) {
                rootNode.children[bits & 0xFF] = new ByteWiseHuffmanTree();
            }
            ByteWiseHuffmanTree.insert(rootNode.children[bits & 0xFF], bits >> 8, codeLengthInBits - 8, symbol);
            return;
        }

        //calculate the amount of possible combination with the combination bits at the end of the byte
        int numCombination = 1 << (8 - codeLengthInBits);

        //fill all remaining bit combinations. apparently these bits can be filled however you like.
        for (int i = 0; i < numCombination; i++) {
            int key = (bits | (i << codeLengthInBits)) & 0xFF;

            ByteWiseHuffmanTree node = new ByteWiseHuffmanTree();
            node.payload = symbol;
            node.isLeaf = true;
            rootNode.children[key] = node;
        }
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
