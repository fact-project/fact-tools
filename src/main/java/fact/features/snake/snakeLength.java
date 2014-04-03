package fact.features.snake;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class snakeLength  implements Processor
{
	String outkey;	

	@Override
	public Data process(Data input) 
	{
		EventUtils.mapContainsKeys(getClass(), input, "snake_X", "snake_Y");		
		double[] x = (double[]) input.get("snake_X");
		double[] y = (double[]) input.get("snake_Y");
		
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

	public String getOutkey() {
		return outkey;
	}

	public void setOutkey(String outkey) {
		this.outkey = outkey;
	}
	
}
