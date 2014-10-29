package fact.cleaning.snake;

import java.awt.geom.Point2D;

import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;;


/**
 *	ImageForce
 *	Interface fuer alle Snake-Kraefte
 *	Stellt gradienten Implementation zur Verfuegung
 *
 *  @author Dominik Baack &lt;dominik.baack@udo.edu&gt;
 *
 */
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
	
	protected double gradY(int chid)
	{
		FactPixelMapping PixelMapping_  = FactPixelMapping.getInstance();
		
		FactCameraPixel currentPixel = PixelMapping_.getPixelFromId(chid);
		// (up,down,topleft,topright,botleft,botright)
		FactCameraPixel[] neighbor = PixelMapping_.getNeighboursForPixelWithDirection(currentPixel);				
		
		for(int i=0; i<6; i++)
		{
			if(neighbor[i] == null)
				return 0;
		}		
		
		float  right = (float) data[neighbor[0].chid];
		float  left = (float) data[neighbor[1].chid];		
		float  botRight = (float) data[neighbor[2].chid];	
		float  topRight = (float) data[neighbor[3].chid];	
		float  botLeft = (float) data[neighbor[4].chid];	
		float  topLeft = (float) data[neighbor[5].chid];	
		
		double erg = (2*right + 1*botRight + 1*topRight) - (2*left + 1*botLeft + 1*topLeft);
		
		
		return erg;//
	}
	
	// Gradient in X-Richtung
	// Linearkombination  -> Siehe BScArbeit Dominik Baack
	protected double gradX(int chid)
	{		
		FactPixelMapping PixelMapping_  = FactPixelMapping.getInstance();
		
		FactCameraPixel currentPixel = PixelMapping_.getPixelFromId(chid);
		FactCameraPixel[] neighbor = PixelMapping_.getNeighboursForPixelWithDirection(currentPixel);				
		
		for(int i=0; i<6; i++)
		{
			if(neighbor[i] == null)
				return 0;
		}		
		
		float  right = (float) data[neighbor[0].chid];
		float  left = (float) data[neighbor[1].chid];		
		float  botRight = (float) data[neighbor[2].chid];	
		float  topRight = (float) data[neighbor[3].chid];	
		float  botLeft = (float) data[neighbor[4].chid];	
		float  topLeft = (float) data[neighbor[5].chid];
		
		double erg1 = (2.0*topRight + 1.0*topLeft + 1.0*right) - (2.0*botLeft + 1.0*left + 1.0*botRight);
		double erg2 = (2.0*topLeft + 1.0*topRight + 1.0*left) - (2.0*botRight + 1.0*right + 1.0*botLeft);		
		
		return (erg1 + erg2)/2.0;		
	}
	
	
	public abstract double forceX(double x, double y);
	public abstract double forceY(double x, double y);
	
	public void setMedian(double med)
	{
		this.median = med;
	}
}
