package fact.features;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.EventUtils;

public class CosDeltaAlpha implements Processor{

	/**
	 * This function calculates CosDeltaAlpha from MARS Code:
	 *  fCosDeltaAlpha cosine of angle between d and a, where
	 *	- d is the vector from the source position to the
	 *	center of the ellipse
	 *	- a is a vector along the main axis of the ellipse,
	 *	defined with positive x-component
	 * @param input
	 * @return
	 */
	@Override
	public Data process(Data input)
	{

		double cosDeltaAlpha = 0;
		
		EventUtils.mapContainsKeys(getClass(), input, sourcePosition, cogX, cogY, hillasDelta);
		sourcePositionArray = (double[]) input.get(sourcePosition);
		cogXValue = (Double) input.get(cogX);
		cogYValue = (Double) input.get(cogY);
		hillasDeltaValue = (Double) input.get(hillasDelta);
		
		double sx,sy,dist;
		sx = cogXValue - sourcePositionArray[0];
		sy = cogYValue - sourcePositionArray[1];
		dist = Math.sqrt(sx*sx + sy*sy);
			
		if(dist == 0)
			return input;
		
		double s = Math.sin(hillasDeltaValue);
		double c = Math.cos(hillasDeltaValue);
		
	    double arg2 = c*sx + s*sy; // mm
						
	     if (arg2 == 0)
	         return input;

	    //double arg1 = c*sy - s*sx;          // [mm]

		cosDeltaAlpha = arg2 / dist;
		
		input.put(outputKey, cosDeltaAlpha);
		return input;
	}
	
	public String getSourcePosition() {
		return sourcePosition;
	}
	@Parameter(required = true, defaultValue = "sourcePosition", description = "Key to array containing the source position in mm.")
	public void setSourcePosition(String sourcePosition) {
		this.sourcePosition = sourcePosition;
	}
	public String getCogX() {
		return cogX;
	}
	@Parameter(required = true, defaultValue = "COGx", description = "Key of the COGX value.")
	public void setCogX(String cogX) {
		this.cogX = cogX;
	}
	public String getCogY() {
		return cogY;
	}
	@Parameter(required = true, defaultValue = "COGy", description = "Key of the COGY value.")
	public void setCogY(String cogY) {
		this.cogY = cogY;
	}
	public String getHillasDelta() {
		return hillasDelta;
	}
	@Parameter(required = true, defaultValue = "Hillas_Delta", description = "Key to the Hillas_Delta angle.")
	public void setHillasDelta(String hillasDelta) {
		this.hillasDelta = hillasDelta;
	}
	public String getOutputKey() {
		return outputKey;
	}
	@Parameter(required = true, defaultValue = "CosDeltaAlpha", description = "Output key for this processor.")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	private String sourcePosition;
	private double[] sourcePositionArray;
	private String cogX, cogY;
	private Double cogXValue, cogYValue;
	private String hillasDelta;
	private Double hillasDeltaValue;
	private String outputKey;
	
}
