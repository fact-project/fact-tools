package fact.features.snake;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class Length  implements Processor
{
	String outkey;	
	
	String snakeX = null;
	String snakeY = null;

	@Override
	public Data process(Data input) 
	{
		if(outkey == null){
			throw new RuntimeException("Missing parameter: outkey");
		}
		
		
		EventUtils.mapContainsKeys(getClass(), input, snakeX, snakeY);		
		double[] x = (double[]) input.get(snakeX);
		double[] y = (double[]) input.get(snakeY);
		
		if (x.length < 3) input.put(outkey, 0);

		float erg = 0;
		for (int i = 1; i < x.length; i++)
		{
			float a = (float) (x[i] - x[i - 1]);
			a *= a;
			float b = (float) (y[i] - y[i - 1]);
			b *=  b;

			erg += Math.sqrt(a + b);
		}

		float a = (float) (x[0] - x[x.length - 1]);
		a *= a;
		float b = (float) (y[0] - y[y.length - 1]);
		b *= b;	

		input.put(outkey, Math.abs(erg));
		
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
