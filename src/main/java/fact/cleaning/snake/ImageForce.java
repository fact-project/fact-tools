package fact.cleaning.snake;

import java.awt.geom.Point2D;

import fact.viewer.ui.DefaultPixelMapping;

public abstract class ImageForce 
{
	protected double[] data = null;
	
	protected Point2D center = null;
	
	double median = 1;
	
	protected ImageForce(double[] data, float centerX, float centerY)
	{
		this.center = new Point2D.Float(centerX, centerY); 
		
		this.data = data;
	}
	
	protected double gradX(int chid)
	{
		int[] neighbor = DefaultPixelMapping.getNeighborsFromChid(chid);		
		for(int i=0; i<6; i++)
		{
			if(neighbor[i] == -1)
				return 0;
		}
		
		float  b = (float) data[neighbor[0]];
		float  t = (float) data[neighbor[1]];		
		float lt = 0;
		float lb = 0;
		float rt = 0;
		float rb = 0;		
		
		if(DefaultPixelMapping.getGeomX(chid) % 2 == 0)
		{
			lt = (float) data[neighbor[2]];
			rt = (float) data[neighbor[4]];
			
			lb = (float) data[neighbor[3]];
			rb = (float) data[neighbor[5]];
		}
		else
		{
			lt = (float) data[neighbor[3]];
			rt = (float) data[neighbor[5]];
			
			lb = (float) data[neighbor[2]];
			rb = (float) data[neighbor[4]];
		}
		
		double erg1 = (2.0*rt + 1.0*rb + 1.0*t) - (2.0*lb + 1.0*b + 1.0*lt);
		double erg2 = (2.0*rb + 1.0*rt + 1.0*b) - (2.0*lt + 1.0*t + 1.0*lb);
		
		return (erg1 + erg2)/2.0;
	}
	
	protected double gradY(int chid)
	{		
		int[] neighbor = DefaultPixelMapping.getNeighborsFromChid(chid);		
		for(int i=0; i<6; i++)
		{
			if(neighbor[i] == -1)
				return 0;
		}
		
		float  b = (float) data[neighbor[0]];
		float  t = (float) data[neighbor[1]];		
		float lt = 0;
		float lb = 0;
		float rt = 0;
		float rb = 0;		
		
		if(DefaultPixelMapping.getGeomX(chid) % 2 == 0)
		{
			lt = (float) data[neighbor[2]];
			rt = (float) data[neighbor[4]];
			
			lb = (float) data[neighbor[3]];
			rb = (float) data[neighbor[5]];
		}
		else
		{
			lt = (float) data[neighbor[3]];
			rt = (float) data[neighbor[5]];
			
			lb = (float) data[neighbor[2]];
			rb = (float) data[neighbor[4]];
		}
		
		double erg = (2*t + 1*lt + 1*rt) - (2*b + 1*lb + 1*rb);		
		
		return erg;
	}
	
	
	public abstract double forceX(double x, double y);
	public abstract double forceY(double x, double y);
	
	public void setMedian(double med)
	{
		this.median = med;
	}
}

