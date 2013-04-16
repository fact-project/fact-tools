package fact.data;


import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Description;
import fact.Constants;
import fact.image.OnlineStatistics;
/**
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */

@Description(group = "Data Stream.FACT")
public class OnlineAverage implements Processor {
	static Logger log = LoggerFactory.getLogger(OnlineAverage.class);
	OnlineStatistics onStat = new OnlineStatistics();
	private boolean calcStd = false;
	private String output, key;


	public OnlineAverage(){}
	public OnlineAverage(String key){
		this.key =  key;
	}

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		if(output == null || output ==""){		
			input.put(key+"_avg", (processEvent(input, key))[0]);
			if(calcStd){
				input.put(key+"_std", onStat.getValueMap().get(key+"_std"));
			}
		} else {
			input.put(output+"_avg", (processEvent(input, key))[0]);
			if(calcStd){
				input.put(output+"_std", onStat.getValueMap().get(key+"_std"));
			}
		}

		return input;
	}


	public double[] processEvent(Data input, String key) {

		Serializable value = null;

		if(input.containsKey(key)){
			value = input.get(key);
		} else {
			//key doesnt exist in map
			log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}

		Class<?> clazz = value.getClass().getComponentType();
		if (value != null && 
				value.getClass().isArray() && 
				(clazz.equals(float.class)||clazz.equals(double.class)||clazz.equals(int.class)) )
		{
			double[] valArray = EventUtils.toDoubleArray(value);
			onStat.updateValues(key, valArray);		
			return onStat.getValueMap().get(key+"_avg");
		}
		//in case value in Map is of the wrong type to do this calculation
		else
		{
			log.info(Constants.EXPECT_ARRAY + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}

	}

	/*
	 * Getter and Setter
	 */
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}






}
