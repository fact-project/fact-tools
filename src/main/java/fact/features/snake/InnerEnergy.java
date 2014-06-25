package fact.features.snake;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class InnerEnergy implements Processor
{
	private String snakeX = null;
	private String snakeY = null;
	
	private String outkey = null;
	
	@Override
	public Data process(Data input) 
	{
		if(outkey == null) throw new RuntimeException("Key \"outkey\" not set");
		
		EventUtils.mapContainsKeys(getClass(), input, snakeX, snakeY);	
		
		double[] x = (double[]) input.get(snakeX);
		double[] y = (double[]) input.get(snakeY);
		
		
		final double b = 1 ;
		final double r = 1.0 + 6.0*b;
		final double p = b;
		final double q = - 4.0*b;						
			
		int dim = x.length;
		RealMatrix matrix = new Array2DRowRealMatrix(dim, dim);		
		
		for(int i=0;i<dim; i++)
		{
			matrix.setEntry(i, i, r);
			
			matrix.setEntry((i + 1) % dim, i, q);
			matrix.setEntry((i + 2) % dim, i, p);

			matrix.setEntry(((i - 1) + dim) % dim, i, q);
			matrix.setEntry(((i - 2) + dim) % dim, i, p);			
		}
			
		matrix = new LUDecomposition(matrix).getSolver().getInverse();
		
		RealMatrix vecX = new Array2DRowRealMatrix(dim,1);
		RealMatrix vecY = new Array2DRowRealMatrix(dim,1);
		
		for(int i=0; i<dim; i++)
		{
			vecX.setEntry(i, 0, x[i]);
			vecY.setEntry(i, 0, y[i]);
		}
		
		double erg = vecX.subtract(matrix.multiply(vecX)).getFrobeniusNorm();
		erg += vecY.subtract(matrix.multiply(vecY)).getFrobeniusNorm();
		
		erg /= 2.0*dim;
		
//		System.out.println(erg);
		
		input.put(outkey, erg);				
		return input;
	}

	public String getSnakeX() {
		return snakeX;
	}

	public void setSnakeX(String snakeX) {
		this.snakeX = snakeX;
	}

	public String getSnakeY() {
		return snakeY;
	}

	public void setSnakeY(String snakeY) {
		this.snakeY = snakeY;
	}

	public String getOutkey() {
		return outkey;
	}

	public void setOutkey(String outkey) {
		this.outkey = outkey;
	}
	
	

}
