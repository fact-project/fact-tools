package fact.utils;

import fact.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This operator simply cuts all values below and above the min and maxValue.
 * 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class CutValues implements Processor {
		static Logger log = LoggerFactory.getLogger(CutValues.class);

		@Parameter(required = true)
		private String key;
		@Parameter(required = true)
		private String outputKey;
		private Double minValue = null;
		private Double maxValue = null;

		/**
		 * @see stream.Processor#process(stream.Data)
		 */
		@Override
		public Data process(Data event) {
		
				double[] data = (double[]) event.get(key);
				int roi = data.length / Constants.NUMBEROFPIXEL;
				
				double[] cutValues = new double[data.length];
				
				//for each pixel
				for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {

				//iterate over all slices
					for (int slice = 0; slice < roi; slice++) {
						int pos = pix * roi + slice;
						if(minValue != null && data[pos] < minValue) {cutValues[pos] = minValue;}
						else{cutValues[pos] = data[pos];}
						if(maxValue != null && data[pos] > maxValue) {cutValues[pos] = maxValue;}
					}
				}
				event.put(outputKey, cutValues);
				
			return event;
		}

		
		//Getter and Setter
		
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getOutputKey() {
			return outputKey;
		}

		public void setOutputKey(String outputKey) {
			this.outputKey = outputKey;
		}
		
		public Double getMaxValue() {
			return maxValue;
		}
		@Parameter(required = true, description = "The maximum Value allowed")
		public void setMaxValue(Double maxValue) {
			this.maxValue = maxValue;
		}
		
		public Double getMinValue() {
			return minValue;
		}
		@Parameter(required = true, description = "The minimum Value allowed")
		public void setMinValue(Double minValue) {
			this.minValue = minValue;
		}
}
