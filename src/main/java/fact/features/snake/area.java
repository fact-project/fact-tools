package fact.features.snake;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class area  implements Processor
{
	String outputKey = null;	
	
	String snakeX = null;
	String snakeY = null;

	@Override
	public Data process(Data input) 
	{
		if(outputKey == null){
			throw new RuntimeException("Missing parameter: outputKey");
		}	
		
		
		EventUtils.mapContainsKeys(getClass(), input, snakeX, snakeY);		
		double[] x = (double[]) input.get(snakeX);
		double[] y = (double[]) input.get(snakeY);
		
		int border = x.length-1;

		float erg = 0;

		//Oberer Rand
		erg += ( x[0] * ( x[border] - x [1]) );


		for(int i=1; i<border-1; i++)
		{
			erg += ( y[i] * ( x[i-1] - x [i+1]) );
		}

		//Unterer Rand
		erg += ( y[border] * ( x[border-1] - x [0]) );

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
