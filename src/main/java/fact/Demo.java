/**
 * 
 */
package fact;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class Demo {

	static Logger log = LoggerFactory.getLogger(Demo.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		log.info("#");
		log.info("#");
		log.info("#  FACT-Tools Demo");
		log.info("#");
		URL url = Demo.class.getResource("/fact-tools-demo.xml");
		log.info("#  Using process {}", url);
		log.info("#");
		log.info("#");
		stream.run.main(url);
	}
}
