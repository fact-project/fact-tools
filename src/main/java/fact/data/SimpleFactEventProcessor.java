package fact.data;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;

public abstract class SimpleFactEventProcessor<TInput extends Serializable, TOutput extends Serializable> implements Processor {
	static Logger log = LoggerFactory.getLogger(MaxAmplitude.class);
	
	protected String key = "DataCalibrated";
	protected String outputKey;
	private String color;
	
	@SuppressWarnings("unchecked")
	@Override
	public Data process(Data input) {

			TInput value = null;
			
			if(input.containsKey(key)){
				try{
					value =  (TInput) input.get(key);
				} catch (ClassCastException e){
					//in case value in Map is of the wrong type to do this calculation
					log.error(Constants.EXPECT_ARRAY_F + key + ",  " + this.getClass().getSimpleName() );
					return null;
				}
			} else {
				//key doesn't exist in map. return.
				log.error(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
				return null;
			}

			//if outputkey is not defined just overwrite the old data
			if(outputKey == null || outputKey.equals("")){
				outputKey = key;
			}
			input.put(outputKey, process(value));
			
			//add color value if set
			if(color !=  null && !color.equals("")){
				input.put("@" + Constants.KEY_COLOR + "_"+outputKey, color);
			}
			
			return input;
	}
	
	private TOutput process(TInput value) {
		try{
			return processSeries(value);		
		} catch (ClassCastException e){
			//in case value in Map is of the wrong type to do this calculation
			log.error(Constants.EXPECT_ARRAY_F + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
	}

	//this has to be implemented by all processes subclassing this class
	public abstract TOutput processSeries(TInput data);

	
	
	
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
	
	
	//brownish
	public String getColor() {
		return color;
	}
	@Parameter(required = false, description = "RGB/Hex description String for the color that will be drawn in the FactViewer ChartPanel")
	public void setColor(String color) {
		this.color = color;
	}

}
