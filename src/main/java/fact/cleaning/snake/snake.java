package fact.cleaning.snake;


import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.EventUtils;
import fact.cleaning.CoreNeighborClean;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;
import fact.viewer.ui.DefaultPixelMapping;




import org.apache.commons.math3.linear.Array2DRowRealMatrix;
// import org.apache.commons.math3.linear.ArrayRealVector;
// import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
//import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.EventUtils;


import fact.cleaning.snake.ImageForce;
import fact.cleaning.snake.StdForce;

public class snake implements StatefulProcessor
{
	private static Logger log = LoggerFactory.getLogger(CoreNeighborClean.class);
	
	private double[] photonCharge = new double[Constants.NUMBEROFPIXEL];
	
	private RealMatrix[] matrix;
	
	private String pixelDataName = null;
	private String startPointNameX = null;
	private String startPointNameY = null;
	
	private double alpha = 0.3;
	private double beta = 0.3;
	private double dt = 0.05;
	private double ds2 = 1.0;
	
	private RealMatrix vecX;
	private RealMatrix vecY;
	
	private int NumberOfVertices = 6;
	
	
	private float centerX = 0;
	private float centerY = 0;
	
	//////////////////////////////////////////////////////////////////////////////
	
	private void calcMatrix(int count)
	{
		final double a = alpha * dt / ds2;
		final double b = beta * dt / ds2;

		final double r = 1.0 + 2.0 * a + 6.0*b;
		final double p = b;
		final double q = -a - 4.0*b;						
			
		for(int i=0; i < count; i++)
		{			
			matrix[count-1].setEntry(i, i, r);
			
			matrix[count-1].setEntry((i + 1) % count, i, q);
			matrix[count-1].setEntry((i + 2) % count, i, p);

			matrix[count-1].setEntry(((i - 1) + count) % count, i, q);
			matrix[count-1].setEntry(((i - 2) + count) % count, i, p);			
		}
			
		matrix[count-1] = new LUDecomposition(matrix[count-1]).getSolver().getInverse();
	}
	
	private void splitLines(double maxDist)
	{
		if(NumberOfVertices > 40)
		{
			return;
		}

		for(int i=0; i<NumberOfVertices; i++)
		{
			double distX = vecX.getEntry((i+1) % NumberOfVertices, 0) - vecX.getEntry(i, 0);
			double distY = vecY.getEntry((i+1) % NumberOfVertices, 0) - vecY.getEntry(i, 0);

			if(distX*distX + distY*distY > maxDist)
			{
				NumberOfVertices++;
				RealMatrix newVecX = new Array2DRowRealMatrix(NumberOfVertices,1);
				RealMatrix newVecY = new Array2DRowRealMatrix(NumberOfVertices,1);				

				for(int j=0; j<=i; j++)
				{
					newVecX.setEntry(j, 0, vecX.getEntry(j, 0));
					newVecY.setEntry(j, 0, vecY.getEntry(j, 0));					
				}

				newVecX.setEntry(i+1, 0, vecX.getEntry(i, 0) + (distX/2.0));				
				newVecY.setEntry(i+1, 0, vecY.getEntry(i, 0) + (distY/2.0));
			

				for(int j = i+1; j < (NumberOfVertices-1); j++)
				{
					newVecX.setEntry(j+1, 0, vecX.getEntry(j, 0));					
					newVecY.setEntry(j+1, 0, vecY.getEntry(j, 0));					
				}

				vecX = newVecX;
				vecY = newVecY;					
			}
		}
	}
	
	private void step(ImageForce f)
	{		
		for(int i=0; i<NumberOfVertices; i++)
		{	
			double x = vecX.getEntry(0,i);
			double y = vecX.getEntry(0,i);			

			vecX.setEntry(i, 0, x + (dt * f.forceX(x, y)) );
			vecY.setEntry(i, 0, y + (dt * f.forceY(x, y)) );		
		}		

		RealMatrix EigenMat = matrix[NumberOfVertices];

		vecX = EigenMat.multiply(vecX);
		vecY = EigenMat.multiply(vecY);

		splitLines(4.0);
		
	}
	//////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void init(ProcessContext context) throws Exception 
	{
		matrix = new Array2DRowRealMatrix[50];
		vecX = new Array2DRowRealMatrix(6,1);
		vecY = new Array2DRowRealMatrix(6,1);
		
		for(int i=0; i < 50; i++)
		{
			matrix[i] = new Array2DRowRealMatrix(i+1, i+1);			
			calcMatrix(i+1);
		}		
	}
	
	@Override
	public Data process(Data input) 
	{
		try
		{			
			EventUtils.mapContainsKeys(getClass(), input, pixelDataName);
			photonCharge= (double[]) input.get(pixelDataName);
			if(photonCharge == null)
			{
				log.error("No weights found in event. Aborting.");
				throw new RuntimeException("No weights found in event. Aborting.");
			}
			
			centerX = (Float) input.get(startPointNameX);
			centerY = (Float) input.get(startPointNameY);
		} 
		catch(ClassCastException e)
		{
			log.error("Could cast the key: " + pixelDataName + "to a double[]");
		}		
		
		ImageForce force = new StdForce(photonCharge,centerX,centerY);
		
		
		for(int i=0; i<200; i++)
		{
			step(force);
		}
		
		return null;
	}

	@Override
	public void finish() throws Exception 
	{
		// TODO Auto-generated method stub	
	}	

	@Override
	public void resetState() throws Exception 
	{
	// TODO Auto-generated method stub	
	}
	
	///////////////////////////////////////////////////////////////////////////////////

	public String getPixelDataName() 
	{
		return pixelDataName;
	}

	public void setPixelDataName(String pixelDataName) 
	{
		this.pixelDataName = pixelDataName;
	}

	public String getStartPointX() 
	{
		return startPointNameX;
	}

	public void setStartPointX(String startPoint) 
	{
		this.startPointNameX = startPoint;
	}
	
	public String getStartPointY() 
	{
		return startPointNameY;
	}

	public void setStartPointY(String startPoint) 
	{
		this.startPointNameY = startPoint;
	}
	
}
