/**
 * 
 */
package fact.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.viewer.ui.DefaultPixelMapping;

/**
 * This processors changes the order of the pixels in the data from SoftId to Chid
 * 
 * @author kai
 * 
 */
public class Remapping extends SimpleFactEventProcessor<short[], short[]>{
	static Logger log = LoggerFactory.getLogger(Remapping.class);
	@Override
	public short[] processSeries(short[] data) {
		int roi = data.length / Constants.NUMBEROFPIXEL;
		//copy the whole data into a new array.
		short[] remapped = new short[data.length];
		for(int softId = 0; softId < Constants.NUMBEROFPIXEL; softId++){
			int chid = DefaultPixelMapping.getChidFromSoftId(softId);
			System.arraycopy(data, softId*roi, remapped, chid*roi, roi );
		}
		return remapped;
	}
}