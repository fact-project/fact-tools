package fact.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
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
	private DataInputStream dataStream;
	private int[] lengthArray;
	private String[] nameArray;
	private String[] typeArray;

	protected int eventBytes;

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

		dataStream = new DataInputStream(new BufferedInputStream(
				getInputStream()));

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
			if (cs < 0) {
				continue;
			}

			String key = line.substring(0, eq);
			String val = line.substring(eq + 1, cs);
			String comment = line.substring(cs + 1);

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
					log.error("Couldnt parse the number of TFROM elements numbers in the header. Assuming a value of 1");
					numberOfElements = 1;
				}

				// get the type from the name by removing the number
				String type = value.replaceAll("\\d+", "").trim();
				typeArray[index - 1] = type;
				lengthArray[index - 1] = numberOfElements;
			}

			if (key.startsWith("TTYPE")) {
				int index = Integer.parseInt(key.replaceAll("\\D+", "").trim());
				nameArray[index - 1] = val.replaceAll("'", "").trim();
			}

			if ("NROI".equals(key)) {
				roi = Integer.parseInt(val.trim());
				log.info("roi is: {}", roi);
			}

			if ("NAXIS1".equals(key)) {
				eventBytes = Integer.parseInt(val.trim());
				log.info("event bytes: {}", eventBytes);
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
		Data item = DataFactory.create();

		try {
			for (int n = 0; n < nameArray.length; n++) {

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

				// read a short
				if (typeArray[n].equals("I")) {
					int numberOfelements = lengthArray[n];
					// --------------this is where the magic
					// happens-------------
					if (numberOfelements > 128) {
						// lets try to be even quicker
						// to save n shorts we need 2*n bytes
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

			new fact.processors.Short2Float().process(item);

		} catch (EOFException e) {
			log.info("End of file reached. ");
			dataStream.close();
			return null;
		}

		item.put("@source", url.getProtocol() + ":" + url.getPath());

		return item;
	}

	public FitsHeader readHeader(DataInputStream in) throws IOException {

		byte[] data = new byte[16 * 2880];
		int pos = 0;
		int read = in.read(data, pos, 2880);
		while (read > 0) {
			pos += read;

			if ((new String(data, "US-ASCII")).trim().endsWith("END")) {
				log.info("Found end-of-header! Header length is {}", pos);
				break;
			}

			read = in.read(data, pos, 2880);
		}

		byte[] header = new byte[pos];
		for (int i = 0; i < header.length; i++) {
			header[i] = data[i];
		}

		if (read % 2880 != 0) {
			throw new IOException("Failed to read header: " + 2880
					+ " bytes expected, only " + read + " bytes could be read!");
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