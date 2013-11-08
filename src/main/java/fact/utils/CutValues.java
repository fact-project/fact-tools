package fact.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;

/**
 * This operator simply cuts all values below and above the min and maxValue.
 * 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class CutValues implements Processor {
		static Logger log = LoggerFactory.getLogger(CutValues.class);

		
		String[] keys = new String[] { Constants.DEFAULT_KEY };
		private Float minValue = null;
		private Float maxValue = null;

		/**
		 * @see stream.DataProcessor#process(stream.Data)
		 */
		@Override
		public Data process(Data event) {

			for (String key : keys) {
		
				if( event.get(key) == null){
					log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
					return null;
				}
				
				float[] data = (float[]) event.get(key);
				int roi = data.length / Constants.NUMBEROFPIXEL;
				
				//foreach pixel
				for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {

				//iterate over all slices
					for (int slice = 0; slice < roi; slice++) {
						int pos = pix * roi + slice;
						if(minValue != null && data[pos] < minValue) {data[pos] = minValue;}
						if(maxValue != null && data[pos] > maxValue) {data[pos] = maxValue;}

					}
 
				}
				event.put(key, data);
				
			}
			return event;
		}

		
		//Getter and Setter
		
		public String[] getKeys() {
			return keys;
		}
		public void setKeys(String[] keys) {
			this.keys = keys;
		}

		
		public Float getMaxValue() {
			return maxValue;
		}
		@Parameter(required = true, description = "The maximum Value allowed")
		public void setMaxValue(Float maxValue) {
			this.maxValue = maxValue;
		}
		
		
		public Float getMinValue() {
			return minValue;
		}
		@Parameter(required = true, description = "The minimum Value allowed")
		public void setMinValue(Float minValue) {
			this.minValue = minValue;
		}
}
