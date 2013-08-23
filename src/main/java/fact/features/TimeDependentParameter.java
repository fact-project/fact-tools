package fact.features;


import fact.Constants;
import fact.viewer.ui.DefaultPixelMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * This class calculates the parameter rho.
 * This parameter is a measure for the source-gamma-ness of an event
 * 
 */

public class TimeDependentParameter implements Processor
{
	@Override
	public Data process(Data input)
	{
		/// init helper and utils
	    mpGeomXCoord            = DefaultPixelMapping.getGeomXArray();
	    mpGeomYCoord            = DefaultPixelMapping.getGeomYArray();
	    
	    
	    
		/// get input
	
		try
		{
			showerPixelArray = (int[]) input.get(showerPixel);
			sourcePositionArray = (float[]) input.get(sourcePosition);
			dataCalibratedArray = (float[]) input.get(dataCalibrated);
		}
		catch (ClassCastException e)
		{
			log.error("wrong types" + e.toString());
		}
		if(dataCalibratedArray == null || showerPixelArray == null ||sourcePositionArray==null){
			log.error("Map does not conatin the right values for the keys");
			return null;
		}
		
		if (showerPixelArray.length < numberOfShowerPixelThreshold)
		{
			return input;
		}
		
		/// init internal parameter
		
		// COG for every slice
		int sliceCount = dataCalibratedArray.length / Constants.NUMBEROFPIXEL; // ROI
		cogx = new float[sliceCount];
		cogy = new float[sliceCount];
		
		cogVelocityX = new float[sliceCount - 1];
		cogVelocityY = new float[sliceCount - 1];
		cogVelocity = new float[sliceCount - 1];
		
		size = new float[sliceCount];
		
		eventBaseline = 0.0f;
		for(int pix : showerPixelArray)
			for(int slice = 0; slice < sliceCount; slice++)
			{
				eventBaseline = eventBaseline > dataCalibratedArray[pix * sliceCount + slice] ? dataCalibratedArray[pix * sliceCount + slice] : eventBaseline;
			}
		eventBaseline = eventBaseline > 0 ? eventBaseline : -eventBaseline;
		
		for(int slice = 0; slice < sliceCount; slice++)
		{
			size[slice] = 0;
			cogx[slice] = 0;
			cogy[slice] = 0;
			
			for(int pix : showerPixelArray)
			{
				
				size[slice] += dataCalibratedArray[pix * sliceCount + slice] + eventBaseline ;
				cogx[slice] += (dataCalibratedArray[pix * sliceCount + slice] + eventBaseline) * mpGeomXCoord[pix];
				cogy[slice] += (dataCalibratedArray[pix * sliceCount + slice] + eventBaseline) * mpGeomYCoord[pix];
				
			}
			cogx[slice] /= size[slice];
			cogy[slice] /= size[slice];
			
			if (slice > 0)
			{
				cogVelocityX[slice - 1] = (cogx[slice] - cogx[slice-1]) / 0.5f;
				cogVelocityY[slice - 1] = (cogy[slice] - cogy[slice-1]) / 0.5f;
				cogVelocity[slice -1] = (float) Math.sqrt(cogVelocityX[slice-1]*cogVelocityX[slice-1] + cogVelocityY[slice - 1] * cogVelocityY[slice - 1]);
			}
			
		}
		
		input.put(outputKey + "COGX", cogx);
		input.put(outputKey + "COGY", cogy);
		input.put(outputKey + "COGVX", cogVelocityX);
		input.put(outputKey + "COGVY", cogVelocityY);
		input.put(outputKey + "COGV", cogVelocity);
		return input;
	}
	
	static Logger log = LoggerFactory.getLogger(TimeDependentParameter.class);
	
	/// @Todo: insert get/set
	private String sourcePosition = "sourcePosition";
	private float[] sourcePositionArray = null;
	
	private String showerPixel = "showerPixel";
	private int[] showerPixelArray = null;
	
	private String dataCalibrated = "DataCalibrated";
	private float[] dataCalibratedArray = null;
	
	private String arrivalTime = "arrivalTime";
	private float[] arrivalTimeArray = null;
	
	// Helper and utils
	private float[] mpGeomXCoord;
	private float[] mpGeomYCoord;
	private float[] size;
	private float eventBaseline;

	private int numberOfShowerPixelThreshold = 5;
	
	// COG of showerPixelSet for every slice
	private float[] cogx = null;
	private float[] cogy = null;
	
	// Velocity of COG of showerPixel
	private float[] cogVelocityX;
	private float[] cogVelocityY;
	private float[] cogVelocity;
	
	private String outputKey = "TDP_";
	

}
