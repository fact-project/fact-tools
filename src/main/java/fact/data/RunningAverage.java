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
public class RunningAverage implements Processor {
	
	static Logger log = LoggerFactory.getLogger(RunningAverage.class);
	private String color = "#CCCCCC";
	private int length = 5;
	private int[] wheigthsArray = {1,1,1,1,1};
	String key =  Constants.DEFAULT_KEY ;
	String outputKey = Constants.DEFAULT_KEY + "_windowed_average";
	
	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data event) {

			if (event.get(key) == null) {
				System.out.println("ERROR! " + key
						+ " does not exist in FactEvent");
				return null;
			}
			//get data from map
			float[] data = (float[]) event.get(key);
			int roi = data.length / Constants.NUMBEROFPIXEL;

			 // array to keep results. will be initialized with zeros
			float[] result = new float[data.length];

			int pivot = (int) (length/2.0);
				pivot = (length % 2 == 0 ) ? pivot : pivot +1;

			// foreach pixel
			for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
				// result[pix*roi] =
				// iterate over all slices
				for (int slice = 0; slice < roi; slice++) {
					int pos = pix * roi + slice;
					for (int i = -pivot; i < pivot; ++i ){
						
						
					}
				}
			}
			// add key to the output name
//			} else {
//				event.put(Constants.KEY_FIR_RESULT + key, result);
//				event.put("@" + Constants.KEY_COLOR + "_"
//						+ Constants.KEY_FIR_RESULT, color);
//			}

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
}