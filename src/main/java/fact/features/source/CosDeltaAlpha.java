package fact.features.source;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates CosDeltaAlpha from MARS Code:
 *  fCosDeltaAlpha cosine of angle between d and a, where
 *	- d is the vector from the source position to the
 *	center of the ellipse
 *	- a is a vector along the main axis of the ellipse,
 *	defined with positive x-component
 * 
 * @author Jan Freiwald
 *
 */
public class CosDeltaAlpha implements Processor{
	@Parameter(required=true)
	private String sourcePositionKey;
	@Parameter(required=true)
	private String cogxKey;
	@Parameter(required=true)
	private String cogyKey;
	@Parameter(required=true)
	private String deltaKey;
	@Parameter(required=true)
	private String outputKey;
	

	private double[] sourcePosition;

	private Double cogx;
	private Double cogy;
	private Double delta;

	
	@Override
	public Data process(Data input)
	{

		double cosDeltaAlpha = 0;
		
		Utils.mapContainsKeys( input, sourcePositionKey, cogxKey, cogyKey, deltaKey);
		sourcePosition = (double[]) input.get(sourcePositionKey);
		cogx = (Double) input.get(cogxKey);
		cogy = (Double) input.get(cogyKey);
		delta = (Double) input.get(deltaKey);
		
		double sx,sy,dist;
		sx = cogx - sourcePosition[0];
		sy = cogy - sourcePosition[1];
		dist = Math.sqrt(sx*sx + sy*sy);
			
		if(dist == 0)
			return input;
		
		double s = Math.sin(delta);
		double c = Math.cos(delta);
		
	    double arg2 = c*sx + s*sy; // mm
						
	     if (arg2 == 0)
	         return input;

	    //double arg1 = c*sy - s*sx;          // [mm]

		cosDeltaAlpha = arg2 / dist;
		
		input.put(outputKey, cosDeltaAlpha);
		return input;
	}

	public String getSourcePositionKey() {
		return sourcePositionKey;
	}

	public void setSourcePositionKey(String sourcePositionKey) {
		this.sourcePositionKey = sourcePositionKey;
	}

	public String getCogxKey() {
		return cogxKey;
	}

	public void setCogxKey(String cogxKey) {
		this.cogxKey = cogxKey;
	}

	public String getCogyKey() {
		return cogyKey;
	}

	public void setCogyKey(String cogyKey) {
		this.cogyKey = cogyKey;
	}

	public String getDeltaKey() {
		return deltaKey;
	}

	public void setDeltaKey(String deltaKey) {
		this.deltaKey = deltaKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	
	
	
}
