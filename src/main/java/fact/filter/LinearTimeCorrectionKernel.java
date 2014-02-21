package fact.filter;

public class LinearTimeCorrectionKernel implements TimeCorrectionKernel {

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
		if (id < times.length)
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
			return values[times.length - 1];
		}
		
	}
	
	/**
	 * This function return the id of the entry with smaller t.
	 * @param t
	 * @return
	 */
	private int getIndex(double t) {
		int id = 0;
		while(id < numPoints){
			if(times[id] > t)
				break;
			id++;
		}
		return id;
	}
	
	private int numPoints = 0;
	private double[] times = null;
	private double[] values = null;
	
}
