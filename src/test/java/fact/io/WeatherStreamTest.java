/**
 * 
 */
package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;

/**
 * @author chris
 * 
 */
public class WeatherStreamTest {

	static Logger log = LoggerFactory.getLogger(WeatherStreamTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			SourceURL url = new SourceURL(
					WeatherStreamTest.class.getResource("/weather.data"));
			url = new SourceURL("file:/Users/chris/weather.data/");
			WeatherStream stream = new WeatherStream(url);
			stream.init();

			Data item = stream.read();
			while (item != null) {
				log.info("item: {} (size: {})", item,
						((byte[]) item.get("data")).length);

				item = stream.read();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}