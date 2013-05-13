package fact.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This operator simply multiplies all values by the given factor.
 * 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class SumKey extends SimpleFactEventProcessor<float[], Double> {
	static Logger log = LoggerFactory.getLogger(SumKey.class);
	
	
	@Override
	public Double processSeries(float[] data) {
		double sum = 0;
		for(float i : data){
			sum += i;
		}
//		int roi = data.length / Constants.NUMBEROFPIXEL;
		//for each pixel
//		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
//			//iterate over all slices
//			for (int slice = 0; slice < roi; slice++) {
//				int pos = pix * roi + slice;
//					sum += data[pos];
//			}
//		}
		return sum;	
	}
	
}
