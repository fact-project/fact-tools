package fact.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.jfree.util.Log;
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
	private int headerSize = 7 * blockSize;

	private BufferedInputStream bufferedStream;
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

		bufferedStream = new BufferedInputStream(this.getInputStream());

		// 1. ------------------
		// mark position in bufferedStream so we can reset it later to that
		// position. This assumes there is a maximum header size.
		bufferedStream.mark(2 * headerSize);

		// try to find the size of the header. its n*blocksize long
		byte[] buf = new byte[lineLength];
		// we skip the first header. There ahs to be a second one.
		bufferedStream.skip(blockSize);
		// count the number of bytes we read before we find the end line
		int lineCounter = 0;
		while ((bufferedStream.read(buf)) > 0) {
			lineCounter++;
			String line = new String(buf, "US-ASCII");
			if (line.startsWith("END")) {
				break;
			}
		}
		// we read linecounter * 80 bytes and skipped the first 2880
		// we want to know the number of blocks we read multiplied by the
		// blocksize
		headerSize = (int) ((blockSize + lineCounter * lineLength) / blockSize + 1)
				* blockSize;

		// 2. --------------------
		// lets start over from hte top and read the header to find out how many
		// fields we have.
		bufferedStream.reset();
		bufferedStream.mark(headerSize);
		// skip the first header
		bufferedStream.skip(blockSize);

		int numberOfFields = 0;
		while ((bufferedStream.read(buf)) > 0) {
			String valueName = new String(buf, "US-ASCII");
			if (valueName.startsWith("TFIELDS")) {
				valueName = valueName.split("=|/")[1];
				numberOfFields = Integer.parseInt(valueName.trim());
			} else if (valueName.trim().startsWith("END")) {
				break;
			}
		}
		if (numberOfFields == 0) {
			throw new IOException("Fits file appears to have 0 fields");
		}
		typeArray = new String[numberOfFields];
		nameArray = new String[numberOfFields];
		lengthArray = new int[numberOfFields];

		// 3------------------------
		bufferedStream.reset();
		bufferedStream.mark(headerSize);
		// the first header is completely irrelevant to us
		bufferedStream.skip(blockSize);
		// read number of fields
		while ((bufferedStream.read(buf)) > 0) {
			String valueName = new String(buf);

			if (valueName.substring(0, 3).equals("NROI")) {
				roi = Integer.parseInt(valueName.split("=|/")[1].trim());
			} else if (valueName.startsWith("NAXIS1")) {
				eventBytes = Integer.parseInt(valueName.split("=|/")[1].trim());
				log.debug("Reading " + eventBytes + " bytes per event");
			}
			// keys name
			else if (valueName.startsWith("TTYPE")) {
				String[] split = valueName.split("=|'");
				int number = Integer.parseInt((split[0].replaceAll("\\D+", ""))
						.trim());
				nameArray[number - 1] = split[2].trim();
			}
			// type name . ie. J,I,b....
			// line starts with TFORM1, or TFORM2 ...
			else if (valueName.startsWith("TFORM")) {
				// TODO lets split direferntly
				String[] split = valueName.split("=|/");
				// String comment = split[split.length-1];
				String name = split[0].trim();
				String value = split[1].replaceAll("'", "").trim();

				int numberOfElements;
				int index;
				try {
					// parse the index from the name string by removing all non
					// digit characters
					String indexString = name.replaceAll("\\D+", "").trim();
					index = Integer.parseInt(indexString);
				} catch (NumberFormatException e) {
					Log.error("Couldnt parse the index of the TFORM in the header. Assuming a value of 0");
					index = 0;
				}

				try {
					// parse the numberOfElemetns from the value string by
					// removing all non digit characters
					String numberString = value.replaceAll("\\D+", "").trim();
					numberOfElements = Integer.parseInt(numberString);
				} catch (NumberFormatException e) {
					Log.error("Couldnt parse the number of TFROM elements numbers in the header. Assuming a value of 1");
					numberOfElements = 1;
				}

				// get the type from the name by removing the number
				String type = value.replaceAll("\\d+", "").trim();
				typeArray[index - 1] = type;
				lengthArray[index - 1] = numberOfElements;
				// typeMap.put(valueName.trim(), valueType.trim());
			} else if (valueName.trim().startsWith("END")) {
				break;
			}
		}
		bufferedStream.reset();
		bufferedStream.skip(headerSize);
		dataStream = new DataInputStream(bufferedStream);

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
			bufferedStream.close();

			return null;
		}

		item.put("@source", url.getProtocol() + ":" + url.getPath());

		return item;
	}

}