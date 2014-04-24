package fact.features.snake;

import java.awt.Polygon;

import fact.Constants;
import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;
import stream.Data;
import stream.Processor;

public class IntegratePolygonSingle implements Processor
{
	private String pixelData = null;
	private String polygonX = null;
	private String polygonY = null;
	
	private String outkey = null;
	
	private String frame = null;
	private int f = 0;
	
	@Override
	public Data process(Data input) 
	{
		if(outkey == null)
		{
			throw new RuntimeException("Key \"outkey\" not set");
		}
		
		EventUtils.mapContainsKeys(getClass(), input, pixelData, polygonX, polygonY);
		
		
		Integer tmp = (Integer) input.get(frame);
		if(tmp != null) f = tmp;
			
		
		double[] data  = (double[]) input.get(pixelData);
		double[] polyX = (double[]) input.get(polygonX);
		double[] polyY = (double[]) input.get(polygonY);		
		
		Polygon poly = new Polygon();
		
		for(int i=0; i<polyX.length; i++)
		{
			poly.addPoint( (int) polyX[i], (int) polyY[i]);
		}
		
		
		double erg = 0;
		for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
		{
			if(poly.contains(DefaultPixelMapping.getPosXinMM(i), DefaultPixelMapping.getPosYinMM(i)) )
			{
				erg += data[f*Constants.NUMBEROFPIXEL + i];
			}			
		}
		
		input.put(outkey, erg);		
		
		return input;
	}

	public String getPixelData() {
		return pixelData;
	}

	public void setPixelData(String pixelData) {
		this.pixelData = pixelData;
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

	public String getFrame() {
		return frame;
	}

	public void setFrame(String frame) {
		this.frame = frame;
	}
	
	

}
