package fact.features.snake;

import fact.Utils;
import stream.Data;
import stream.Processor;

public class PolygonLength  implements Processor
{
	String outkey;	
	
	String polygonX = null;
	String polygonY = null;

	@Override
	public Data process(Data input) 
	{
		if(outkey == null) throw new RuntimeException("Key \"outkey\" not set");		
		
		Utils.mapContainsKeys( input, polygonX, polygonY);
		
		
		double[] x = (double[]) input.get(polygonX);
		double[] y = (double[]) input.get(polygonY);
		
		if (x.length < 3) input.put(outkey, 0);

		double erg = 0;
		for (int i = 1; i < x.length; i++)
		{
			double a = (double) (x[i] - x[i - 1]);
			a *= a;
			double b = (double) (y[i] - y[i - 1]);
			b *=  b;

			erg += Math.sqrt(a + b);
		}

		double a = (double) (x[0] - x[x.length - 1]);
		a *= a;
		double b = (double) (y[0] - y[y.length - 1]);
		b *= b;	
		erg += Math.sqrt(a + b);

		input.put(outkey, Math.abs(erg));
		
		return input;
	}

	

	public String getPolygonX() {
		return polygonX;
	}



	public void setPolygonX(String polygonX) {
		this.polygonX = polygonX;
	}



	public String getPolygonY() {
		return polygonY;
	}



	public void setPolygonY(String polygonY) {
		this.polygonY = polygonY;
	}



	public String getOutkey() {
		return outkey;
	}

	public void setOutkey(String outkey) {
		this.outkey = outkey;
	}
	
}
