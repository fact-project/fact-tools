package fact.features.snake;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class PolygonArea  implements Processor
{
	private String outkey = null;	
	
	private String polygonX = null;
	private String polygonY = null;

	@Override
	public Data process(Data input) 
	{
		if(outkey == null) throw new RuntimeException("Key \"outkey\" not set");		
		
		EventUtils.mapContainsKeys(getClass(), input, polygonX, polygonY);		
		
		
		double[] x = (double[]) input.get(polygonX);
		double[] y = (double[]) input.get(polygonY);
		
		final int N = x.length;
		
		float erg = 0;
		
		for(int i=0; i < N; i++)
		{			
			erg += (y[i] + y[(i+1) % N]) * (x[i] - x[(i+1) % N]);
		}				

		input.put(outkey, Math.abs(0.5 * erg));
		
		return input;
	}

	public String getOutkey() {
		return outkey;
	}

	public void setOutkey(String outkey) {
		this.outkey = outkey;
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
	
	
}
