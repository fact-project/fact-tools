package fact.features.snake.video;

import fact.Utils;
import fact.hexmap.ui.overlays.PolygonOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class SnakeDrawer implements Processor
{

	@Parameter(required = true, description = "Input: Name of Looped Data")
	private String key = null;
	@Parameter(required = true, description = "Input: Name of Overlay")
	private String keyOverlay = null;
	@Parameter(required = true, description = "Input: Number of Slice")
	private String keySlice = null;
	
	
	
	@Override	
	public Data process(Data input) 
	{			
		Utils.isKeyValid(input, key, Data[].class);
		
		Data[] items = (Data[]) input.get(key);	
				
		double[][] x = new double[300][0];
		double[][] y = new double[300][0];		
		
		for(int itr=0; itr<items.length; itr++)
		{
			Utils.isKeyValid(items[itr], keyOverlay, PolygonOverlay.class);
			Utils.isKeyValid(items[itr], keySlice, Integer.class);
			
			PolygonOverlay overlay = (PolygonOverlay) items[itr].get(keyOverlay);		
			int slice = (Integer) items[itr].get(keySlice);			
			
			x[ slice ] = overlay.getX()[0];
			y[ slice ] = overlay.getY()[0];
		
		}
		
		
		input.put("SnakeVideo_overlays", new PolygonOverlay(x, y) );
		
		return input;
	}



	public String getKeyOverlay() {
		return keyOverlay;
	}



	public void setKeyOverlay(String keyOverlays) {
		this.keyOverlay = keyOverlays;
	}



	public String getKey() {
		return key;
	}



	public void setKey(String key) {
		this.key = key;
	}



	public String getKeySlice() {
		return keySlice;
	}



	public void setKeySlice(String keySlice) {
		this.keySlice = keySlice;
	}

	
	
	
	
	
}


