package fact.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

public class FitsStream extends AbstractStream {
	static Logger log = LoggerFactory.getLogger(FitsStream.class);
	final static int blockSize = 2880;
	final static int lineLength = 80;

	int roi = 300;
	int numberOfPixel = 1440;
	private DataInputStream dataStream;
	private BufferedInputStream bStream;
	private int[] lengthArray;
	private String[] nameArray;
	private String[] typeArray;
	private Data headerItem = DataFactory.create();

	protected int eventBytes;
	//	private FileChannel inChannel;

	public FitsStream(SourceURL url) {
		super(url);
	}

	/**
	 * This consists of 3 steps 1. Get the size of the fits header. A header
	 * contains 2 subheaders. We ingnore the first one and read the second one
	 * until we reach "END" From the line read we get the header size since we
	 * know its a multiple of the blocksize (2880) 2. Then we parse the headers
	 * for the number of fields the fits file contains. 3. Each file has a name,
	 * datatype and a number of elements. The header is parsed again
	 */
	@Override
	public void init() throws Exception {
		File f = new File(this.url.getFile());
		if (!f.canRead()){
			log.error("Cannot read file. Wrong path? ");
		}
		bStream = new BufferedInputStream(getInputStream());
		dataStream = new DataInputStream(bStream);
		//		inChannel = fileStream.getChannel();

		//		FileInputStream fileStream = new FileInputStream(url.getFile());
		//		bufferdStream = new BufferedInputStream(fileStream);
		//		

		FitsHeader header = this.readHeader(dataStream);
		log.debug("Header #1 read:\n{}", header);

		header = this.readHeader(dataStream);
		log.debug("Header #2 read:\n{}", header);

		int numberOfFields = 0;

		for (String line : header.getLines()) {
			log.trace("Checking line: '{}'", line);
			if (line.startsWith("TFIELDS")) {
				String[] tok = line.split("(=|/)");
				numberOfFields = Integer.parseInt(tok[1].trim());
			}
		}
		log.debug("File has {} fields.", numberOfFields);

		if (numberOfFields == 0) {
			throw new IOException("Fits file appears to have 0 fields");
		}
		typeArray = new String[numberOfFields];
		nameArray = new String[numberOfFields];
		lengthArray = new int[numberOfFields];

		for (String line : header.getLines()) {
			int eq = line.indexOf("=");
			if (eq < 0)
				continue;

			int cs = line.indexOf("/", eq);

			String key = line.substring(0, eq);
			String val = null;
			String comment = null;
			if (cs > 0) {
				val = line.substring(eq + 1, cs);
				comment = line.substring(cs + 1);
			} else {
				val = line.substring(eq + 1);
				comment = "";
			}

			log.debug("key: {}", key);
			log.debug("value: {}", val);
			log.debug("comment: {}", comment);

			if (key.startsWith("TFORM")) {
				//
				// TFORM4 references the (4-1)th index in the column name array
				//
				int index = Integer.parseInt(key.replaceAll("\\D+", "").trim());

				int numberOfElements;
				String value = val.replaceAll("'", "").trim();

				try {
					// parse the numberOfElemetns from the value string by
					// removing all non digit characters
					String numberString = value.replaceAll("\\D+", "").trim();
					numberOfElements = Integer.parseInt(numberString);
				} catch (NumberFormatException e) {
					log.info("Couldnt parse the number of TFORM elements in the header. Assuming a value of 1.");
					numberOfElements = 1;
				}

				// get the type from the name by removing the number
				String type = value.replaceAll("\\d+", "").trim();
				typeArray[index - 1] = type;
				lengthArray[index - 1] = numberOfElements;
			} else if (key.startsWith("TTYPE")) {
				int index = Integer.parseInt(key.replaceAll("\\D+", "").trim());
				nameArray[index - 1] = val.replaceAll("'", "").trim();
				log.debug("field[{}] = {}", index - 1, nameArray[index - 1]);
			} else {
				String value = val.replaceAll("'", "").trim();
				key = key.trim();
				try{
					Integer v = Integer.parseInt(value);
					headerItem.put(key, v);
				} catch (NumberFormatException e){
					try{
						Float v = Float.parseFloat(value);
						headerItem.put(key, v);
					} catch (NumberFormatException ef){
						if(value.equals("f") || value.equals("F")){
							Boolean b = new Boolean(false);
							headerItem.put(key, b);
						} else if (value.equals("t") || value.equals("T")){
							Boolean b = new Boolean(true);
							headerItem.put(key, b);
						} else {
							headerItem.put(key, value);
						}
					}
				}
			}


			if ("NROI".equals(key)) {
				roi = Integer.parseInt(val.trim());
				log.debug("roi is: {}", roi);
			}

			if ("NPIX".equals(key)) {
				numberOfPixel = Integer.parseInt(val.trim());
				log.debug("numberOfPixel is: {}", numberOfPixel);
			}

			if ("NAXIS1".equals(key)) {
				eventBytes = Integer.parseInt(val.trim());
				log.debug("event bytes: {}", eventBytes);
			}
		}
	}

