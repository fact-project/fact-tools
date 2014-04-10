package fact.utils;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import fact.Constants;

public abstract class SimpleFactEventProcessor<TInput extends Serializable, TOutput extends Serializable> implements StatefulProcessor {
	static Logger log = LoggerFactory.getLogger(SimpleFactEventProcessor.class);

    @Parameter(required =  true, description = "The data elements to apply the filter on, i.e. the name of the pixels array (double).")
    protected String key;
    @Parameter(required = true, description = "The name of the result array that will be written to the stream.")
	protected String outputKey;
    @Parameter(required = false, description = "RGB/Hex description String for the color that will be drawn in the FactViewer GraphWindow")
    private String color;
	
	
	@Override
	public Data process(Data input) {

			if(!input.containsKey(key)){
				//key doesn't exist in map. return.
				log.error("Key not found "  + key + ",  " + this.getClass().getSimpleName() );
				throw new RuntimeException("Key not found");
			}
			//if outputkey is not defined just overwrite the old data
			if(outputKey.equals("")){
				log.error("outputKey in xml was empty.");
                throw new RuntimeException();
			}

			try{
				@SuppressWarnings("unchecked")
				TInput value =  (TInput) input.get(key);
				input.put(outputKey, process(value, input));
			} catch (ClassCastException e){
				//in case value in Map is of the wrong type to do this calculation
				log.error("Wrong type in map for key. " + key + ",  " + this.getClass().getSimpleName() );
                throw new RuntimeException("Wrong type in map for key. " + key + ",  " + this.getClass().getSimpleName());
			}
			//add color value if set
			if(color !=  null && !color.equals("")){
				input.put("@" + Constants.KEY_COLOR + "_"+outputKey, color);
			}
			
			return input;
	}
	
	private TOutput process(TInput value, Data event) {
			return processSeries(value);		
	}

	//this has to be implemented by all processes subclassing this class
	public abstract TOutput processSeries(TInput data);

	//implement the stateful processor interface.
	@Override
	public void init(ProcessContext context){}
	@Override
	public void resetState(){}
	@Override
	public void finish() {}
	
	
	
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
	
	
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}

}
