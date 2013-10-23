package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.viewer.ui.DefaultPixelMapping;


public class SlopeSpreadWeighted implements Processor
{

	public Data process(Data input)
	{
	    mpGeomXCoord =  DefaultPixelMapping.getGeomXArray();
	    mpGeomYCoord =  DefaultPixelMapping.getGeomYArray();

		return input;
	}

	private float[] mpGeomXCoord;
	private float[] mpGeomYCoord;
	
	private String showerPixel;
	private String arrivalTime;
	private String photonCharge;
	
	
	
}
