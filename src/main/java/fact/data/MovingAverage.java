/**
 * 
 */
package fact.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Description;
import stream.annotations.Parameter;
import fact.Constants;

/**
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
@Description(group = "Fact Tools.Filter", text = "A simple running average")
public class MovingAverage implements Processor {
	
	static Logger log = LoggerFactory.getLogger(MovingAverage.class);
	private String color = "#CFCFCC";
	private int length = 5;
	String key =  Constants.DEFAULT_KEY ;
	String outputKey = "windowed_average";

	

	@Override
	public Data process(Data event) {
		
			if(length%2 != 0){
				length++;
				log.info("CentralMovingAverage only supports even window lengths. New length is: " + length);
			}
			int pivot = (int) (length/2.0);
				
			if (event.get(key) == null) {
				log.error(key
						+ " does not exist in Data");
				return null;
			}

			//get data from map
			float[] data = (float[]) event.get(key);
			
			float[] result;
			result = new float[data.length];
			
			int roi = data.length / Constants.NUMBEROFPIXEL;


			// foreach pixel
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
			event.put(outputKey, result);
			event.put("@" + Constants.KEY_COLOR + "_"
						+ outputKey, color);
		return event;
	}
	


	/* Getter and Setter*/
	
	public String getColor() {
		return color;
	}
	@Parameter(description = "RGB/Hex description String for the color that will be drawn in the FactViewer ChartPanel", defaultValue = "#CCCCCC")
	public void setColor(String color) {
		this.color = color;
	}
	

	
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	

	
	public String getKey() {
		return key;
	}
	@Parameter(description = "The data elements to apply the filter on, i.e. the name of the pixels array (floats).")
	public void setKey(String key) {
		this.key = key;
	}
	
	
	
	
	public String getOutputKey() {
		return outputKey;
	}
	@Parameter(description = "The name of the result array that will be written to the stream.")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}


}