	/**
	 * this parses an event from the datastream and the bytebuffer in case we
	 * read alot of shorts(more than 128) We use a NIO buffer to load a complete
	 * bunch of bytes and intepret them as a short array
	 */
	@Override
	public Data readNext() throws Exception {
		Data item = DataFactory.create(headerItem);

		try {
			for (int n = 0; n < nameArray.length; n++) {
				log.debug("Reading {}", nameArray[n]);
				// read int
				if (typeArray[n].equals("J")) {
					int numberOfelements = lengthArray[n];

					if (numberOfelements > 1) {
						int[] el = new int[numberOfelements];
						for (int i = 0; i < numberOfelements; i++) {
							el[i] = dataStream.readInt();
						}
						item.put(nameArray[n], el);
					} else if (numberOfelements == 1) {
						item.put(nameArray[n], dataStream.readInt());
					}
				}
				//read float
				if (typeArray[n].equals("E")) {
					log.debug("Reading field '{}'", nameArray[n]);
					int numberOfElements = lengthArray[n];
					if (numberOfElements > 128) {
						// save all the floats into a float buffer. to save n
						// floats we need 4*n bytes
						byte[] el = new byte[4 * numberOfElements];
						dataStream.read(el);
						//						bStream.read(el);
						FloatBuffer sBuf = ByteBuffer.wrap(el).asFloatBuffer();
						float[] ar = new float[numberOfElements];
						sBuf.get(ar);
						item.put(nameArray[n], ar);
					} else if (numberOfElements > 1) {
						float[] el = new float[numberOfElements];
						for (int i = 0; i < numberOfElements; i++) {
							el[i] = dataStream.readFloat();
						}
						item.put(nameArray[n], el);
					} else {
						item.put(nameArray[n], dataStream.readFloat());
					}
				}

				// read double
				if (typeArray[n].equals("D")) {
					int numberOfelements = lengthArray[n];

					if (numberOfelements > 1) {
						double[] el = new double[numberOfelements];
						for (int i = 0; i < numberOfelements; i++) {
							el[i] = dataStream.readDouble();
						}
						item.put(nameArray[n], el);
					} else if (numberOfelements == 1) {
						item.put(nameArray[n], dataStream.readDouble());
					}
				}

				// read byte
				if (typeArray[n].equals("B")) {
					int numberOfelements = lengthArray[n];

					if (numberOfelements > 1) {
						byte[] el = new byte[numberOfelements];
						for (int i = 0; i < numberOfelements; i++) {
							el[i] = dataStream.readByte();
						}
						item.put(nameArray[n], el);
					} else if (numberOfelements == 1) {
						item.put(nameArray[n], dataStream.readByte());
					}
				}
				// read byte of type L. whatever that means. is it signed? see page 21 of the .fits file standard pdf
				if (typeArray[n].equals("L")) {
					int numberOfelements = lengthArray[n];

					if (numberOfelements > 1) {
						boolean[] el = new boolean[numberOfelements];
						for (int i = 0; i < numberOfelements; i++) {
							byte b = dataStream.readByte();
							if(b == '0'){
								el[i] = false; 
							} else {
								el[i] = true;
							}
						}
						item.put(nameArray[n], el);
					} else if (numberOfelements == 1) {
						boolean b = false;
						byte c = dataStream.readByte();
						if(c == 0){
							b = false; 
						} else {
							b = true;
						}
						item.put(nameArray[n], b);
					}
				}

				// read a short
				if (typeArray[n].equals("I")) {
					int numberOfelements = lengthArray[n];
					// --------------this is where the magic
					// happens-------------
					if (numberOfelements > 128) {

						// lets try to be even quicker
						// to save n shorts we need 2*n bytes
						//						byte[] el = new byte[2 * numberOfelements];
						//						ByteBuffer byteBuffer2 = ByteBuffer.wrap(el);
						//						inChannel.read(byteBuffer2);
						//						bStream.read(el);
						//						ShortBuffer sBuf = byteBuffer2.asShortBuffer();
						byte[] el = new byte[2 * numberOfelements];
						dataStream.read(el);
						ShortBuffer sBuf = ByteBuffer.wrap(el).asShortBuffer();
						short[] ar = new short[numberOfelements];
						sBuf.get(ar);
						item.put(nameArray[n], ar);
					} else if (numberOfelements > 1) {
						short[] el = new short[numberOfelements];
						for (int i = 0; i < numberOfelements; i++) {
							el[i] = dataStream.readShort();
						}
						item.put(nameArray[n], el);
					} else if (numberOfelements == 1) {
						item.put(nameArray[n], dataStream.readShort());
					}
				}
			}


		} catch (EOFException e) {
			log.info("End of file reached. " + url.getFile());
			dataStream.close();
			return null;
		}

		item.put("@source", url.getProtocol() + ":" + url.getPath());
		item.put("numberOfPixel", numberOfPixel);
		return item;
	}

