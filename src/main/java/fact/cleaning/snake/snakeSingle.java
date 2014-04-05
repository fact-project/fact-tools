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
import fact.statistics.PixelDistribution2D;
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

public class snakeSingle implements StatefulProcessor
{
	private static Logger log = LoggerFactory.getLogger(CoreNeighborClean.class);
	
	private double[] photonCharge = new double[Constants.NUMBEROFPIXEL];
	
	private RealMatrix[] matrix;
	
	private String pixelDataName = null;
	private String distribution = null;	
	private String showerCenterX = null;
	private String showerCenterY = null;
	private String mean = null;

	

	private double alpha = 0.08;
	private double beta = 0.08;
	private double dt = 0.05;
	private double ds2 = 1.0;
	
	private RealMatrix vecX;
	private RealMatrix vecY;
	
	private int NumberOfVertices = 6;
	
	
	private double centerX = 0;
	private double centerY = 0;
	
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
			double y = vecY.getEntry(i,0);			

			vecX.setEntry(i, 0, x + (dt * f.forceX(x, y)) );
			vecY.setEntry(i, 0, y + (dt * f.forceY(x, y)) );		
		}		

		RealMatrix EigenMat = matrix[NumberOfVertices-1];

		vecX = EigenMat.multiply(vecX);
		vecY = EigenMat.multiply(vecY);

		splitLines(361.0);	// 2 Pixel lang
		
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
			EventUtils.mapContainsKeys(getClass(), input, pixelDataName, mean);
			photonCharge= (double[]) input.get(pixelDataName);
			if(photonCharge == null)
			{
				log.error("No weights found in event. Aborting.");
				throw new RuntimeException("No weights found in event. Aborting.");
			}
			
			if(distribution != null)
			{
				PixelDistribution2D dist;
				dist = (PixelDistribution2D) input.get(distribution);
			
				centerX = dist.getCenterX();
				centerY = dist.getCenterY();
			}
			else if(showerCenterX != null && showerCenterY != null)
			{
				centerX = (Double) input.get(showerCenterX);
				centerY = (Double) input.get(showerCenterY);
			}
			else
			{
				throw new RuntimeException("No center parameter set!");
			}
			
				
		} 
		catch(ClassCastException e)
		{
			log.error("CastError");
		}				
		
		NumberOfVertices = 6;
		vecX = new Array2DRowRealMatrix(6,1);
		vecY = new Array2DRowRealMatrix(6,1);
		
		for (int i = 0; i < 6; i++)
		{
			float a = (float) (centerX + 15.0 * Math.sin(i*3.1415 / 3.0));
			float b = (float) (centerY + 15.0 * Math.cos(i*3.1415 / 3.0));

			vecX.setEntry(i, 0, a);
			vecY.setEntry(i, 0, b);
		}						
		
		double[] data = new double[Constants.NUMBEROFPIXEL];
		for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
		{
			data[i] = 10 * photonCharge[i];
		}
		
		ImageForce force = new StdForce(data, (float) centerX, (float) centerY);	
		force.setMedian((Double) input.get(mean));
		
		System.out.println(force.median);
	
		double[][] xBuf = new double[200][];
		double[][] yBuf = new double[200][];
		
		for(int i=0; i<200; i++)
		{			
			step(force);
			xBuf[i] = vecX.getColumn(0);
			yBuf[i] = vecY.getColumn(0);
		}		
		
		input.put("snake_X_Prog", xBuf);
		input.put("snake_Y_Prog", yBuf);
		
		input.put("snake_X", vecX.getColumn(0));
		input.put("snake_Y", vecY.getColumn(0));
		
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
	
	public String getDistribution() {
		return distribution;
	}

	public void setDistribution(String distribution) {
		this.distribution = distribution;
	}
	
	public String getMean() {
		return mean;
	}

	public void setMean(String mean) {
		this.mean = mean;
	}

	public String getShowerCenterX() {
		return showerCenterX;
	}

	public void setShowerCenterX(String showerCenterX) {
		this.showerCenterX = showerCenterX;
	}

	public String getShowerCenterY() {
		return showerCenterY;
	}

	public void setShowerCenterY(String showerCenterY) {
		this.showerCenterY = showerCenterY;
	}
	
	
	
}
