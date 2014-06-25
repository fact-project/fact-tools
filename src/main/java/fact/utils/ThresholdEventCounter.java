package fact.utils;


public class ThresholdEventCounter extends
		SimpleFactEventProcessor<double[], Long> {

	private double maxValue = 5;    
	private long counter = 0;
	
	@Override
	public Long processSeries(double[] data) {
		
		for(int t = 0; t < data.length; ++t){
			if(data[t] > maxValue){
				counter++;
				break;
			}
		}
		return counter;
	}
			
}
