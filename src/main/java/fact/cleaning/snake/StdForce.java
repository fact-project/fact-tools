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

		double grad = gradX(chid);

		// Externe Kraft zur mitte	-> Domsche Rücktreib Kraft!				

		double exForce = data[chid] - (median * 3.0);
		exForce = exForce * ((center.getX() - x) < 0 ? 1.0 : -2.8);


		double winkel = Math.atan((y - center.getY()) / (x - center.getX()));

		double sinValue = Math.sin(winkel);
		exForce = exForce * (sinValue>0.0 ? sinValue : (-sinValue));
		//				

		return (grad / 410.0) + (exForce / 80.0);		
	}

	@Override
	public double forceY(double x, double y) 
	{
		int chid = DefaultPixelMapping.geomToChid((float) x, (float) y );		
		if (chid == -1) return 0;

		double grad = gradY(chid);

		// Externe Kraft zur mitte

		double exForce = data[chid] - (median * 3.0);
		exForce = exForce * ((center.getY() - y) < 0 ? 1.0 : -2.8);

		double winkel = Math.atan((y - center.getY()) / (x - center.getX()));

		double sinValue = Math.sin(winkel);
		exForce = exForce * (sinValue>0.0 ? sinValue : (-sinValue));
		//				

		return (grad / 410.0) + (exForce / 80.0);
	}

}
