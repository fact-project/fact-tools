package fact.data;


public class ThresholdEventCounter extends
		SimpleFactEventProcessor<float[], Long> {

	private float maxValue = 2048;
	private long counter = 0;
	
	@Override
	public Long processSeries(float[] data) {
		
		for(int t = 0; t < data.length; ++t){
			if(data[t] > maxValue){
				counter++;
				break;
			}
		}
		return counter;
	}
			
}
