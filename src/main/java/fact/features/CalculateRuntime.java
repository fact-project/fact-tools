package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

// Writes the Runtime in Minutes to the outputkey

public class CalculateRuntime implements Processor {

	private String outputkey = "runtime";
	final Logger log = LoggerFactory.getLogger(MuonHoughTransform.class);

	@Override
	public Data process(Data input) {
		
		Float start_time_decimals = (Float) input.get("TSTARTF");
		Integer start_time_integers = (Integer) input.get("TSTARTI"); 

		Float stop_time_decimals = (Float) input.get("TSTOPF");
		Integer stop_time_integers = (Integer) input.get("TSTOPI");
		
		int int_difference = stop_time_integers - start_time_integers;
		double decimal_difference = (stop_time_decimals - start_time_decimals) * 24 * 60;
		
		Double runtime = int_difference + decimal_difference;
		input.put(outputkey, runtime);
		return input;
	}



	
	public String getOutputkey() {
		return outputkey;
	}

	@Parameter(required=false, description="outputkey", defaultValue="runtime")
	public void setOutputkey(String outputkey) {
		this.outputkey = outputkey;
	}

}