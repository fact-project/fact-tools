package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class TimeSpread implements Processor {
	
	@Parameter(required = false, defaultValue = "pixel:arrivalTimes",
			description = "array with arrivaltimes in pixesls")
	private String arrivalTimeKey 	= "pixel:arrivalTimes";
	@Parameter(required = false, defaultValue = "pixel:estNumPhotons",
				description = "array with weights e.g. number of extracted photons in pixels")
	private String weightsKey 		= "pixel:estNumPhotons";
	@Parameter(required = false, defaultValue = "shower",
				description = "pixels set")
	private String pixelSetKey 		= "shower";
	@Parameter(required = false, defaultValue = "shower:timespread",
				description = "output key")
	private String outputKey 		= "shower:timespread";
	
	@Override
	public Data process(Data item) {
		
		Utils.mapContainsKeys( item, arrivalTimeKey, weightsKey, pixelSetKey);

		double[] arrivalTime 	= (double[]) item.get(arrivalTimeKey);
		double[] weights 		= (double[]) item.get(weightsKey);
		int[] shower 			= ((PixelSet) item.get(pixelSetKey)).toIntArray();
		
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
		double sumt = Utils.arraySum(t);
		// Sum over the weight array:
		double sumw = Utils.arraySum(w);
		// Sum over the weighted time array:
		double sumwt = Utils.arraySum(Utils.arrayMultiplication(w, t));
		// Sum over the element wise multiplication of t and t:
		double sumtt = Utils.arraySum(Utils.arrayMultiplication(t, t));
		// Sum over the element wise multiplication of t and t, weighted:
		double sumwtt = Utils.arraySum(Utils.arrayMultiplication(w, Utils.arrayMultiplication(t, t)));
		
		
		double timespread = Math.sqrt( sumtt / n - Math.pow(sumt/n,2));
		double weightedTimespread = Math.sqrt( sumwtt / sumw - Math.pow(sumwt/sumw,2));
		
		item.put(outputKey, timespread);
		item.put(outputKey+"Weighted", weightedTimespread);
		
		return item;
	}
}
