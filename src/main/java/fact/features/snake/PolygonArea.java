package fact.features.snake;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class PolygonArea  implements Processor
{
	String outputKey = null;	
	
	String snakeX = null;
	String snakeY = null;

	@Override
	public Data process(Data input) 
	{
		if(outputKey == null)
		{
			throw new RuntimeException("Missing parameter: outputKey");
		}	
		
		
		EventUtils.mapContainsKeys(getClass(), input, snakeX, snakeY);		
		double[] x = (double[]) input.get(snakeX);
		double[] y = (double[]) input.get(snakeY);
		
		final int N = x.length;
		
		float erg = 0;
		
		for(int i=0; i < N; i++)
		{			
			erg += (y[i] + y[(i+1) % N]) * (x[i] - x[(i+1) % N]);
		}				

		input.put(outputKey, Math.abs(0.5 * erg));
		
		return input;
	}

	public String getOutkey() {
		return outputKey;
	}

	public void setOutkey(String outkey) {
		this.outputKey = outkey;
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
	
	
	
}
