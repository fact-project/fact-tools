package fact.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.annotations.Description;
import fact.Constants;

/**
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
@Description(group = "Fact Tools.Filter", text = "A simple running average")
public class MovingAverage extends SimpleFactEventProcessor<float[],  float[]> {
	
	static Logger log = LoggerFactory.getLogger(MovingAverage.class);

	private int length = 5;

	@Override
	public float[] processSeries(float[] data) {
		if(length%2 == 0){
			length++;
			log.info("CentralMovingAverage only supports uneven window lengths. New length is: " + length);
		}
		int pivot = (int) (length/2.0);
		float[] result;
		result = new float[data.length];
		
		int roi = data.length / Constants.NUMBEROFPIXEL;


		// for each pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			int start = pix*roi;
			int end = pix*roi + (roi-1);
			
			float sum = 0;
			//iterate over window to get sma
			for(int i = 0; i < pivot ; ++i){
				sum += data[i];
			}
			sum  = sum + (pivot*data[pix*roi]);
			result[0] = sum /(2*pivot);
			
			
			for (int slice = 1; slice < roi; slice++) {
				int pos = pix * roi + slice;
				if(pos + pivot > end ){
					result[pos] = result[pos-1] + (data[end] - data[pos-pivot])/length;
				} else if(pos - pivot < start ){
					result[pos] = result[pos-1] + (data[pos+pivot] - data[start])/length;
				} else{
					result[pos] = result[pos-1] + (data[pos+pivot] - data[pos-pivot])/length;
				}
			}
		}
		return result;
	}

	
	
	/* Getter and Setter*/
	
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
}