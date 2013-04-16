/**
 * 
 */
package fact.weather;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class ParseWeatherMessage extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(ParseWeatherMessage.class);
	final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
	String key = "data";
	String prefix = "";

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if (!input.containsKey(key)) {
			return input;
		}

		try {
			byte[] data = (byte[]) input.get(key);
			if (data[1] == 0x42) {
				extractHumidity(data, input);
			}

			if (data[1] == 0x41) {
				extractRain(data, input);
			}

			if (data[1] == 0x46) {
				extractPressure(data, input);
			}

			if (data[1] == 0x60) {
				createDateMessage(data, input);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return input;
	}

	protected void extractPressure(byte[] msg, Data item) {
		int b2 = (int) msg[2];
		int b3 = (int) msg[3];
		int press = (0x7 & (int) b3) * 256 + b2;
		item.put(prefix + "pressure", press);
	}

	protected void extractHumidity(byte[] msg, Data item) {
		log.debug("Extracting humidity from {}", getHex(msg, msg.length));
		int b3 = (int) msg[3];
		int b4 = (int) msg[4];
		item.put(prefix + "temperature", (256 * b4 + b3) / 10.0d);

		int b6 = (int) msg[6];
		int b7 = (int) msg[7];
		item.put(prefix + "dewpoint", (256 * b7 + b6) / 10.0d);

		int h = (int) msg[5];
		item.put(prefix + "humidity", h);
	}

	protected void extractRain(byte[] data, Data item) {
		int b2 = (int) data[2];
		int b3 = (int) data[3];

		double rain = (b3 * 256 + b2) / 10.0d * 0.254;
		item.put(prefix + "rain", rain);

		int b8 = (int) data[8];
		int b9 = (int) data[9];
		double totalRain = (b9 * 256 + b8) / 10.0d * 0.254;
		item.put(prefix + "totalRain", totalRain);
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
}