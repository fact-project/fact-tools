/**
 * 
 */
package fact.io;

import java.io.EOFException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;
import stream.io.SourceURL;

/**
 * @author chris
 * 
 */
public class WeatherStream extends ByteChunkStream {

	static Logger log = LoggerFactory.getLogger(WeatherStream.class);
	public final static int MAX_MESSAGE_LENGTH = 255;
	final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
	InputStream inputStream;
	Long offset = 0L;
	int last = 0;
	int buf = 0;
	final ByteBuffer msg = ByteBuffer.allocate(MAX_MESSAGE_LENGTH);
	String prefix = "weather:";
	boolean debug = false;

	public WeatherStream(SourceURL url) throws Exception {
		super(url, new byte[] { (byte) 0xff, (byte) 0xff });
	}

	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug
	 *            the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @see stream.io.AbstractStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();
		inputStream = new Weird8ByteChunkStream(url.openStream());
	}

	/**
	 * @see stream.io.AbstractStream#readNext()
	 */
	@Override
	public Data read() throws Exception {

		Data item = DataFactory.create();
		item.put("@timestamp", System.currentTimeMillis());
		while (true) {
			try {
				byte[] data = readMessage();
				// checksum(data);

				byte type = data[1];
				log.debug("-------------------Message-------------------");
				log.debug("Length: {}", data.length);
				log.debug("Type: {}", Integer.toHexString(type));
				log.debug("Message: {}", getHex(data, data.length));
				if (debug)
					item.put("weather:data", getHex(data, data.length));
				switch (type) {

				// case 0x42:
				// extractHumidity(data, item);
				// break;

				case 0x60:
					createDateMessage(data, item);
					break;
				}

				item.put("data", data);
				return item;
			} catch (EOFException eof) {
				eof.printStackTrace();
				// return null;
			}
		}
	}

	public byte[] readMessage() throws Exception {
		int len = 0;
		buf = inputStream.read();

		while (true) {
			// log.info("Byte: {}",
			// WeatherStream.getHex(new byte[] { (byte) buf }, 1));

			if (last == 0xffffffff && buf == 0xffffffff) {
				log.debug("Found message header!");
				if (len > 0) {
					log.debug("Last message: {}",
							WeatherStream.getHex(msg.array(), len));

					byte[] data = new byte[len];
					for (int i = 0; i < data.length; i++) {
						data[i] = msg.array()[i];
					}
					len = 0;
					msg.clear();
					return data;
				}
				len = 0;
				msg.clear();
			}

			last = buf;
			if (buf != 0xffffffff) {
				msg.put((byte) buf);
				len++;
			}
			buf = inputStream.read();
		}
	}

	protected void extractPressure(byte[] msg, Data item) {
		int off = -1;

		int rp4 = (int) msg[off + 4];
		int press = (0x0f & (int) msg[off + 5]) * 256 + rp4;
		item.put(prefix + "pressure", press);
	}

	protected void extractHumidity(byte[] msg, Data item) {
		log.info("Extracting humidity from {}", getHex(msg, msg.length));
		int b3 = (int) msg[3];
		int b4 = (int) msg[4];
		item.put(prefix + "temperature", (256 * b4 + b3) / 10.0d);

		int b6 = (int) msg[6];
		int b7 = (int) msg[7];
		item.put(prefix + "dewpoint", (256 * b7 + b6) / 10.0d);

		int h = (int) msg[5];
		item.put(prefix + "humidity", h);
	}

	protected Data createDateMessage(byte[] msg, Data item) {
		int off = 0;
		int min = (int) msg[off + 4];
		int hour = (int) msg[off + 5];
		int day = (int) msg[off + 4];
		int mon = (int) msg[off + 5];
		int year = 2000 + ((int) msg[off + 6]);

		String str = String.format("%4d-%02d-%02d %02d:%02d", year, mon, day,
				hour, min);
		log.debug("Date string is: {}", str);
		str = year + "-" + mon + "-" + day + " " + hour + ":" + min;
		try {
			Date date = dateFormat.parse(str);
			item.put("@timestamp", date.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// int cs = (msg[off + 10] << 8) | msg[off + 11];
		// log.info("Checksum for dateTime is: {}", Integer.toHexString(cs));
		return item;
	}

	public static String getHex(byte[] bytes, int len) {
		return getHex(bytes, 0, len);
	}

	public static String getHex(byte[] bytes, int off, int len) {
		StringBuffer s = new StringBuffer("[");
		for (int i = off; i < bytes.length && i < len; i++) {
			s.append("0x" + Integer.toHexString((int) bytes[i]));
			if (i + 1 < bytes.length)
				s.append(", ");
		}
		s.append("]");
		return s.toString();
	}

	public void checksum(byte[] msg) {
		int sum = 0;
		for (int i = 0; i < msg.length - 2; i++) {
			sum += (int) msg[i];
		}

		int chk = (0xf & msg[msg.length - 1]) << 8
				| (0xf & msg[msg.length - 2]);
		log.debug("Checksum is: {}, check in msg: {}", sum, chk);
	}
}
