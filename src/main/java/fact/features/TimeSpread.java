package fact.features;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class TimeSpread implements Processor {
	
	private String arrivalTimeKey = null;
	private String weightsKey = null;
	private String showerKey = null;
	private String outputKey = null;
	
	private double[] arrivalTime = null;
	private double[] weights = null;
	private int[] shower = null;

	@Override
	public Data process(Data input) {
		
		EventUtils.mapContainsKeys(getClass(), input, arrivalTimeKey, weightsKey, showerKey);
		
		arrivalTime = (double[]) input.get(arrivalTimeKey);
		weights = (double[]) input.get(weightsKey);
		shower = (int[]) input.get(showerKey);
		
		// NumberShowerPixel
		int n = shower.length;
		
		// Times of shower pixel
		double[] t = new double[n];
		// Weights of shower pixel
		double[] w = new double[n];
		for (int i = 0 ; i < n ; i++)
		{
			int chid = shower[i];
			t[i] = arrivalTime[chid];
			w[i] = weights[chid];
		}
		
		// Sum over the time array:
		double sumt = EventUtils.arraySum(t);
		// Sum over the weight array:
		double sumw = EventUtils.arraySum(w);
		// Sum over the weighted time array:
		double sumwt = EventUtils.arraySum(EventUtils.arrayMultiplication(w, t));
		// Sum over the element wise multiplication of t and t:
		double sumtt = EventUtils.arraySum(EventUtils.arrayMultiplication(t, t));
		// Sum over the element wise multiplication of t and t, weighted:
		double sumwtt = EventUtils.arraySum(EventUtils.arrayMultiplication(w, EventUtils.arrayMultiplication(t, t)));
		
		
		double timespread = Math.sqrt( sumtt / n - Math.pow(sumt/n,2));
		double weightedTimespread = Math.sqrt( sumwtt / sumw - Math.pow(sumwt/sumw,2));
		
		input.put(outputKey, timespread);
		input.put(outputKey+"_weighted", weightedTimespread);
		
		return input;
	}

}
