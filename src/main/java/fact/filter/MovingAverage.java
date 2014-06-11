package fact.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.annotations.Description;
//import stream.annotations.Parameter;
import fact.Constants;
import fact.utils.SimpleFactEventProcessor;

/**
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
@Description(group = "Fact Tools.Filter", text = "A simple running average")
public class MovingAverage extends SimpleFactEventProcessor<double[],  double[]> {
	
	static Logger log = LoggerFactory.getLogger(MovingAverage.class);


	private int length = 5;

	@Override
	public double[] processSeries(double[] data) {
		if(length%2 == 0){
			length++;
			log.info("CentralMovingAverage only supports uneven window lengths. New length is: " + length);
		}
		int pivot = (int) (length/2.0);
		double[] result;
		result = new double[data.length];
		
		int roi = data.length / Constants.NUMBEROFPIXEL;

		for(int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			double sum = 0;
			for(int slice = 0; slice <= pivot; slice++){
				int pos = pix * roi + slice;
					
				for(int slice1 = 0; slice1 <= slice+pivot; slice1++){
						int pos1 = pix*roi + slice1;
						sum += data[pos1];
					}
				
				double average = sum/(pivot+slice);
				result[pos]=average;
			}
			
			for(int slice = pivot; slice <= (roi - 1) - pivot; slice++){
				sum = 0;
				int pos = pix * roi + slice;
				int start = slice - pivot;
				int end = slice + pivot;
				
				for(int i = start; i <= end; i++){
					int pos1 = pix*roi + i;
					sum += data[pos1];
				}		
				
				double average = sum/length;
				result[pos]=average;
			}
					
			for(int slice = roi - pivot; slice < roi; slice++){
				sum = 0;
				int pos = pix*roi + slice;
				
				for(int slice1 = roi - pivot; slice1 < roi; slice1++){
					int pos1 = pix*roi + slice1;
					sum += data[pos1];
				}
				
				double average = sum/(roi-slice);
				result[pos]=average;
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