package fact.features.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
/**
 * This processor calculates the angle between the main axis of the shower and the line cog <-> sourcePosition
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;Fabian Temme
 * 
 */
public class Alpha implements Processor {
	static Logger log = LoggerFactory.getLogger(Alpha.class);

	@Parameter(required = false, defaultValue = "shower:ellipse:cog:x")
	private String cogxKey = "shower:ellipse:cog:x";
	@Parameter(required = false, defaultValue = "shower:ellipse:cog:y")
	private String cogyKey = "shower:ellipse:cog:y";
	@Parameter(required = false, defaultValue = "shower:ellipse:delta")
	private String deltaKey = "shower:ellipse:delta";
	@Parameter(required = false, defaultValue="source:x")
	private String sourcexKey = "source:x";
	@Parameter(required = false, defaultValue="source:y")
	private String sourceyKey = "source:y";
	@Parameter(required = false, defaultValue="shower:source:alpha")
	private String outputKey = "shower:source:alpha";
	
	@Override
	public Data process(Data item) {
		
		Utils.mapContainsKeys(item, sourcexKey, sourceyKey, cogxKey, cogyKey, deltaKey);

		double sourcex = (Double) item.get(sourcexKey);
		double sourcey = (Double) item.get(sourceyKey);

		double cogx = (Double) item.get(cogxKey);
		double cogy = (Double) item.get(cogyKey);
		double delta = (Double) item.get(deltaKey);

		double alpha = 0.0;
	    double auxiliary_angle  = Math.atan( (sourcey - cogy )/(sourcex - cogx) );
	    alpha                  =  (delta - auxiliary_angle);
	
	    if (alpha > Math.PI / 2)
	    {
	        alpha              = alpha - Math.PI;
	    }
	    if (alpha < -Math.PI / 2)
	    {
	        alpha              = Math.PI + alpha;
	    }
	    item.put(outputKey, alpha);
		return item;
	}
}
