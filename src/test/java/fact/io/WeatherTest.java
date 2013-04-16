/**
 * 
 */
package fact.io;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class WeatherTest {

	static Logger log = LoggerFactory.getLogger(WeatherTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			URL url = WeatherTest.class.getResource("/remote-weather.xml");
			stream.run.main(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}