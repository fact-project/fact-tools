package fact.cleaning.snake;


import fact.viewer.ui.DefaultPixelMapping;

public class StdForce extends ImageForce 
{
	
	public StdForce(double [] data, float x, float y) 
	{
		super(data, x, y);
	}
	
	
	public StdForce(double[] data, double centerX, double centerY) 
	{
		super(data, (float) centerX, (float) centerY);
	}


	@Override
	public double forceX(double x, double y) 
	{
		int chid = DefaultPixelMapping.geomToChid((float) x, (float) y);			
		
		if (chid == -1) return 0;

		double grad = -gradX(chid);

		// Externe Kraft zur mitte		
		
		double exForce = data[chid] - (median * 16.0);	// Data wurde vorher verzehnfacht!
		exForce = exForce * ((center.getX() - x) < 0 ? 1.0 : -2.0);

		double skal = 10.0 / Math.abs(center.getX() - x);		
		//exForce = skal * exForce;

		//double winkel = Math.atan((y - center.getY()) / (x - center.getX()));

		//double sinValue = Math.sin(winkel);
		//exForce = exForce * (sinValue>0.0 ? sinValue : (-sinValue));
				

		return (grad / 180.0) + (exForce / 69.0);		
	}

	@Override
	public double forceY(double x, double y) 
	{
		int chid = DefaultPixelMapping.geomToChid((float) x, (float) y );		
		if (chid == -1) return 0;

		double grad = gradY(chid);

		// Externe Kraft zur mitte

		double exForce = data[chid] - (median * 16.0);
		exForce = exForce * ((center.getY() - y) < 0 ? 1.0 : -2.0);
		
		double skal = 10.0 / Math.abs(center.getX() - x);		
		//exForce = skal * exForce;

		double winkel = Math.atan((y - center.getY()) / (x - center.getX()));

		double sinValue = Math.sin(winkel);
		exForce = exForce * (sinValue>0.0 ? sinValue : (-sinValue));
		//				

		return (grad / 180.0) + (exForce / 69.0);
	}

}
