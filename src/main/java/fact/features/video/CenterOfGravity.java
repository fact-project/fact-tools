package fact.features.video;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.viewer.ui.DefaultPixelMapping;


public class CenterOfGravity implements Processor
{
	/**
	 * This function calculates the center of gravity for every slice. It uses only shower pixel.
	 * It also calculates the variance and covariance of the center of gravity.
	 */
	
	@Override
	public Data process(Data input)
	{
		/// init helper and utils
	    mpGeomXCoord = DefaultPixelMapping.getGeomXArray();
	    mpGeomYCoord = DefaultPixelMapping.getGeomYArray();
	    
		/// get input
		try
		{
			showerPixelArray = (int[]) input.get(showerPixel);
			dataCalibratedArray = (float[]) input.get(dataCalibrated);
		}
		catch (ClassCastException e)
		{
			log.error("wrong types" + e.toString());
		}
		
		if(dataCalibratedArray == null || showerPixelArray == null)
		{
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
		
		varcogx = new float[sliceCount];
		varcogy = new float[sliceCount];
		covcog = new float[sliceCount];

		cogVelocityX = new float[sliceCount - 1];
		cogVelocityY = new float[sliceCount - 1];
		cogVelocity = new float[sliceCount - 1];
		
		size = new float[sliceCount];
		
		// Baseline correction
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
			varcogx[slice] = 0;
			varcogy[slice] = 0;
			covcog[slice] = 0;
			
			// Calculate COGs
			for(int pix : showerPixelArray)
			{
				
				size[slice] += dataCalibratedArray[pix * sliceCount + slice] + eventBaseline ;
				cogx[slice] += (dataCalibratedArray[pix * sliceCount + slice] + eventBaseline) * mpGeomXCoord[pix];
				cogy[slice] += (dataCalibratedArray[pix * sliceCount + slice] + eventBaseline) * mpGeomYCoord[pix];
				
			}
			cogx[slice] /= size[slice];
			cogy[slice] /= size[slice];

			// Calculate variance and covariance
		    for (int pix: showerPixelArray )
		    {
		        varcogx[slice] += (dataCalibratedArray[pix * sliceCount + slice] + eventBaseline) * (mpGeomXCoord[pix] - cogx[slice]) * (mpGeomXCoord[pix] - cogx[slice]);
		        varcogy[slice] += (dataCalibratedArray[pix * sliceCount + slice] + eventBaseline) * (mpGeomYCoord[pix] - cogy[slice]) * (mpGeomYCoord[pix] - cogy[slice]);
		        covcog[slice]  += (dataCalibratedArray[pix * sliceCount + slice] + eventBaseline) * (mpGeomXCoord[pix] - cogx[slice]) * (mpGeomYCoord[pix] - cogy[slice]);
		    }
			varcogx[slice] /= size[slice];
			varcogy[slice] /= size[slice];
			covcog[slice] /= size[slice];

		    // Calculate velocities on the fly
			if (slice > 0)
			{
				cogVelocityX[slice - 1] = (cogx[slice] - cogx[slice-1]) / 0.5f;
				cogVelocityY[slice - 1] = (cogy[slice] - cogy[slice-1]) / 0.5f;
				cogVelocity[slice -1] = (float) Math.sqrt(cogVelocityX[slice-1]*cogVelocityX[slice-1] + cogVelocityY[slice - 1] * cogVelocityY[slice - 1]);
			}
		}
		
		input.put(outputKey + "_X", cogx);
		input.put(outputKey + "_Y", cogy);
		
		input.put(outputKey + "_VarX", varcogx);
		input.put(outputKey + "_VarY", varcogy);
		input.put(outputKey + "_CovXY" , covcog);

		input.put(outputKey + "_VelX", cogVelocityX);
		input.put(outputKey + "_VelY", cogVelocityY);
		
		input.put(outputKey + "_Vel", cogVelocity);
		
		return input;
	}

	public String getShowerPixel() {
		return showerPixel;
	}
	
	@Parameter(required = true, defaultValue = "showerPixel", description = "Key to the array of showerpixel Chids.")
	public void setShowerPixel(String showerPixel) {
		this.showerPixel = showerPixel;
	}

	public String getDataCalibrated() {
		return dataCalibrated;
	}
	
	@Parameter(required = true, defaultValue = "DataCalibrated", description = "Key to the calibrated data array.")
	public void setDataCalibrated(String dataCalibrated) {
		this.dataCalibrated = dataCalibrated;
	}

	public String getOutputKey() {
		return outputKey;
	}
	
	@Parameter(required = true, defaultValue  = "CenterOfGravity", description = "The output key tag. Will be inserted before all output keys.")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public int getNumberOfShowerPixelThreshold() {
		return numberOfShowerPixelThreshold;
	}
	
	@Parameter(required = true, defaultValue = "4", description = "Minimum of shower pixel to start calculation.")
	public void setNumberOfShowerPixelThreshold(int numberOfShowerPixelThreshold) {
		this.numberOfShowerPixelThreshold = numberOfShowerPixelThreshold;
	}

	static Logger log = LoggerFactory.getLogger(CenterOfGravity.class);
	
	private String showerPixel;
	private int[] showerPixelArray = null;
	
	private String dataCalibrated;
	private float[] dataCalibratedArray = null;
	
	// Helper and utilities
	private float[] mpGeomXCoord;
	private float[] mpGeomYCoord;
	private float[] size;
	private float eventBaseline;

	private int numberOfShowerPixelThreshold;
	
	// COG of showerPixelSet for every slice
	private float[] cogx = null;
	private float[] cogy = null;
	private float[] varcogx = null;
	private float[] varcogy = null;
	private float[] covcog = null;

	// Velocity of COG of showerPixel
	private float[] cogVelocityX;
	private float[] cogVelocityY;
	private float[] cogVelocity;
	
	
	private String outputKey;
	

}
