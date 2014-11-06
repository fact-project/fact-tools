package fact.cleaning.snake;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;

import stream.annotations.Parameter;

/**
 *	Snake
 *	Interface fuer alle Snake Arten
 *	Stellt grundlegende unabhaengige Implementationen fuer die Snake bereit
 *	
 * 
 *
 *  @author Dominik Baack &lt;dominik.baack@udo.edu&gt;
 *
 */
public class Snake 
{	
	private final int _MAX_VERTICES = 55;
	
	protected final DoubleMatrix[] matrix = new DoubleMatrix[_MAX_VERTICES];	
	
	//private double alpha = 0.10;
	//private double beta = 0.06;
	@Parameter(required = false, description = "Constant: Alpha ")
	private double alpha = 0.03;	
	@Parameter(required = false, description = "Constant: Alpha ")
	private double beta = 0.01;
	@Parameter(required = false, description = "Constant: dz ")
	private double dt = 0.08;
	@Parameter(required = false, description = "Constant: ds2 ")
	private double ds2 = 1.0;
	
	private DoubleMatrix vecX;
	private DoubleMatrix vecY;
	
	
	private int NumberOfVertices = 6;
	
	
	Snake()
	{			
	}
	
	public void initConstant(double alpha, double beta, double dt, double ds2)
	{
		this.alpha = alpha;
		this.beta = beta;
		this.dt = dt;
		this.ds2 = ds2;
	}
	
	public void initMatrix()
	{		
		for(int i=0; i < _MAX_VERTICES; i++)
		{
			matrix[i] = DoubleMatrix.zeros(i+1, i+1);				
			calcMatrix(i+1);
		}	
	}
	
	public void initStartPos(double centerX, double centerY, double radius, int startPointNumber)
	{
		NumberOfVertices = startPointNumber;
		vecX = new DoubleMatrix(startPointNumber,1);
		vecY = new DoubleMatrix(startPointNumber,1);
		
		for (int i = 0; i < startPointNumber; i++)
		{
			final double div = startPointNumber / 2.0;
			
			float a = (float) (centerX + radius * Math.sin(i*3.1415 / div));
			float b = (float) (centerY + radius * Math.cos(i*3.1415 / div));

			vecX.data[i] = a;
			vecY.data[i] = b;
		}	
	}
	
	protected void step(ImageForce f)
	{		
		for(int i=0; i<NumberOfVertices; i++)
		{	
			final double x = vecX.data[i];
			final double y = vecY.data[i];			

			
			final double dxErg = dt * f.forceX(x, y);
			final double dyErg = dt * f.forceY(x, y);
			
			double dx = dxErg;
			double dy = dyErg;		
			
			while((dx*dx + dy*dy) > Math.max(dxErg*dxErg,dyErg*dyErg) )
			{
				dx = dx * 0.95;
				dy = dy * 0.95;
			}
			
			vecX.data[i] = x + dx;
			vecY.data[i] = y + dy;		
		}		

		
		final DoubleMatrix EigenMat = matrix[NumberOfVertices-1];
			
		vecX = EigenMat.mmul(vecX);
		vecY = EigenMat.mmul(vecY);
				

		splitLines(220.0);	// 2 Pixel lang
		
	}
	

	protected void calcMatrix(int count)
	{
		final double a = alpha * dt / ds2;
		final double b = beta * dt / ds2;

		final double r = 1.0 + (2.0 * a) + (6.0 * b);
		final double p = b;
		final double q = (-a) - (4.0 * b);						
			
		for(int i=0; i < count; i++)
		{			
			matrix[count-1].put(i, i, r);
			
			matrix[count-1].put((i + 1) % count, i, q);
			matrix[count-1].put((i + 2) % count, i, p);

			matrix[count-1].put(((i - 1) + count) % count, i, q);
			matrix[count-1].put(((i - 2) + count) % count, i, p);			
		}
			
			
		matrix[count-1] = Solve.pinv(matrix[count-1]);
	}
	
	protected void splitLines(double maxDist)
	{
		for(int i=0; i<NumberOfVertices; i++)
		{
			if(NumberOfVertices >= (_MAX_VERTICES-1))
			{
				return;
			}
			
			double distX = vecX.data[(i+1) % NumberOfVertices] - vecX.data[i];
			double distY = vecY.data[(i+1) % NumberOfVertices] - vecY.data[i];

			if(distX*distX + distY*distY > maxDist)
			{
				NumberOfVertices++;
				final DoubleMatrix newVecX = new DoubleMatrix(NumberOfVertices,1);
				final DoubleMatrix newVecY = new DoubleMatrix(NumberOfVertices,1);				

				for(int j=0; j<=i; j++)
				{
					newVecX.data[j] = vecX.data[j];
					newVecY.data[j] = vecY.data[j];					
				}

				newVecX.data[i+1] = vecX.data[i] + (distX/2.0);				
				newVecY.data[i+1] = vecY.data[i] + (distY/2.0);
			

				for(int j = i+1; j < (NumberOfVertices-1); j++)
				{
					newVecX.data[j+1] = vecX.data[j];					
					newVecY.data[j+1] = vecY.data[j];					
				}

				vecX = newVecX;
				vecY = newVecY;					
			}
		}
	}
	
	
	protected double[] matVecMul(final double[][] mat, double[] vec)
	{
		if(mat == null || mat[0] == null) return null;
		
		for(int i=0; i< mat.length; i++)
		{
			
		}
		
		
		return vec;		
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
		return vecX.getColumn(0).data;
	}
	
	public double[] getSnakeY()
	{
		return vecY.getColumn(0).data;
	}
	
	public int getNumberOfVertices()
	{
		return NumberOfVertices;
	}
}
