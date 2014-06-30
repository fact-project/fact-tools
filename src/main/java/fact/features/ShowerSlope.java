package fact.features;

import fact.EventUtils;
import fact.mapping.FactPixelMapping;
import stream.Data;
import stream.Processor;

public class ShowerSlope implements Processor {

	private String photonChargeKey = null;
	private String arrivalTimeKey = null;
	private String showerKey = null;
	private String cogxKey = null;
	private String cogyKey = null;
	private String deltaKey = null;
	private String outputKey = null;
	
	private double[] photonCharge = null;
	private double[] arrivalTime = null;
	private int[] shower = null;
	private double cogx;
	private double cogy;
	private double delta;
	
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();
	
	@Override
	public Data process(Data input) {
		EventUtils.mapContainsKeys(getClass(), input, photonChargeKey, arrivalTimeKey, showerKey, cogxKey, cogyKey, deltaKey);
		
		photonCharge = (double[]) input.get(photonChargeKey);
		arrivalTime = (double[]) input.get(arrivalTimeKey);
		shower = (int[]) input.get(showerKey);
		cogx = (Double) input.get(cogxKey);
		cogy = (Double) input.get(cogyKey);
		delta = (Double) input.get(deltaKey);
		
		// NumberShowerPixel
		int n = shower.length;
		// in shower coordinates rotated x coord of the shower pixel
		double x[] = new double[n];
		// in shower coordinates rotated x coord of the shower pixel
		double y[] = new double[n];
		// Times of shower pixel
		double t[] = new double[n];
		// Weights of shower pixel
		double w[] = new double[n];
		
		for (int i = 0 ; i < n ; i++)
		{
			int chid = shower[i];
			double xcoord = pixelMap.getPixelFromId(chid).getXPositionInMM();
			double ycoord = pixelMap.getPixelFromId(chid).getYPositionInMM();
			double[] rotPixels = EventUtils.rotatePointInShowerSystem(xcoord, ycoord,cogx, cogy, delta);
			x[i] = rotPixels[0];
			y[i] = rotPixels[1];
			t[i] = arrivalTime[chid];
			w[i] = photonCharge[chid];
		}
		
		// Calculate several element wise multiplication
		double [] xt = EventUtils.arrayMultiplication(x, t);
		double [] yt = EventUtils.arrayMultiplication(y, t);
		double [] xx = EventUtils.arrayMultiplication(x, x);
		double [] yy = EventUtils.arrayMultiplication(y, y);

		// Calculate several sums of arrays
		double sumx = EventUtils.arraySum(x);
		double sumy = EventUtils.arraySum(y);
		double sumt = EventUtils.arraySum(t);
		double sumxt = EventUtils.arraySum(xt);
		double sumyt = EventUtils.arraySum(yt);
		double sumxx = EventUtils.arraySum(xx);
		double sumyy = EventUtils.arraySum(yy);
		
		double slopeLong  = (n*sumxt-sumt*sumx) / (n*sumxx - sumx*sumx);
		double slopeTrans = (n*sumyt-sumt*sumy) / (n*sumyy - sumy*sumy);
		
		// Calculate the difference from 0 (in time) per Pixel for a linear
		// dependency, described by slopeLong
		double b[] = new double[n];
		for (int i = 0 ; i < n ; i++)
		{
			b[i] = t[i] - slopeLong*x[i];
		}
		double [] bb = EventUtils.arrayMultiplication(b, b);
		
		double sumw = EventUtils.arraySum(w);
		double sumb = EventUtils.arraySum(b);
		double sumbb = EventUtils.arraySum(bb);
		double sumwb = EventUtils.arraySum(EventUtils.arrayMultiplication(w, b));
		double sumwbb = EventUtils.arraySum(EventUtils.arrayMultiplication(w, bb));
		
		double slopeSpread = Math.sqrt(sumbb/n - Math.pow(sumb/n, 2));
		double slopeSpreadWeighted = Math.sqrt(sumwbb/sumw - Math.pow(sumwb/sumw, 2));
				
		input.put(outputKey+"_long", slopeLong);
		input.put(outputKey+"_trans", slopeTrans);
		input.put(outputKey+"_spread", slopeSpread);
		input.put(outputKey+"_spread_weighted", slopeSpreadWeighted);
		return input;
	}
	


}