	public FitsHeader readHeader(InputStream in) throws IOException {
		byte[] data = new byte[16 * 2880];
		int pos = 0;
		int read = in.read(data, pos, 2880);
		boolean parsedHeader = false;

		// try to find the END keyword. 
		while (read > 0) {
			pos += read;
			String str = (new String(data, "US-ASCII")).trim();
			if (str.endsWith("END")) {
				log.debug("Found end-of-header! Header length is {}", pos);
				parsedHeader = true;
				break;
			}
			read = in.read(data, pos, 2880);
			//this might result in an infinite loop also the unit tests fail.
			//			int cur = 0;
			//			while (cur < 2880) {
			//				int br = in.read(data, pos + cur, 2880 - cur);
			//				cur += br;
			//			}
			//			read += cur;
		}

		byte[] header = new byte[pos];
		for (int i = 0; i < header.length; i++) {
			header[i] = data[i];
		}

		if (read % 2880 != 0) {
			throw new IOException("Failed to read header: " + 2880
					+ " bytes expected, only " + read + " bytes could be read!");
		}
		if (!parsedHeader){
			throw new IOException("Failed to read header. Did not find the END keyword");
		}

		return new FitsHeader(header);
	}

	public class FitsHeader {

		final byte[] headerData;

		public FitsHeader(byte[] data) {
			headerData = data;
		}

		public String[] getLines() {

			String[] lines = new String[headerData.length / 80];
			for (int i = 0; i < lines.length; i++) {
				byte[] bytes = new byte[80];
				for (int j = 0; j < bytes.length; j++) {
					bytes[j] = headerData[i * bytes.length + j];
				}
				try {
					lines[i] = new String(bytes, "US-ASCII");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					lines[i] = "";
				}
			}
			return lines;
		}

		public String toString() {
			StringBuffer s = new StringBuffer();
			for (String line : getLines()) {
				s.append(line);
				s.append("\n");
			}
			return s.toString();
		}
	}
}