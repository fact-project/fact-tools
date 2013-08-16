package fact.statistics;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.annotations.Description;
import fact.utils.SimpleFactPixelProcessor;
/**
 * This operator calculates the average of all the slices in each Pixel and stores the result as a double array.
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */

@Description(group = "Data Stream.FACT")
public class PixelAverage extends SimpleFactPixelProcessor {
	static Logger log = LoggerFactory.getLogger(PixelAverage.class);
	

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 **/
	
	@Override
	public float processPixel(float[] pixelData) {
		float avg = 0;
		for (float f : pixelData){
			avg += f;
		}
		return avg/pixelData.length;
	}

	
}
