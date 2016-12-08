package fact.io.hdureader.zfits;

/**
 * Created by mackaiver on 16/11/16.
 */
public class ByteWiseHuffmanTree {

    public Symbol payload = null;
    public ByteWiseHuffmanTree[] children;

    public boolean isLeaf = false;



    public static void insert(ByteWiseHuffmanTree rootNode, int bits, int codeLengthInBits, Symbol symbol){
         if(rootNode.children == null) {
             rootNode.children =new ByteWiseHuffmanTree[256];
         }


        // the alias symbol is bigger then 8 bits we need to extend it
        if (codeLengthInBits > 8) {
            if (rootNode.children[bits & 0xFF] == null) {
                rootNode.children[bits & 0xFF] = new ByteWiseHuffmanTree();
            }
            ByteWiseHuffmanTree.insert(rootNode.children[bits & 0xFF], bits >> 8, codeLengthInBits - 8, symbol);
//            rootNode.children[bits & 0xFF].insertSymbol(symbol, (byte) (numBits - 8), bits >> 8);
            return;
        }

        //calculate the amount of possible combination with the combination bits at the end of the byte
        int numCombination = 1 << (8 - codeLengthInBits);

        //fill all remaining bit combinations. apparently these bits can be filled however you like.
        for (int i = 0; i < numCombination; i++) {
            int key = (bits | (i << codeLengthInBits))&0xFF;

            ByteWiseHuffmanTree node = new ByteWiseHuffmanTree();
            node.payload = symbol;
            node.isLeaf = true;
            rootNode.children[key] = node;
        }
    }

//    public static Symbol getFromNode(ByteWiseHuffmanTree node, int code){
//        return node.children[code];
//    }
//
//    public SymbolTree get(int index) {
//        return this.children[index];
//    }

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
