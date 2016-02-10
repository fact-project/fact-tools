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
	
	
	@Parameter(required = false, defaultValue="sourcePosition:x")
	private String sourcePositionXKey = "sourcePosition:x";
	@Parameter(required = false, defaultValue="sourcePosition:y")
	private String sourcePositionYKey = "sourcePosition:y";
	@Parameter(required = false, defaultValue = "shower:ellipse:cog:x")
	private String cogxKey = "shower:ellipse:cog:x";
	@Parameter(required = false, defaultValue = "shower:ellipse:cog:y")
	private String cogyKey = "shower:ellipse:cog:y";
	@Parameter(required = false, defaultValue = "shower:ellipse:delta")
	private String deltaKey = "shower:ellipse:delta";
	@Parameter(required = false, defaultValue="shower:source:cosDeltaAlpha")
	private String outputKey = "shower:source:cosDeltaAlpha";
	
	@Override
	public Data process(Data item)
	{
		
		Utils.mapContainsKeys( item, sourcePositionXKey, sourcePositionYKey, cogxKey, cogyKey, deltaKey);
		
		double sourcex = (Double) item.get(sourcePositionXKey);
		double sourcey = (Double) item.get(sourcePositionYKey);

		double cogx = (Double) item.get(cogxKey);
		double cogy = (Double) item.get(cogyKey);
		double delta = (Double) item.get(deltaKey);
		
		double sx,sy,dist;
		sx = cogx - sourcex;
		sy = cogy - sourcey;
		dist = Math.sqrt(sx*sx + sy*sy);
			
		if(dist == 0)
			return item;
		
		double s = Math.sin(delta);
		double c = Math.cos(delta);
		
	    double arg2 = c*sx + s*sy; // mm
						
	     if (arg2 == 0)
	         return item;

	    //double arg1 = c*sy - s*sx;          // [mm]

		double cosDeltaAlpha = arg2 / dist;
		
		item.put(outputKey, cosDeltaAlpha);
		return item;
	}
}
