package fact.filter;


import fact.Constants;
import fact.utils.SimpleFactEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.annotations.Description;
/**
 * This operator calculates the difference between data[i] and data[i+offset] for each pixel in each event and stores the result as a float array named outputKey. <\br>
 * if i+offset is greater or smaller the current window the first respectively the last value will be repeated. 
 *  
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */

@Description(group = "Data Stream.FACT")
public class MotionDiff extends SimpleFactEventProcessor<double[], double[]> {
	static Logger log = LoggerFactory.getLogger(MotionDiff.class);
	
	private int offset = 0;
	@Override
	public double[] processSeries(double[] data) {
		double[] result =  data;
		if(! key.equals(outputKey)){
			result = new double[data.length];
		}
		
		
		int roi = result.length / Constants.NUMBEROFPIXEL;
		// for each pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//get start and end of slices in event
			int start = pix*roi;
			int end = pix*roi + (roi-1);
			
			for (int slice = 1; slice < roi; slice++) {
				int pos = pix * roi + slice;
				//continue first or last values
				if(pos + offset > end){
					result[pos] = data[pos] - data[end];
				} else if(pos - offset < start ){
					result[pos] = data[pos] - data[start];
				} else{
					result[pos] = data[pos] - data[pos + offset];
				}
			}
		}
		
		return result;
	}
	
	
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
}
