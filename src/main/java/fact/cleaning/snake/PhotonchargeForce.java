package fact.cleaning.snake;


import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;

/**
 *	StdForce
 *	Implementiert ImageForce
 *	Berechnet die Kraft auf ein einzelnes Vertice der Snake
 *	Input: Einzelnes Bild und ein approximierter Schauermittelpunkt
 * 	Output: Kartesischer Kraftvektor fuer den Vertex
 *
 *  @author Dominik Baack &lt;dominik.baack@udo.edu&gt;
 *
 */
public class PhotonchargeForce extends ImageForce 
{
	
	public PhotonchargeForce(double [] data, float x, float y) 
	{
		super(data, x, y);
	}
	
	
	public PhotonchargeForce(double[] data, double centerX, double centerY) 
	{
		super(data, (float) centerX, (float) centerY);
	}


	@Override
	public double forceX(double x, double y) 
	{
		FactPixelMapping PixelMapping_  = FactPixelMapping.getInstance();		
		FactCameraPixel currentPixel = PixelMapping_.getPixelBelowCoordinatesInMM((float) x, (float) y);
		
		if (currentPixel == null) return 0;
		
		int chid = currentPixel.chid;			

		double grad = -gradX(chid);		

		// Externe Kraft zur mitte		
		
		double exForce = data[chid] - (median * 1.20);	// Kraft bestimmen (ungerichtet)
		exForce = exForce * ((center.getX() < x) ? +1.0 : -1.0);	// Richtung bestimmen (-links, +rechts)
		
				
		double erg = (grad / 40.0) + (exForce / 4.50);			
		return erg;	
	}

	@Override
	public double forceY(double x, double y) 
	{

		FactPixelMapping PixelMapping_  = FactPixelMapping.getInstance();		
		FactCameraPixel currentPixel = PixelMapping_.getPixelBelowCoordinatesInMM((float) x, (float) y);
		
		if (currentPixel == null) return 0;
		
		int chid = currentPixel.chid;		

		double grad = -gradY(chid);		

		// Externe Kraft zur mitte

		double exForce = data[chid] - (median * 1.20);	// Kraft bestimmen (ungerichtet)			
		exForce = exForce * ((center.getY() < y) ? +1.0 : -1.0);	// Richtung bestimmen (-links, +rechts)			

		double erg = (grad / 40.0) + (exForce / 4.50);		
		return erg;	
	}

}
