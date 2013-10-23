package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.viewer.ui.DefaultPixelMapping;


public class SlopeSpreadWeighted implements Processor
{

	public Data process(Data input)
	{
	    mpGeomXCoord =  DefaultPixelMapping.getGeomXArray();
	    mpGeomYCoord =  DefaultPixelMapping.getGeomYArray();
	    
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
	    
		return input;
	}

	private float[] mpGeomXCoord;
	private float[] mpGeomYCoord;
	
	private String showerPixel;
	private int[] showerPixelArray; // chids
	private String arrivalTime;
	private double[] arrivalTimeArray; // extracted arrivaltimes
	private String photonCharge;
	private double[] photonChargeArray;
	private String hillasDelta;
	private Double hillasDeltaValue;
	
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
