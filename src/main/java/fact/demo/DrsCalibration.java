/**
 * 
 */
package fact.demo;

import stream.Data;
import stream.Processor;

/**
 * @author chris
 * 
 */
public class DrsCalibration implements Processor {

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		return input;
	}

}
