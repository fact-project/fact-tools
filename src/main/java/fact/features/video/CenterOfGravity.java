package fact.features.video;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.Constants;
import fact.viewer.ui.DefaultPixelMapping;


/**
 * This class calculates the center of gravity for every slice. It uses only shower pixel.
 * It also calculates the variance and covariance of the center of gravity.
 *
 * @param ShowerPixel Array containing shower pixel
 * @param DataCalibrated Array containing the calibrated dataCalibrated
 * @param NumberOfShowerPixelThreshold Constant that triggers calculation based on the number of shower pixel
 * @param OutputKey String contains the name of the output key
 *
 * @return X Array contains the X position of the center of gravity
 * @return Y Array contains the Y position of the center of gravity
 * @return VarX Array contains the X variance of the center of gravity
 * @return VarY Array contains the Y variance of the center of gravity
 * @return CovXY Array contains the covariance of the center of gravity in X and Y
 * @return VelX Array contains the X velocity of the center of gravity
 * @return VelY Array contains the Y velocity of the center of gravity
 * @return Vel Array contains the velocity of the center of gravity
 */

public class CenterOfGravity implements Processor
{
	
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

	public void setShowerPixel(String showerPixel) {
		this.showerPixel = showerPixel;
	}

	public String getDataCalibrated() {
		return dataCalibrated;
	}

	public void setDataCalibrated(String dataCalibrated) {
		this.dataCalibrated = dataCalibrated;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public int getNumberOfShowerPixelThreshold() {
		return numberOfShowerPixelThreshold;
	}

	public void setNumberOfShowerPixelThreshold(int numberOfShowerPixelThreshold) {
		this.numberOfShowerPixelThreshold = numberOfShowerPixelThreshold;
	}

	static Logger log = LoggerFactory.getLogger(CenterOfGravity.class);
	
	private String showerPixel = "showerPixel";
	private int[] showerPixelArray = null;
	
	private String dataCalibrated = "DataCalibrated";
	private float[] dataCalibratedArray = null;
	
	// Helper and utilities
	private float[] mpGeomXCoord;
	private float[] mpGeomYCoord;
	private float[] size;
	private float eventBaseline;

	private int numberOfShowerPixelThreshold = 5;
	
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
	
	
	private String outputKey = "CenterOfGravity";
	

}
