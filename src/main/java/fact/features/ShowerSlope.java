package fact.features;

import fact.Utils;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class ShowerSlope implements Processor {

	@Parameter(required = false, defaultValue = "pixel:estNumPhotons",
				description = "")
	private String estNumPhotonsKey = "pixels:estNumPhotons";
	@Parameter(required = false, defaultValue = "pixel:arrivalTimes",
				description = "")
	private String arrivalTimeKey 	= "pixels:arrivalTimes";
	@Parameter(required = false, defaultValue = "shower",
				description = "")
	private String pixelSetKey 		= "shower";
	@Parameter(required = false, defaultValue = "shower:ellipse:cog:x",
				description = "")
	private String cogxKey 			= "shower:ellipse:cog:x";
	@Parameter(required = false, defaultValue = "shower:ellipse:cog:y",
				description = "")
	private String cogyKey 			= "shower:ellipse:cog:y";
	@Parameter(required = false, defaultValue = "shower:ellipse:delta",
				description = "")
	private String deltaKey 		= "shower:ellipse:delta";
	@Parameter(required = false, defaultValue = "shower:slope:long",
				description = "")
	private String slopeLongOutputKey 			= "shower:slope:long";
	@Parameter(required = false, defaultValue = "shower:slope:trans",
				description = "")
	private String slopeTransOutputKey 			= "shower:slope:trans";
	@Parameter(required = false, defaultValue = "shower:slope:spread",
				description = "")
	private String slopeSpreadOutputKey 		= "shower:slope:spread";
	@Parameter(required = false, defaultValue = "shower:slope:spreadWeighted",
				description = "")
	private String slopeSpreadWeightedOutputKey = "shower:slope:spreadWeighted";
	
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Override
	public Data process(Data item) {
		Utils.mapContainsKeys( item, estNumPhotonsKey, arrivalTimeKey, pixelSetKey, cogxKey, cogyKey, deltaKey);

        double[] estNumPhotons = (double[]) item.get(estNumPhotonsKey);
        double[] arrivalTimes = (double[]) item.get(arrivalTimeKey);
		int[] shower = ((PixelSet) item.get(pixelSetKey)).toIntArray();
		double cogx = (Double) item.get(cogxKey);
		double cogy = (Double) item.get(cogyKey);
		double delta = (Double) item.get(deltaKey);

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
			double[] rotPixels = Utils.transformToEllipseCoordinates(xcoord, ycoord, cogx, cogy, delta);
			x[i] = rotPixels[0];
			y[i] = rotPixels[1];
			t[i] = arrivalTimes[chid];
			w[i] = estNumPhotons[chid];
		}

		// Calculate several element wise multiplication
		double [] xt = Utils.arrayMultiplication(x, t);
		double [] yt = Utils.arrayMultiplication(y, t);
		double [] xx = Utils.arrayMultiplication(x, x);
		double [] yy = Utils.arrayMultiplication(y, y);

		// Calculate several sums of arrays
		double sumx = Utils.arraySum(x);
		double sumy = Utils.arraySum(y);
		double sumt = Utils.arraySum(t);
		double sumxt = Utils.arraySum(xt);
		double sumyt = Utils.arraySum(yt);
		double sumxx = Utils.arraySum(xx);
		double sumyy = Utils.arraySum(yy);

		double slopeLong  = (n*sumxt-sumt*sumx) / (n*sumxx - sumx*sumx);
		double slopeTrans = (n*sumyt-sumt*sumy) / (n*sumyy - sumy*sumy);

		// Calculate the difference from 0 (in time) per Pixel for a linear
		// dependency, described by slopeLong
		double b[] = new double[n];
		for (int i = 0 ; i < n ; i++)
		{
			b[i] = t[i] - slopeLong*x[i];
		}
		double [] bb = Utils.arrayMultiplication(b, b);

		double sumw = Utils.arraySum(w);
		double sumb = Utils.arraySum(b);
		double sumbb = Utils.arraySum(bb);
		double sumwb = Utils.arraySum(Utils.arrayMultiplication(w, b));
		double sumwbb = Utils.arraySum(Utils.arrayMultiplication(w, bb));

		double slopeSpread = Math.sqrt(sumbb/n - Math.pow(sumb/n, 2));
		double slopeSpreadWeighted = Math.sqrt(sumwbb/sumw - Math.pow(sumwb/sumw, 2));

		item.put(slopeLongOutputKey, slopeLong);
		item.put(slopeTransOutputKey, slopeTrans);
		item.put(slopeSpreadOutputKey, slopeSpread);
		item.put(slopeSpreadWeightedOutputKey, slopeSpreadWeighted);
		return item;
	}
}
