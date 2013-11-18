package fact.features;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;


public class SlopeSpreadWeighted implements Processor
{
	@Override
	public Data process(Data input)
	{
	    
	    // load input out of hashmap
		EventUtils.mapContainsKeys(getClass(), input, showerPixel, photonCharge, arrivalTime, hillasDelta);
	    
		showerPixelArray = (int[]) input.get(showerPixel);
		photonChargeArray = (double[]) input.get(photonCharge);
		
		arrivalTimeArray = new double[photonChargeArray.length];
		int[] arrivalPos = (int[]) input.get(arrivalTime);
		for(int i = 0; i < arrivalPos.length; i++)
		{
			arrivalTimeArray[i] = (double) arrivalPos[i];
		}
		hillasDeltaValue = (Double) input.get(hillasDelta);
		
	    // input values
	    int num = showerPixelArray.length;
	    double[] x = new double[num]; // x position rotated by delta
	    double[] y = new double[num]; // y likewise
	    double[] t = new double[num]; // extracted arrival time
	    double[] w = new double[num]; // extracted photoncharge
	    
	    // helper
	    double[] bx = new double[num];
	    double[] bxw = new double[num];
	    
	    // output values
	    double slopeLong = 0;
	    double slopeTrans = 0;
	    double slopeSpread = 0;
	    double slopeSpreadWeighted = 0;
	    double timeSpread = 0;
	    double timeSpreadWeighted = 0;
	    
	    for(int i = 0; i < num; i++)
	    {
	    	int chid = showerPixelArray[i];
	    	double [] rotatedPixel = DefaultPixelMapping.rotate(chid, hillasDeltaValue);
	    	x[i] = rotatedPixel[0];
	    	y[i] = rotatedPixel[1];
	    	t[i] = arrivalTimeArray[chid];
	    	w[i] = photonChargeArray[chid];
	    }
	    try
	    {
	    	slopeLong = ((double) num * arraySum(arrayMultiplication(x, t)) - ( arraySum(t) * arraySum(x) )) / ((double) num * arraySum(arrayMultiplication(x, x)) - arraySum(x)*arraySum(x) );
	    	
	    	for(int i = 0; i < num; i++)
	    	{
	    		bx[i] = t[i] - slopeLong * x[i];
	    		bxw[i] = t[i] - slopeLong * x[i] * w[i];
	    	}
	    	
	    	slopeTrans = ((double) num * arraySum(arrayMultiplication(y, t)) - ( arraySum(t) * arraySum(y) )) / ((double) num * arraySum(arrayMultiplication(y, y)) - arraySum(y)*arraySum(y) );
	    	timeSpread = Math.sqrt(arraySum(arrayMultiplication(t, t)) / (double) num - (arraySum(t) / (double) num) * (arraySum(t) / (double) num));
	    	timeSpreadWeighted = Math.sqrt(arraySum(arrayMultiplication(arrayMultiplication(t, t),w))/arraySum(w) - (arraySum(arrayMultiplication(t, w)) / arraySum(w))*(arraySum(arrayMultiplication(t, w)) / arraySum(w)) );
	    	slopeSpread = Math.sqrt( arraySum(arrayMultiplication(bx, bx)) /(double) num - ( arraySum(bx) / (double) num )*( arraySum(bx) / (double) num ) );
	    	slopeSpreadWeighted = Math.sqrt( arraySum(arrayMultiplication(bxw, bxw)) /(double) num - ( arraySum(bxw) / (double) num )*( arraySum(bxw) / (double) num ) );
	    }
	    catch(Exception e)
	    {
	    	// Error handling here
	    	return input;
	    }
	    
	    input.put(outputKey + "_slopeSpread", slopeSpread);
	    input.put(outputKey + "_slopeSpreadWeighted", slopeSpreadWeighted);	    
	    input.put(outputKey + "_timeSpread", timeSpread);
	    input.put(outputKey + "_timeSpreadWeighted", timeSpreadWeighted);
	    input.put(outputKey + "_slopeTrans", slopeTrans);
	    input.put(outputKey + "_slopeLong", slopeLong);
		return input;
	}

	public String getShowerPixel() {
		return showerPixel;
	}
	
	@Parameter(required = true, defaultValue = "showerPixel", description = "Key of array containing showerpixel chids.")
	public void setShowerPixel(String showerPixel) {
		this.showerPixel = showerPixel;
	}

	public String getArrivalTime() {
		return arrivalTime;
	}
	
	@Parameter(required = true, defaultValue = "arrivalTime", description = "Key of array containing extracted arrivaltimes for each pixel.")
	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getPhotonCharge() {
		return photonCharge;
	}
	
	@Parameter(required = true, defaultValue = "photoncharge", description = "Key of array containing the extracted photoncharge for each pixel.")
	public void setPhotonCharge(String photonCharge) {
		this.photonCharge = photonCharge;
	}

	public String getHillasDelta() {
		return hillasDelta;
	}
	
	@Parameter(required = true, defaultValue = "Hillas_delta", description = "Key of the extracted Hillas_delta angle (Double value).")
	public void setHillasDelta(String hillasDelta) {
		this.hillasDelta = hillasDelta;
	}
	
	public String getOutputKey() {
		return outputKey;
	}
	@Parameter(required = true, defaultValue = "SlopeSpreadWeighted", description = "Key prefix for output values.")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	private String showerPixel;
	private int[] showerPixelArray; // chids
	private String arrivalTime;
	private double[] arrivalTimeArray; // extracted arrivaltimes
	private String photonCharge;
	private double[] photonChargeArray;
	private String hillasDelta;
	private Double hillasDeltaValue;
	
	private String outputKey;
	
	/**
	 * Sum up array
	 * @param a
	 * @return
	 */
	private Double arraySum(double[] a)
	{
		if(a == null)
			return 0.0;
		if(a.length == 0)
			return 0.0;
		
		double ret = 0.0;
		
		for(int i = 0; i < a.length; i++)
		{
			ret += a[i];
		}
		
		return ret;
	}
	
	/**
	 * Elementwise multiplication of arrays
	 * @param a
	 * @param b
	 * @return double array containing a[i] * b[i]
	 * @throws Exception 
	 */
	private double[] arrayMultiplication(double[] a, double[] b) throws Exception
	{
		if (a == null || b == null)
		{
			return null;
		}
		if (a.length != b.length)
		{
			throw new ArrayStoreException("Array sizes do not match.");
		}
		if(a.length == 0)
		{
			throw new ArrayStoreException("Array of length zero.");
		}
		
		double[] ret = new double[a.length];
		
		for(int i = 0; i < ret.length; i++)
		{
			ret[i] = a[i] * b[i];
		}
		
		return ret;
	}
	
}
