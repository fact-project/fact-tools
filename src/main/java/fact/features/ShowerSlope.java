package fact.features;

import fact.Utils;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSetOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class ShowerSlope implements Processor {

	@Parameter(required = true)
	private String photonChargeKey = null;
	@Parameter(required = true)
	private String arrivalTimeKey = null;
	@Parameter(required = true)
	private String pixelSetKey = null;
	@Parameter(required = true)
	private String cogxKey = null;
	@Parameter(required = true)
	private String cogyKey = null;
	@Parameter(required = true)
	private String deltaKey = null;
	@Parameter(required = true)
	private String slopeLongOutputKey = null;
	@Parameter(required = true)
	private String slopeTransOutputKey = null;
	@Parameter(required = true)
	private String slopeSpreadOutputKey = null;
	@Parameter(required = true)
	private String slopeSpreadWeightedOutputKey = null;
	
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys( input, photonChargeKey, arrivalTimeKey, pixelSetKey, cogxKey, cogyKey, deltaKey);

        double[] photonCharge = (double[]) input.get(photonChargeKey);
        double[] arrivalTime = (double[]) input.get(arrivalTimeKey);
		int[] shower = ((PixelSetOverlay) input.get(pixelSetKey)).toIntArray();
		double cogx = (Double) input.get(cogxKey);
		double cogy = (Double) input.get(cogyKey);
		double delta = (Double) input.get(deltaKey);

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
			t[i] = arrivalTime[chid];
			w[i] = photonCharge[chid];
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

		input.put(slopeLongOutputKey, slopeLong);
		input.put(slopeTransOutputKey, slopeTrans);
		input.put(slopeSpreadOutputKey, slopeSpread);
		input.put(slopeSpreadWeightedOutputKey, slopeSpreadWeighted);
		return input;
	}

	public String getPhotonChargeKey() {
		return photonChargeKey;
	}

	public void setPhotonChargeKey(String photonChargeKey) {
		this.photonChargeKey = photonChargeKey;
	}

	public String getArrivalTimeKey() {
		return arrivalTimeKey;
	}

	public void setArrivalTimeKey(String arrivalTimeKey) {
		this.arrivalTimeKey = arrivalTimeKey;
	}

	public void setPixelSetKey(String pixelSetKey) {
		this.pixelSetKey = pixelSetKey;
	}

	public String getCogxKey() {
		return cogxKey;
	}

	public void setCogxKey(String cogxKey) {
		this.cogxKey = cogxKey;
	}

	public String getCogyKey() {
		return cogyKey;
	}

	public void setCogyKey(String cogyKey) {
		this.cogyKey = cogyKey;
	}

	public String getDeltaKey() {
		return deltaKey;
	}

	public void setDeltaKey(String deltaKey) {
		this.deltaKey = deltaKey;
	}

	public String getSlopeLongOutputKey() {
		return slopeLongOutputKey;
	}

	public void setSlopeLongOutputKey(String slopeLongOutputKey) {
		this.slopeLongOutputKey = slopeLongOutputKey;
	}

	public String getSlopeTransOutputKey() {
		return slopeTransOutputKey;
	}

	public void setSlopeTransOutputKey(String slopeTransOutputKey) {
		this.slopeTransOutputKey = slopeTransOutputKey;
	}

	public String getSlopeSpreadOutputKey() {
		return slopeSpreadOutputKey;
	}

	public void setSlopeSpreadOutputKey(String slopeSpreadOutputKey) {
		this.slopeSpreadOutputKey = slopeSpreadOutputKey;
	}

	public String getSlopeSpreadWeightedOutputKey() {
		return slopeSpreadWeightedOutputKey;
	}

	public void setSlopeSpreadWeightedOutputKey(String slopeSpreadWeightedOutputKey) {
		this.slopeSpreadWeightedOutputKey = slopeSpreadWeightedOutputKey;
	}

	


}
