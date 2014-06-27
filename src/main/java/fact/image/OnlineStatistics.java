package fact.image;

import java.util.LinkedHashMap;

public class OnlineStatistics {

	//this getColorFromValue contains the values needed to calculate the updated average and variance in case a new dataitem arrives
	//this needs to be accessible from the outside so the caller can write whatever data they want to the stream
	private LinkedHashMap<String, double[]> valueMap = new LinkedHashMap<String , double[]>();
	public LinkedHashMap<String, double[]> getValueMap() {	return valueMap; }
	public void setValueMap(LinkedHashMap<String, double[]> valueMap) {	this.valueMap = valueMap;	}


	//this getColorFromValue contains counters that count the number of vales that have arrived for every key
	private LinkedHashMap<String, Integer> counterMap = new LinkedHashMap<String, Integer>();
	
	
	public void updateValues(String key, double[] values){

		if(!valueMap.containsKey(key)){
			valueMap.put(key, values);
		}
		if(values == null) return;
		
		int counter = 1;
		if(counterMap.containsKey(key))
		{
			counter = counterMap.get(key);
		}
		//check if avg and std array already exists
		//The  avg array holds the average of the value. Its being updated for every event.
		double[] avg;
		double[] squaredDiff;
		double[] std;
		double[] stdErr;			
		if(counter==1){
			//initialize values and arrays
			avg = values;
			valueMap.put(key+"_avg", avg);	//init to values
			
			std = new double[values.length];
			valueMap.put(key+"_std", std);	//init to zero
			
			squaredDiff = new double[values.length];

			valueMap.put(key+"_qavg", squaredDiff);	//init to zero
			
			stdErr = new double [values.length];
			valueMap.put(key+"_stdErr", stdErr);

			
		} else {
			avg = valueMap.get(key+"_avg");
			squaredDiff = valueMap.get(key+"_qavg");
			std = valueMap.get(key+"_std");
			stdErr = valueMap.get(key+"_stdErr");
//			double[] fs = valueMap.get(key);
			for(int i = 0; i < values.length; i++){
//				fs[i] = fs[i] +values[i];
				//windowed average
				double previousAvg = 0;
				previousAvg = avg[i];
				avg[i] = avg[i] + (values[i] - avg[i])/((double) counter);	
				//same with qAvg
				squaredDiff[i] = squaredDiff[i] + ( (values[i] - avg[i])*(values[i] - previousAvg ) );
				//save difference to std to get the variance
				std[i] = Math.sqrt((squaredDiff[i])/((double)counter-1));
				//save the stdError
				stdErr[i] = std[i]/Math.sqrt(counter);
			}
		}

		counter++;
		counterMap.put(key, counter);
	}


}
