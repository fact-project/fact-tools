package fact.utils;

import java.util.Arrays;

/**
 * This class does an linear interpolation of input points.
 * @author jan, kai, max
 *
 */
public class LinearTimeCorrectionKernel implements TimeCorrectionKernel {
	
	private int numPoints = 0;
	private double[] times = null;
	private double[] values = null;
	
	@Override
	public void fit(double[] realTime, double[] value) {
		numPoints = realTime.length;
		times = realTime;
		values = value;
	}

	/**
	 * This calculates a linear interpolation of $[t,v]_i$ for all $i$.
	 */
	@Override
	public double interpolate(double t) {
		
		int id = getIndex(t);
		if (id < numPoints - 1)
		{
			// check left border
			if(id == 0 && times[0] > t)
				return values[0];
			
			double t0 = times[id];
			double t1 = times[id + 1];
			double s = (t - t0) / (t1 - t0); // interpolation in "percent"
		
			double v0 = values[id];
			double v1 = values[id + 1];
			
			return (v1 - v0) * s + v0;
		}else // check right border
		{
			return values[numPoints - 1];
		}
		
	}
	
	/**
	 * This function return the id of the entry with smaller t. Since the values in the times array
     * are sorted in ascending order we can use a binary search to find the right index in the array.
	 * @param t the value to get the index for.
	 * @return the index of the times array.
	 */
	private int getIndex(double t) {
		int pos = Arrays.binarySearch(times, t);
		if (pos >= 0){
			return pos;
		}else {
			return - (pos + 1);
		}
	}




}
