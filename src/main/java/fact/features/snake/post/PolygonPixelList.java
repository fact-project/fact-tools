package fact.features.snake.post;

import java.awt.Polygon;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.Utils;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.PixelSetOverlay;

/**
 *	PolygonPixelList
 *	Erzeugt ein Array an allen Pixeln die sich innerhalb der Snake befinden
 *	Diese kann anschlie{\ss}end mit den ueblichen Hillas-Parameter Prozessoren verarbeitet werden
 *	
 *  @author Dominik Baack &lt;dominik.baack@udo.edu&gt;
 *
 */
public class PolygonPixelList implements Processor
{
	@Parameter(required = true, description = "Snake X-Koordinaten")
	private String polygonX = null;
	@Parameter(required = true, description = "Snake Y-Koordinaten")
	private String polygonY = null;
	
	@Parameter(required = true, description = "Anzahl an Pixel die Innerhalb der Snake liegen")
	private String outkeyNumberOfPixel = null;
	 @Parameter(required = true, description = "Liste an Pixeln die Innerhalb der Snake liegen")
	private String outkeyPixelList = null;

    private FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    private PixelSetOverlay cleanedPixelSet;

    @Override
	public Data process(Data input) 
	{		
		Utils.mapContainsKeys( input, polygonX, polygonY);		
		Utils.isKeyValid(input, polygonX, double[].class);
		Utils.isKeyValid(input, polygonY, double[].class);
		
		double[] polyX = (double[]) input.get(polygonX);
		double[] polyY = (double[]) input.get(polygonY);		
		
		Polygon poly = new Polygon();	// Wandel die Snake in ein Polygon um
		
		for(int i=0; i<polyX.length; i++)
		{
			poly.addPoint( (int) polyX[i], (int) polyY[i]);
		}
		
		int numberOfPixel = 0;
		boolean[] chidInPoly = new boolean[1440];
		
		for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
		{
			if(poly.contains(pixelMap.getPixelFromId(i).getXPositionInMM(), pixelMap.getPixelFromId(i).getYPositionInMM()))	// Pruefe ob Pixel im Poly/Snake liegt
			{				
				chidInPoly[i] = true;
				numberOfPixel++;
			}			
		}
		
		int[] chids = new int[numberOfPixel];	
		cleanedPixelSet = new PixelSetOverlay();
		
		for(int i = 0, tmpCount=0; i<Constants.NUMBEROFPIXEL; i++)
		{
			if(chidInPoly[i])
			{
				chids[tmpCount++] = i;
				cleanedPixelSet.addById(i);
			}
		}		
		input.put(outkeyPixelList+"Set", cleanedPixelSet);
		
		input.put(outkeyNumberOfPixel, numberOfPixel);
		input.put(outkeyPixelList, chids);
		
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

	public String getOutkeyNumberOfPixel() {
		return outkeyNumberOfPixel;
	}

	public void setOutkeyNumberOfPixel(String outkeyNumberOfPixel) {
		this.outkeyNumberOfPixel = outkeyNumberOfPixel;
	}

	public String getOutkeyPixelList() {
		return outkeyPixelList;
	}

	public void setOutkeyPixelList(String outkeyPixelList) {
		this.outkeyPixelList = outkeyPixelList;
	}
	
}
