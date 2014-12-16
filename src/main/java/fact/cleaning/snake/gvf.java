package fact.cleaning.snake;

import stream.Data;
import stream.Processor;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.VectorFieldOverlay;


/* Gradient Vector Field
 * 
 * Interpolate gradient on every possible position
 */
public class gvf extends ImageForce implements Processor
{
	protected gvf(double[] data, float centerX, float centerY) 
	{
		super(data, centerX, centerY);
	}
	
	public gvf()
	{
		super(null, 0, 0);
	}

	
	public double getVecFieldX(double x, double y)
	{
		
		FactCameraPixel currentPixel = PixelMapping_.getPixelBelowCoordinatesInMM((float) x, (float) y);
				// (up,down,topleft,topright,botleft,botright)
		if(currentPixel == null) return 0;
		
		FactCameraPixel[] neighbor = PixelMapping_.getNeighborsForPixelWithDirection(currentPixel);
		
		
		
		double l1 = 0, l2 = 0, l3 = 0;
		double Area = 0;
		
		FactCameraPixel pix1 = currentPixel;
		FactCameraPixel pix2 = null, pix3 = null;
		
		for(int i=0; i<6; i++)
		{
			final double vec1X = currentPixel.getXPositionInMM();
			final double vec1Y = currentPixel.getYPositionInMM();
			
			double vec2X=0, vec2Y = 0, vec3X = 0, vec3Y = 0;
			
			switch(i)
			{
			case 0:				
				pix2 = neighbor[3];
				pix3 = neighbor[0];
				break;
			case 1:
				pix2 = neighbor[5];
				pix3 = neighbor[3];
				break;
			case 2:
				pix2 = neighbor[1];
				pix3 = neighbor[5];
				break;
			case 3: 
				pix2 = neighbor[4];
				pix3 = neighbor[1];
				break;
			case 4:				
				pix2 = neighbor[2];
				pix3 = neighbor[4];
				break;
			case 5:
				pix2 = neighbor[0];
				pix3 = neighbor[2];
				break;
			}
			if(pix2 == null || pix3 == null) return 0;
			
			vec2X = pix2.getXPositionInMM();
			vec2Y = pix2.getYPositionInMM();
			vec3X = pix3.getXPositionInMM();
			vec3Y = pix3.getYPositionInMM();
			
			Area = (vec2Y - vec3Y)*(vec1X - vec3X) + (vec3X - vec2X)*(vec1Y - vec3Y);
			
			l1 = ((vec2Y - vec3Y)*(x - vec3X) + (vec3X - vec2X)*(y - vec3Y)) / Area;
			l2 = ((vec3Y - vec1Y)*(x - vec3X) + (vec1X - vec3X)*(y - vec3Y)) / Area;	
			
			l3 = 1 - l1 - l2;
			
			if(l1 > 1 || l2 > 1 || l3 > 1)
			{
				continue;
			}
			else
			{
				return l1*gradX(pix1.chid) + l2*gradX(pix2.chid) + l3*gradX(pix3.chid);
			}
		}		
				
		return 0;		
	}
	
	public double getVecFieldY(double x, double y)
	{
		
		FactCameraPixel currentPixel = PixelMapping_.getPixelBelowCoordinatesInMM((float) x, (float) y);
		if(currentPixel == null) return 0;
		
		// (up,down,topleft,topright,botleft,botright)
		FactCameraPixel[] neighbor = PixelMapping_.getNeighborsForPixelWithDirection(currentPixel);
		
		
		
		double l1 = 0, l2 = 0, l3 = 0;
		double Area = 0;
		
		FactCameraPixel pix1 = currentPixel;
		FactCameraPixel pix2 = null, pix3 = null;
		
		for(int i=0; i<6; i++)
		{
			final double vec1X = currentPixel.getXPositionInMM();
			final double vec1Y = currentPixel.getYPositionInMM();
			
			double vec2X=0, vec2Y = 0, vec3X = 0, vec3Y = 0;
			
			switch(i)
			{
			case 0:				
				pix2 = neighbor[0];
				pix3 = neighbor[3];
				break;
			case 1:
				pix2 = neighbor[3];
				pix3 = neighbor[5];
				break;
			case 2:
				pix2 = neighbor[5];
				pix3 = neighbor[1];
				break;
			case 3: 
				pix2 = neighbor[1];
				pix3 = neighbor[4];
				break;
			case 4:				
				pix2 = neighbor[4];
				pix3 = neighbor[2];
				break;
			case 5:
				pix2 = neighbor[2];
				pix3 = neighbor[0];
				break;
			}
			if(pix2 == null || pix3 == null) return 0;
			
			
			vec2X = pix2.getXPositionInMM();
			vec2Y = pix2.getYPositionInMM();
			vec3X = pix3.getXPositionInMM();
			vec3Y = pix3.getYPositionInMM();
			
			Area = (vec2Y - vec3Y)*(vec1X - vec3X) + (vec3X - vec2X)*(vec1Y - vec3Y);
			
			l1 = ((vec2Y - vec3Y)*(x - vec3X) + (vec3X - vec2X)*(y - vec3Y)) / Area;
			l2 = ((vec3Y - vec1Y)*(x - vec3X) + (vec1X - vec3X)*(y - vec3Y)) / Area;	
			
			l3 = 1 - l1 - l2;
			
			if(l1 > 1 || l2 > 1 || l3 > 1)
			{
				continue;
			}
			else
			{
				return l1*gradY(pix1.chid) + l2*gradY(pix2.chid) + l3*gradY(pix3.chid);
			}
		}		
				
		return 0;		
	}



	@Override
	public double forceX(double x, double y) 
	{		
		return 0;
	}



	@Override
	public double forceY(double x, double y) 
	{
		return 0;
	}


	
	
	@Override
	public Data process(Data input) 
	{
		this.data = (double[]) input.get("photoncharge");
				
		VectorFieldOverlay vecField = new VectorFieldOverlay();
		
		double[][] arr = new double[5000][4];
		
		for(int i=0; i<5000; i++)
		{			
			arr[i][0] = (Math.random()-0.5)*350.0;
			arr[i][1] = (Math.random()-0.5)*350.0;
			arr[i][2] = getVecFieldX(arr[i][0], arr[i][1]);
			arr[i][3] = getVecFieldY(arr[i][0], arr[i][1]);	
			
		}
		
		vecField.setArrows(arr);
		
		input.put("TestOverlay", vecField);
		return input;
	}
	
	
}
