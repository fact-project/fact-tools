package fact.processors.parfact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Processor;
import stream.annotations.Parameter;
import stream.Data;
import fact.Constants;

/**
 * This operator simply multiplies all values by the given factor.
 * 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class MonteCarloCalibration implements Processor {
		static Logger log = LoggerFactory.getLogger(MonteCarloCalibration.class);

		
		String[] keys = new String[] { Constants.DEFAULT_KEY_MC };
		public String[] getKeys() {
			return keys;
		}
		public void setKeys(String[] keys) {
			this.keys = keys;
		}

		
		private float offset = -1000;
		public float getOffset() {
			return offset;
		}
		@Parameter(required = false, description = "The offset value", defaultValue="-1000")
		public void setOffset(float offset) {
			this.offset = offset;
		}


		private float factor = 190.0f;
		public float getFactor() {
			return factor;
		}
		@Parameter(required = false, description = "The gain Factor", defaultValue="190.0")
		public void setFactor(float threshold) {
			this.factor = threshold;
		}
		
		private boolean overWrite = true;
		public boolean isOverWrite() {
			return overWrite;
		}
		@Parameter(required = false, description = "If true the DataMC key will be overwritten, else a key DataMCCalibrated will be insertet", defaultValue="190.0")
		public void setOverWrite(boolean overWrite) {
			this.overWrite = overWrite;
		}

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
				
				// Iterieren ueber alle Pixel
				float[] data = (float[]) event.get(key);
				int roi = data.length / Constants.NUMBEROFPIXEL;
				float[] vals = null;
				if (!overWrite){
					vals = new float[data.length];
				} else {
					vals = data;
				}
				//foreach pixel
				for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {

//					//iterate over all slices
					for (int slice = 0; slice < roi; slice++) {
						int pos = pix * roi + slice;
						vals[pos] = (data[pos] + offset)/factor;
					}
				}
				// add key to the output name
				if(!overWrite){
					event.put(key+"Calibrated", vals);
				} else {
					event.put(key, vals);
				}

			}
			return event;
		}

	
}
