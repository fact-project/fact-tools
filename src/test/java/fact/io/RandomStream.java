/**
 * 
 */
package fact.io;

import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;

/**
 * @author chris
 * 
 */
public class RandomStream extends AbstractStream {

	/**
	 * @see stream.io.AbstractStream#readNext()
	 */
	@Override
	public Data readNext() throws Exception {
		Data item = DataFactory.create();
		item.put("value", Math.random());
		return item;
	}
}
