package fact.cleaning.snake;


import java.util.Arrays;

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
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.EventUtils;


import fact.cleaning.snake.ImageForce;
import fact.cleaning.snake.StdForce;

public class snakeFrame implements StatefulProcessor
{
	private static Logger log = LoggerFactory.getLogger(CoreNeighborClean.class);
	
	private double[] pixelArrayData = null;
	
	private RealMatrix[] matrix;
	
	private String pixelDataName = null;
	private String startPointName = null;
	
	private double alpha = 0.3;
	private double beta = 0.3;
	private double dt = 0.05;
	private double ds2 = 1.0;
	
	private RealMatrix vecX;
	private RealMatrix vecY;
	
	private int NumberOfVertices = 6;
	
	
	private double[] centerX = null;
	private double[] centerY = null;
	
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
			double x = vecX.getEntry(i,0);
			double y = vecX.getEntry(i,0);			

			vecX.setEntry(i, 0, x + (dt * f.forceX(x, y)) );
			vecY.setEntry(i, 0, y + (dt * f.forceY(x, y)) );		
		}		

		RealMatrix EigenMat = matrix[NumberOfVertices-1];

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
			pixelArrayData= (double[]) input.get(pixelDataName);
			if(pixelArrayData == null)
			{
				log.error("No weights found in event. Aborting.");
				throw new RuntimeException("No weights found in event. Aborting.");
			}
			
			centerX = (double[]) input.get(startPointName + "_X");
			centerY = (double[]) input.get(startPointName + "_Y");
		} 
		catch(ClassCastException e)
		{
			log.error("Could cast the key: " + pixelDataName + "to a double[]");
		}		
		
		for(int frame = 0; frame <300; frame++)
		{
			for (int i = 0; i < 6; i++)
			{
				float a = (float) (centerX[frame] + 2.0 * Math.sin(i*3.1415 / 3.0));
				float b = (float) (centerY[frame] + 2.0 * Math.cos(i*3.1415 / 3.0));

				vecX.setEntry(i, 0, a);
				vecY.setEntry(i, 0, b);
			}		
			
			double[] data = Arrays.copyOfRange(pixelArrayData, Constants.NUMBEROFPIXEL * frame, Constants.NUMBEROFPIXEL * frame + Constants.NUMBEROFPIXEL);
			ImageForce force = new StdForce(data, (float) centerX[frame], (float) centerY[frame]);		
		
			for(int i=0; i<200; i++)
			{
				step(force);
			}
			
			input.put("snake_X_"+frame, vecX.getRow(0));
			input.put("snake_Y_"+frame, vecY.getRow(0));
		}
		
		System.out.println("Snake Beendet für Event: " + input.get("EventNum"));
		
		return input;
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

	public String getStartPoint() 
	{
		return startPointName;
	}

	public void setStartPoint(String startPoint) 
	{
		this.startPointName = startPoint;
	}	
	
}
