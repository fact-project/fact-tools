package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class FitsStream extends AbstractStream {
	static Logger log = LoggerFactory.getLogger(FitsStream.class);

	int numberOfPixel;
    int blockSize;

    private final int MAX_HEADER_BYTES = 16*2880;
	private DataInputStream dataStream;
	private int[] lengthArray;
	private String[] nameArray;
	private String[] typeArray;
	private Data headerItem = DataFactory.create();

    @Parameter(required = false, description = "This value defines the size of the buffer of the BufferedInputStream", defaultValue = "8*1024")
	private int bufferSize = 8*1024;

    private int headerLength = 0;

	public FitsStream(SourceURL url) {
		super(url);
	}
	
	public FitsStream() {
		super();
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
		super.init();
		File f = new File(this.url.getFile());
		if (!f.canRead()){
			log.error("Cannot read file. Wrong path? " + f.getAbsolutePath());
			throw new FileNotFoundException("Cannot read file " + f.getAbsolutePath());
		}
        BufferedInputStream bStream = new BufferedInputStream(getInputStream(), bufferSize );
		dataStream = new DataInputStream(bStream);
        dataStream.mark(MAX_HEADER_BYTES);

		FitsHeader header = new FitsHeader(dataStream);
		log.debug("Header #1 read:\n{}", header);
        headerLength = header.getLength();
        dataStream.reset();
        dataStream.skip(headerLength);

		header = new FitsHeader(dataStream);
		log.debug("Header #2 read:\n{}", header);
        headerLength += header.getLength();
        dataStream.reset();

        //now create a Data Item containing header information
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
			String val;
			String comment;
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
					log.debug("Couldnt parse the number of TFORM elements in the header. Assuming a value of 1.");
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
							headerItem.put(key, false);
						} else if (value.equals("t") || value.equals("T")){
							headerItem.put(key, true);
						} else {
							headerItem.put(key, value);
						}
					}
				}
			}
		}

        if ( headerItem.get("NAXIS1") != null ){
            blockSize = (Integer) headerItem.get("NAXIS1");
        }
        if (headerItem.get("NPIX") != null){
            numberOfPixel = (Integer) headerItem.get("NPIX");
        }

        long skipped = dataStream.skip(headerLength);
        if (skipped != headerLength){
            log.error("Error while reading the FITS Header. Header length wrong?");
        }
    }

	/**
	 * this parses an event from the datastream and the bytebuffer in case we
	 * read alot of shorts(more than 128) We use a NIO buffer to load a complete
	 * bunch of bytes and intepret them as a short array
	 */
	@Override
	public Data readNext() throws Exception {

//		FactEvent item = new FactEvent();
		Data item = DataFactory.create(headerItem);
//		Data item = headerItem;

        try {
            dataStream.mark(blockSize + 1);
            long byteCounter = 0;
            for (int n = 0; n < nameArray.length; n++) {
				log.debug("Reading {}", nameArray[n]);
				// read int
				if (typeArray[n].equals("J")) {
					int numberOfelements = lengthArray[n];

					if (numberOfelements > 1) {
						int[] el = new int[numberOfelements];
						for (int i = 0; i < numberOfelements; i++) {
							el[i] = dataStream.readInt();
                            byteCounter += 4;
						}
						item.put(nameArray[n], el);
					} else if (numberOfelements == 1) {
                        byteCounter += 4;
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

                        byteCounter += el.length;
                        dataStream.read(el);
                        //						bStream.read(el);
						FloatBuffer sBuf = ByteBuffer.wrap(el).asFloatBuffer();
						float[] ar = new float[numberOfElements];
						sBuf.get(ar);
						item.put(nameArray[n], ar);
					} else if (numberOfElements > 1) {
						float[] el = new float[numberOfElements];
						for (int i = 0; i < numberOfElements; i++) {
                            byteCounter += 4;
							el[i] = dataStream.readFloat();
						}
						item.put(nameArray[n], el);
					} else if (numberOfElements == 1){
                        byteCounter += 4;
						item.put(nameArray[n], dataStream.readFloat());
					}
				}

//				// read double
				if (typeArray[n].equals("D")) {
					int numberOfelements = lengthArray[n];

					if (numberOfelements > 1) {
						double[] el = new double[numberOfelements];
						for (int i = 0; i < numberOfelements; i++) {
                            byteCounter += 8;
							el[i] = dataStream.readDouble();
						}
						item.put(nameArray[n], el);
					} else if (numberOfelements == 1) {
                        byteCounter += 8;
						item.put(nameArray[n], dataStream.readDouble());
					}
				}
//
//				// read byte
				if (typeArray[n].equals("B")) {
					int numberOfelements = lengthArray[n];

					if (numberOfelements > 1) {
						byte[] el = new byte[numberOfelements];
						for (int i = 0; i < numberOfelements; i++) {
                            byteCounter += 1;
							el[i] = dataStream.readByte();
						}
						item.put(nameArray[n], el);
					} else if (numberOfelements == 1) {
                        byteCounter += 1;
						item.put(nameArray[n], dataStream.readByte());
					}
				}
				// read byte of type L. whatever that means. is it signed? see page 21 of the .fits file standard pdf
				if (typeArray[n].equals("L")) {
					int numberOfelements = lengthArray[n];

					if (numberOfelements > 1) {
						boolean[] el = new boolean[numberOfelements];
						for (int i = 0; i < numberOfelements; i++) {

                            byteCounter += 1;
							byte b = dataStream.readByte();

                            el[i] = (b != '0');
						}
						item.put(nameArray[n], el);
					} else if (numberOfelements == 1) {
						boolean b;
                        byteCounter += 1;
						byte c = dataStream.readByte();
                        b = (c != 0);
						item.put(nameArray[n], b);
					}
				}


                if (typeArray[n].equals("K")){
                    int numberOfelements = lengthArray[n];
                    if (numberOfelements > 1) {
                        long[] el = new long[numberOfelements];
                        for (int i = 0; i < numberOfelements; i++) {
                            byteCounter += 8;
                            long b = dataStream.readLong();
                            el[i] = b;
                        }
                        item.put(nameArray[n], el);
                    } else if (numberOfelements == 1) {
                        byteCounter += 8;
                        long c = dataStream.readLong();
                        item.put(nameArray[n], c);
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

                        byteCounter += el.length;
						dataStream.read(el);

                        ShortBuffer sBuf = ByteBuffer.wrap(el).asShortBuffer();
						short[] ar = new short[numberOfelements];
						sBuf.get(ar);
						item.put(nameArray[n], ar);
						
					} else if (numberOfelements > 1) {
						short[] el = new short[numberOfelements];
						for (int i = 0; i < numberOfelements; i++) {
                            byteCounter += 2;
							el[i] = dataStream.readShort();
						}
						item.put(nameArray[n], el);
					} else if (numberOfelements == 1) {
                        byteCounter += 2;
						item.put(nameArray[n], dataStream.readShort());
					}
				}
			}

            dataStream.reset();
            long skipped =  dataStream.skip(blockSize);
            if (skipped != byteCounter){
                log.debug("Error while reading block. EOF?");
            }

		} catch (EOFException e) {
			log.info("End of file reached. " + url.getFile());
			dataStream.close();
			return null;
		}
		item.put("@source", url.getProtocol() + ":" + url.getPath());
		item.put("@numberOfPixel", numberOfPixel);
		return item;
	}

	public class FitsHeader {

		final byte[] headerData;

        public FitsHeader(InputStream in) throws IOException {
            byte[] data = new byte[MAX_HEADER_BYTES];
            boolean parsedHeader = false;

            int pos = 0;
            int read = in.read(data, pos, 2880);
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
            }
            headerData = new byte[pos];
            System.arraycopy(data, 0, headerData, 0, headerData.length);
            if (read % 2880 != 0) {
                throw new IOException("Failed to read header: n*" + 2880
                        + " bytes expected, only " + pos + " bytes could be read!");
            }
            if (!parsedHeader){
                throw new IOException("Failed to read header. Did not find the END keyword");
            }
            log.debug("Bytes in header: " + pos + " read: " + read);
        }

        public int getLength(){
            return headerData.length;
        }

		public String[] getLines() {

			String[] lines = new String[headerData.length / 80];
			for (int i = 0; i < lines.length; i++) {
				byte[] bytes = new byte[80];
//				for (int j = 0; j < bytes.length; j++) {
//					bytes[j] = headerData[i * bytes.length + j];
//				}
				System.arraycopy(headerData, i*bytes.length, bytes, 0, bytes.length);
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

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}