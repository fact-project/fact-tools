package fact.cleaning.snake;

import java.awt.geom.Point2D;

import fact.viewer.ui.DefaultPixelMapping;

public abstract class ImageForce 
{
	protected double[] data = null;
	
	// center of shower (precleaning)
	protected Point2D center = null;
	
	// image intensity median
	double median = 1;
	
	protected ImageForce(double[] data, float centerX, float centerY)
	{
		this.center = new Point2D.Float(centerX, centerY); 
		
		this.data = data;
	}
	
	// Gradient in X-Richtung
	// Linearkombination  -> Siehe BScArbeit Dominik Baack
	protected double gradX(int chid)
	{
		int[] neighbor = DefaultPixelMapping.getNeighborsFromChid(chid);		
		for(int i=0; i<6; i++)
		{
			if(neighbor[i] == -1)
				return 0;
		}		
		
		float  bot = (float) data[neighbor[0]];
		float  top = (float) data[neighbor[1]];		
		float leftTop = 0;
		float leftBot = 0;
		float rightTop = 0;
		float rightBot = 0;		
		
		if(DefaultPixelMapping.getGeomX(chid) % 2 == 0)
		{
			leftTop = (float) data[neighbor[2]];
			rightTop = (float) data[neighbor[4]];
			
			leftBot = (float) data[neighbor[3]];
			rightBot = (float) data[neighbor[5]];
		}
		else
		{
			leftTop = (float) data[neighbor[3]];
			rightTop = (float) data[neighbor[5]];
			
			leftBot = (float) data[neighbor[2]];
			rightBot = (float) data[neighbor[4]];
		}
		
		double erg1 = (2.0*rightTop + 1.0*rightBot + 1.0*top) - (2.0*leftBot + 1.0*bot + 1.0*leftTop);
		double erg2 = (2.0*rightBot + 1.0*rightTop + 1.0*bot) - (2.0*leftTop + 1.0*top + 1.0*leftBot);
		
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
		
		float  bot = (float) data[neighbor[0]];
		float  top = (float) data[neighbor[1]];		
		float leftTop = 0;
		float leftBot = 0;
		float rightTop = 0;
		float rightBot = 0;		
		
		if(DefaultPixelMapping.getGeomX(chid) % 2 == 0)
		{
			leftTop = (float) data[neighbor[2]];
			rightTop = (float) data[neighbor[4]];
			
			leftBot = (float) data[neighbor[3]];
			rightBot = (float) data[neighbor[5]];
		}
		else
		{
			leftTop = (float) data[neighbor[3]];
			rightTop = (float) data[neighbor[5]];
			
			leftBot = (float) data[neighbor[2]];
			rightBot = (float) data[neighbor[4]];
		}
		
		double erg = (2*top + 1*leftTop + 1*rightTop) - (2*bot + 1*leftBot + 1*rightBot);		
		
		return -erg;
	}
	
	
	public abstract double forceX(double x, double y);
	public abstract double forceY(double x, double y);
	
	public void setMedian(double med)
	{
		this.median = med;
	}
}

