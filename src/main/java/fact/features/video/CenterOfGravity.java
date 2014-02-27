package fact.features.video;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.EventUtils;
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
	    mpGeomXCoord =  DefaultPixelMapping.getGeomXArray();
	    mpGeomYCoord =  DefaultPixelMapping.getGeomYArray();
	    
	    // check keys
		EventUtils.mapContainsKeys(getClass(), input, showerPixel, dataCalibrated);
		/// get input
		try
		{
			showerPixelArray = (int[]) input.get(showerPixel);
			dataCalibratedArray = (double[]) input.get(dataCalibrated);
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
		
		cogx = new double[sliceCount];
		cogy = new double[sliceCount];
		
		varcogx = new double[sliceCount];
		varcogy = new double[sliceCount];
		covcog = new double[sliceCount];

		cogVelocityX = new double[sliceCount - 1];
		cogVelocityY = new double[sliceCount - 1];
		cogVelocity = new double[sliceCount - 1];
		cogVelocityXError = new double[sliceCount - 1];
		cogVelocityYError = new double[sliceCount - 1];
		cogVelocityError = new double[sliceCount - 1];
		size = new double[sliceCount];
		
		double minimalVelocity = Double.MAX_VALUE; // minimal velocity of all slices
		int minimalVelocityId = 0;
		double maximalVelocity = Double.MIN_VALUE; // maximal velocity of all slices
		int maximalVelocityId = 0;
		double bestVelocity = 0; // velocity with minimal "error"
		int bestVelocityId = 0;
		double bestVelocityError = Double.MAX_VALUE; // the corresponding "error"
		
		
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
				//TODO insert rotate by hillas_delta switch
				size[slice] += dataCalibratedArray[pix * sliceCount + slice] + eventBaseline;
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
			
			varcogx[slice] = Math.sqrt(varcogx[slice]);
			varcogy[slice] = Math.sqrt(varcogy[slice]);
			covcog[slice] = Math.sqrt(covcog[slice]);
			
			
		    // Calculate velocities on the fly
			if (slice > 0)
			{
				cogVelocityX[slice - 1] = (cogx[slice] - cogx[slice - 1]) / 0.5f;
				cogVelocityY[slice - 1] = (cogy[slice] - cogy[slice - 1]) / 0.5f;
				cogVelocity[slice - 1] = (double) Math.sqrt(cogVelocityX[slice - 1]*cogVelocityX[slice - 1] + cogVelocityY[slice - 1] * cogVelocityY[slice - 1]);
				cogVelocityXError[slice - 1] = 2.0 * (double) Math.sqrt(varcogx[slice] * varcogx[slice] + varcogx[slice - 1] * varcogx[slice - 1]);
				cogVelocityYError[slice - 1] = 2.0 * (double) Math.sqrt(varcogy[slice] * varcogy[slice] + varcogy[slice - 1] * varcogy[slice - 1]);
				// here i will define a better quality parameter
				cogVelocityError[slice - 1] = Math.sqrt((cogVelocityX[slice - 1] * cogVelocityX[slice - 1] *
														cogVelocityXError[slice - 1] * cogVelocityXError[slice - 1] +
														cogVelocityY[slice - 1] * cogVelocityY[slice - 1] *
														cogVelocityYError[slice - 1] * cogVelocityYError[slice - 1] ) / 
														(cogVelocityX[slice - 1] * cogVelocityX[slice - 1] + cogVelocityY[slice - 1] * cogVelocityY[slice - 1]) );

				if (cogVelocity[slice - 1] < minimalVelocity)
				{
					minimalVelocity = cogVelocity[slice - 1];
					minimalVelocityId = slice - 1;
				}
				if(cogVelocity[slice - 1] > maximalVelocity)
				{
					maximalVelocity = cogVelocity[slice - 1];
					maximalVelocityId = slice - 1;
				}
				if(cogVelocityError[slice - 1] < bestVelocityError)
				{
					bestVelocityError = cogVelocityError[slice - 1];
					bestVelocity = cogVelocity[slice - 1];
					bestVelocityId = slice - 1;
				}
			
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
		input.put(outputKey + "_VelErr", cogVelocityError);
		
		input.put(outputKey + "_MinVel", minimalVelocity);
		input.put(outputKey + "_MinVelId", minimalVelocityId);
		
		input.put(outputKey + "_MaxVel", maximalVelocity);
		input.put(outputKey + "_MaxVelId", maximalVelocityId);
		
		input.put(outputKey + "_BestVel", bestVelocity);
		input.put(outputKey + "_BestVelError", bestVelocityError);
		input.put(outputKey + "_BestVelId", bestVelocityId);
		
		return input;
	}

	public String getShowerPixel() {
		return showerPixel;
	}
	
	@Parameter(required = true, defaultValue = "showerPixel", description = "Key to the array of showerpixel chids.")
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
	private double[] dataCalibratedArray = null;
	
	// Helper and utilities
	private float[] mpGeomXCoord;
	private float[] mpGeomYCoord;
	private double[] size;
	private double eventBaseline;

	private int numberOfShowerPixelThreshold;
	
	// COG of showerPixelSet for every slice
	private double[] cogx = null;
	private double[] cogy = null;
	private double[] varcogx = null;
	private double[] varcogy = null;
	private double[] covcog = null;

	// Velocity of COG of showerPixel
	private double[] cogVelocityX = null;
	private double[] cogVelocityY = null;
	private double[] cogVelocity = null;

	private double[] cogVelocityXError = null;
	private double[] cogVelocityYError = null;
	private double[] cogVelocityError = null;
	
	private String outputKey;
	

}
