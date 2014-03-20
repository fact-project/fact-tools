package fact.io.zfits;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import stream.util.parser.ParseException;
import fact.io.zfits.HuffmanCoder.DecodingException;

public class BlockHeader {
	enum Prozessor {
		UNCOMPRESSED(0),
		SMOOTHING(1),
		HUFFMAN(2);
		
		private final short id;
		// using int and casting to short saves us the work to write '(short)x' everywhere above
		private Prozessor(int id) {
			this.id = (short)id;
		}
		public short getId() {
			return this.id;
		}
		public static BlockHeader.Prozessor getProzesserFromId(short id) {
			for (BlockHeader.Prozessor p : Prozessor.values()) {
				if (p.getId() == id)
					return p;
			}
			throw new NoSuchElementException("The given prozessor id: '"+id+"' is not supported");
		}
	}
	enum Ordering {
		ROW('R'),
		COLUMN('C');
		
		private final char character;
		private Ordering(char character) {
			this.character = character;
		}
		public char getCharacter() {
			return this.character;
		}
		public static BlockHeader.Ordering getOrderingFormCharacter(char ch) {
			for (BlockHeader.Ordering o : Ordering.values()) {
				if (o.getCharacter()==ch)
					return o;
			}
			throw new NoSuchElementException("The given ordering: '"+ch+"' byte: '"+(byte)(ch&0x00FF)+"' is not supported");
		}
	}
	private long size;
	private BlockHeader.Ordering ordering;
	private int numProzessors; //should be unsigned char but we don't have it so let it be int
	private BlockHeader.Prozessor[] prozessors;
	
	private byte[] data;
	
	public BlockHeader (byte[] input) throws ParseException {
		if (input.length < (8+1+1))
			throw new ParseException("Block Header is to small, given: "+input.length);
		ByteBuffer buffer = ByteUtil.wrap(input);

		this.size = buffer.getLong();
		this.ordering = Ordering.getOrderingFormCharacter((char)buffer.get());
		this.numProzessors = (int)buffer.get();
		
		this.prozessors = new BlockHeader.Prozessor[this.numProzessors];
		for (int i=0; i < this.numProzessors; i++) {
			this.prozessors[i] = Prozessor.getProzesserFromId(buffer.getShort());
		}
		//read the data into buffer and make it the buffer
		ByteBuffer dataBuffer = ByteUtil.create(buffer.remaining());
		buffer.get(dataBuffer.array());
		this.data = dataBuffer.array();
	}
	
	public String toString() {
		String s = "Size: "+String.format("%6s", this.size)
				 + ", Ordering: "+String.format("%6s", this.ordering.toString())
				 + ", Prozessors Num: "+this.numProzessors
				 + ", P: ";
		for (BlockHeader.Prozessor p : this.prozessors) {
			s += p.toString()+", ";
		}
		return s;
	}
	
	public void unSmoothing() {
		ByteBuffer buffer = ByteUtil.wrap(data);
		long size = buffer.capacity();
		for (int i=2; i<size; i++) {
			byte tmp = (byte)(buffer.get(i) + (buffer.get(i-1)+buffer.get(i-2))/2);
			buffer.put(i, tmp);
		}
		this.data = buffer.array();
	}

	public void unHuffman() throws DecodingException {
		this.data = HuffmanCoder.uncompressData(this.data);
	}

	public byte[] decode() throws DecodingException {
		for (int i=0; i<(int)this.numProzessors; i++) {
			//we must go backwards, the prozessors show the coding not the decoding
			switch (this.prozessors[(this.numProzessors-1)-i]) {
			case UNCOMPRESSED:
				break;
			case SMOOTHING:
				unSmoothing();
				break;
			case HUFFMAN:
				unHuffman();
				break;
			}
		}
		return this.data;
	}
}