package fact.cleaning.snake;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class Snake 
{	
	final int _MAX_VERTICES = 50;
	
	RealMatrix[] matrix;	
	
	private double alpha = 0.10;
	private double beta = 0.03;
	private double dt = 0.1;
	private double ds2 = 1.0;
	
	private RealMatrix vecX;
	private RealMatrix vecY;
	
	private int NumberOfVertices = 6;
	
	
	Snake()
	{
			
	}
	
	public void initMatrix()
	{
		matrix = new Array2DRowRealMatrix[_MAX_VERTICES];		
		
		for(int i=0; i < _MAX_VERTICES; i++)
		{
			matrix[i] = new Array2DRowRealMatrix(i+1, i+1);			
			calcMatrix(i+1);
		}	
	}
	
	public void initStartPos(double centerX, double centerY, double radius, int startPointNumber)
	{
		NumberOfVertices = startPointNumber;
		vecX = new Array2DRowRealMatrix(startPointNumber,1);
		vecY = new Array2DRowRealMatrix(startPointNumber,1);
		
		for (int i = 0; i < startPointNumber; i++)
		{
			final double div = startPointNumber / 2.0;
			
			float a = (float) (centerX + radius * Math.sin(i*3.1415 / div));
			float b = (float) (centerY + radius * Math.cos(i*3.1415 / div));

			vecX.setEntry(i, 0, a);
			vecY.setEntry(i, 0, b);
		}	
	}
	
	protected void step(ImageForce f)
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

		splitLines(220.0);	// 2 Pixel lang
		
	}
	

	protected void calcMatrix(int count)
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
	
	protected void splitLines(double maxDist)
	{
		if(NumberOfVertices >= (_MAX_VERTICES))
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
	
	public double getAlpha() 
	{
		return alpha;
	}

	public void setAlpha(double alpha) 
	{
		this.alpha = alpha;
	}

	public double getBeta() 
	{
		return beta;
	}

	public void setBeta(double beta) 
	{
		this.beta = beta;
	}

	public double getDt() 
	{
		return dt;
	}

	public void setDt(double dt) 
	{
		this.dt = dt;
	}

	public double getDs2() 
	{
		return ds2;
	}

	public void setDs2(double ds2) 
	{
		this.ds2 = ds2;
	}
	
	public double[] getSnakeX()
	{
		return vecX.getColumn(0);
	}
	
	public double[] getSnakeY()
	{
		return vecY.getColumn(0);
	}
	
	public int getNumberOfVertices()
	{
		return NumberOfVertices;
	}
}
