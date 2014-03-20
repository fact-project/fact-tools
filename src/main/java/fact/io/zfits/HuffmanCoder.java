package fact.io.zfits;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuffmanCoder {
	static Logger log = LoggerFactory.getLogger(HuffmanCoder.class);

	public static class EncodingException extends Exception {
		private static final long serialVersionUID = 6104588043457586725L;

		public EncodingException(String message) {
			super(message);
		}
	}
	public static class DecodingException extends Exception {
		private static final long serialVersionUID = 8266085144654408994L;

		public DecodingException(String message) {
			super(message);
		}
	}

	public static class Encoder {
		public class TreeNode {
			private short symbol = 0;
			private int count = 0;
			private int size = 1; // the size of the tree
			private TreeNode one = null;
			private TreeNode zero = null;

			public TreeNode(short symbol, int count) {
				this.symbol = symbol;
				this.count = count;
			}

			public TreeNode(TreeNode a, TreeNode b) {
				this.zero = (a.count > b.count) ? a : b;
				this.one = (a.count > b.count) ? b : a;
				this.count = a.count + b.count;
				this.size = Math.max(a.size, b.size) + 1;
			}

			public int getCount() {
				return this.count;
			}

			public int getSize() {
				return this.size;
			}

			public short getSymbol() {
				return this.symbol;
			}
			
			public boolean isLeaf() {
				return this.one==null && this.zero==null;
			}
			
			public TreeNode getZero() {
				return this.zero;
			}
			
			public TreeNode getOne() {
				return this.one;
			}
		}

		public class TreeNodeSorter implements Comparator<TreeNode> {
			@Override
			public int compare(TreeNode arg0, TreeNode arg1) {
				int tmp = Integer.compare(arg0.getCount(), arg1.getCount());
				if (tmp == 0) {
					return Integer.compare(arg0.getSize(), arg1.getSize());
				}
				return tmp;
			}

		}

		private TreeNode huffmanTree = null;

		private class Code {
			public Code(int bits, int numBits) {
				this.bits = bits;
				this.numBits = numBits;
			}
			public int bits;
			public int numBits;
		}
		private Code[] symbol2Code = null;
		private int codeTableSize = 4; // 4 for the int(numTableEntries);
		private int numCodeTableEntries = 0;

		private void createSymbolArray(TreeNode node) throws EncodingException {
			if (this.symbol2Code == null) {
				this.symbol2Code = new Code[1<<16];
			}
			createSymbolArray(node, 0, 0);
		}

		private void createSymbolArray(TreeNode node, int bits, int numBits) throws EncodingException {
			if (numBits>32) {
				throw new EncodingException("Huffman Encoder supports only a Bitlengh of maximum 32 bits");
			}
			if (node.isLeaf()) {
				if (numBits==0) {
					throw new EncodingException("We need at least one bit to encode a symbol: '"+node.getSymbol()+"' count: '"+node.getCount()+"'");
				}
				codeTableSize += 2+1+((numBits+7)/8); //short(symbol), byte(numBits), byte[1-4](bits)
				this.symbol2Code[node.getSymbol()&0xFFFF] = new Code(bits, numBits);
				this.numCodeTableEntries++;
			} else {
				createSymbolArray(node.getZero(), bits, numBits+1);
				createSymbolArray(node.getOne(), bits | (1<<numBits), numBits+1);
			}
		}

		public Encoder(ByteBuffer buffer) throws EncodingException {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			if (buffer.capacity()==0) {
				throw new EncodingException("The given buffer is empty");
			}
			int[] countSymbols = new int[1 << 16]; // FIXME check if set zero
			int numOfShorts = buffer.capacity()/2;
			if (buffer.capacity()%2==1)
				throw new EncodingException("Buffer is not even to 2");
			for (int i = 0; i < numOfShorts; i++) {
				int index = buffer.getShort() & 0x0000FFFF;
				countSymbols[index]++;
			}

			PriorityQueue<TreeNode> frequencySortedSymbols = new PriorityQueue<TreeNode>(
					numOfShorts, new TreeNodeSorter());
			for (int i = 0; i < 1<<16; i++) {
				if (countSymbols[i] != 0) {
					TreeNode node = new TreeNode((short) i, countSymbols[i]);
					frequencySortedSymbols.add(node);
				}
			}

			while (frequencySortedSymbols.size() != 1) {
				TreeNode a = frequencySortedSymbols.poll();
				TreeNode b = frequencySortedSymbols.poll();
				TreeNode parent = new TreeNode(a, b);
				frequencySortedSymbols.add(parent);
			}

			this.huffmanTree = frequencySortedSymbols.poll();
			createSymbolArray(this.huffmanTree);
		}
		
		public String codeTableString() {
			String s = "Entries: "+this.numCodeTableEntries+"\n";
			for (int i=0; i<this.symbol2Code.length; i++) {
				Code c = this.symbol2Code[i];
				if (c==null)
					continue;
				s += "\t"+i+ ":" + c.numBits +":'"+String.format("%"+c.numBits+"s", Integer.toBinaryString(c.bits)).replace(' ','0')+"'\n";
			} 
			return s;
		}

		public ByteBuffer createCodeTable() {
			byte[] data = new byte[this.codeTableSize];
			ByteBuffer output = ByteUtil.wrap(data);
			output.putInt(this.numCodeTableEntries);

			int j = 4;
			for (int i=0; i<(1<<16); i++) {
				if (this.symbol2Code[i]==null) { // symbol does not exist ignore 
					continue;
				}
				// write symbol
				output.putShort((short)i);
				j+=2;
				// write number bits
				output.put((byte)(this.symbol2Code[i].numBits));
				j+=1;
				// write bits 
				if (this.symbol2Code[i].numBits<9) { // write one byte
					output.put((byte)this.symbol2Code[i].bits);
					j+=1;
				} else if (this.symbol2Code[i].numBits<17) { //write two bytes
					output.putShort((short)this.symbol2Code[i].bits);
					j+=2;
				} else if (this.symbol2Code[i].numBits<25) { //write three bytes
					output.put((byte)this.symbol2Code[i].bits);
					output.put((byte)(this.symbol2Code[i].bits>>8));
					output.put((byte)(this.symbol2Code[i].bits>>16));
					j+=3;
				} else { //write four bytes
					output.putInt(this.symbol2Code[i].bits);
					j+=4;
				}
			}
			return output;
		}
		public byte[] encode(ByteBuffer input) {
			ByteBuffer output = ByteUtil.create(input.capacity());
			int inputSizeShort = input.capacity()/2;
			//TODO fix uneven input %2
			long buffer = 0;
			int curBit = 0;
			for (int i=0; i<inputSizeShort; i++) {
				int index = input.getShort()&0x0000FFFF;
				Code code = this.symbol2Code[index];

				buffer = buffer | (code.bits<<curBit); // insert bits into buffer
				curBit += code.numBits;
				while (curBit > 8) {
					//we have a full byte write it away
					output.put((byte)(buffer&0xFF));
					buffer = buffer>>8;
					curBit -= 8;
				}
			}
			if (curBit!=0) {
				output.put((byte)(buffer&0xFF));
			}
			byte[] ret = new byte[output.position()];
			output.rewind();
			output.get(ret);
			return ret;
		}
	}

	public static class Decoder {
		private class SymbolEntry {
			public short symbol;
			public int numBits;

			SymbolEntry(short symbol, int numBits) {
				this.symbol = symbol;
				this.numBits = numBits;
			}
		}

		private class SymbolTree {
			private boolean isLeaf = false;
			private SymbolEntry entry = null;
			private SymbolTree[] children = null;

			public SymbolTree(SymbolEntry entry) {
				this.entry = entry;
				this.isLeaf = true;
			}

			public SymbolTree() {
			}

			public void insertSymbol(short symbol, int numBits, int bits) {
				if (this.children == null)
					children = new SymbolTree[256];

				// the alias symbol is bigger then 8 bits we need to extend it
				if (numBits > 8) {
					if (children[bits & 0xFF] == null)
						children[bits & 0xFF] = new SymbolTree();
					children[bits & 0xFF].insertSymbol(symbol,
							(byte) (numBits - 8), bits >> 8);
					return;
				}

				//calculate the amount of possible combination with the combination bits at the end of the byte
				int numCombination = 1 << (8 - numBits);

				for (int i = 0; i < numCombination; i++) {
					int key = (bits | (i << numBits))&0xFF;

					children[key] = new SymbolTree(new SymbolEntry(symbol,
							numBits));
				}
			}

			public boolean isLeaf() {
				return this.isLeaf;
			}

			public short getSymbole() {
				return this.entry.symbol;
			}

			public SymbolTree get(int index) {
				return this.children[index];
			}

			public int getNumBits() {
				return this.entry.numBits;
			}
		}

		private SymbolTree tree = new SymbolTree();

		Decoder(ByteBuffer buffer) throws DecodingException {
			// first 4 bytes are the number of entries
			int numEntries = (int)buffer.getLong();//TODO was int
			//log.info("Decoding HuffmanTree, {} entries", numEntries);
			// read all entries and create the decoding tree 
			for (int i = 0; i < numEntries; i++) {
				// first two bytes are the symbole
				short symbol = buffer.getShort();

				if (numEntries == 1) { // only one entry
					tree.insertSymbol(symbol, (byte) 0, 0);
					break;
				}

				int numBits = buffer.get()&0xFF;
				int numBytes = (((int) numBits) + 7) / 8;

				if (numBytes > 4) {
					//FIXME System.out.println("Bits: '"+numBits+"'");
					throw new DecodingException("Number of bytes in a single symbol is bigger then 4 bytes.");
				}

				// read the lookout of the bits of the Huffman representation of
				// the symbol
				byte[] tmp = new byte[4];
				buffer.get(tmp, 0, numBytes);
				// convert byte array to an int
				int bits = ByteUtil.wrap(tmp).getInt();
				//log.info("Sym: '{}', '{}', '{}', '{}'", symbol, numBits, numBytes, String.format("%"+numBits+"s", Integer.toBinaryString(bits)).replace(' ', '0'));
				tree.insertSymbol(symbol, numBits, bits);
			}
		}

		public ByteBuffer decode(ByteBuffer buffer, long sizeDecoded) {
			//log.info("decoding to {} bytes ", sizeDecoded);
			//ByteBuffer buffer = ByteBuffer.wrap(inputData);
			ByteBuffer outputBuffer = ByteUtil.create(sizeDecoded);
			int curBit = 0;
			int bufferPosition = buffer.position();
			SymbolTree curTree = this.tree;

			int bufferSize = buffer.capacity();

			while (bufferSize != bufferPosition) {
				short data = 0;
				if (bufferPosition+1==bufferSize) {
					data = (short)(buffer.get(bufferPosition)&0x00FF);
				} else {
					// get 2 bytes to extract one byte from the curBit position 
					data = buffer.getShort(bufferPosition);	
				}
				byte curByte = (byte) (data >> curBit);
				int index = curByte&0xFF;

				curTree = curTree.get(index);
				if (curTree.isLeaf() == false) {
					bufferPosition++;
					continue;
				}

				// write down symbol
				outputBuffer.putShort(curTree.getSymbole());
				if (outputBuffer.remaining()==0) {
					break;
				}
				curBit += curTree.getNumBits();

				curTree = this.tree;
				if (curBit >= 8) {
					curBit %= 8;
					bufferPosition++;
				}
			}
			return outputBuffer;
		}
	}

	private Decoder decoder = null;
	private ByteBuffer result = null;

	HuffmanCoder(ByteBuffer buffer) throws DecodingException {
		// first four bytes is the size of the decompressed data
		int dataSize = buffer.getInt();

		this.decoder = new Decoder(buffer);

		this.result = this.decoder.decode(buffer, dataSize);
	}

	public ByteBuffer getResult() {
		return this.result;
	}
	
	public static byte[] uncompressData(byte[] input) throws DecodingException {
		ByteBuffer buffer = ByteUtil.wrap(input);
		// first four bytes is the size of the decompressed data
		//load data fast
		buffer.getInt();
		long dataSize = buffer.getLong()*2; //size is stored in num of shorts
		//log.info("Decompressed data size is {} bytes.", dataSize);
		Decoder decoder = new Decoder(buffer);
		return decoder.decode(buffer, dataSize).array();
	}

	public static byte[] compressData(byte[] input) throws EncodingException {
		Encoder encoder = new Encoder(ByteUtil.wrap(input));
		ByteBuffer codeTable = encoder.createCodeTable();
		byte[] encodedData = encoder.encode(ByteUtil.wrap(input));

		ByteBuffer tmp = ByteUtil.wrap(new byte[codeTable.capacity()+encodedData.length+4]);
		tmp.putInt(input.length);
		tmp.put(codeTable.array());
		tmp.put(encodedData);
		return tmp.array();
	}
}
