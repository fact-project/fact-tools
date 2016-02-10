package fact.features.source;

import fact.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Quite simply the distance between the CoG of the shower and the source position.
 * @author kaibrugge, Fabian Temme
 *
 */
public class Distance implements Processor {
	static Logger log = LoggerFactory.getLogger(Distance.class);
   
	@Parameter(required = false, defaultValue = "shower:ellipse:cog:x")
	private String cogxKey = "shower:ellipse:cog:x";
	@Parameter(required = false, defaultValue = "shower:ellipse:cog:y")
	private String cogyKey = "shower:ellipse:cog:y";
	@Parameter(required = false, defaultValue="sourcePosition:x")
	private String sourcePositionXKey = "sourcePosition:x";
	@Parameter(required = false, defaultValue="sourcePosition:y")
	private String sourcePositionYKey = "sourcePosition:y";
	@Parameter(required = false, defaultValue="shower:source:distance")
	private String outputKey = "shower:source:distance";

	@Override
	public Data process(Data item) {
		Utils.mapContainsKeys( item, cogxKey, cogyKey, sourcePositionXKey, sourcePositionYKey);

		double sourcex = (Double) item.get(sourcePositionXKey);
		double sourcey = (Double) item.get(sourcePositionYKey);

		double cogx = (Double) item.get(cogxKey);
		double cogy = (Double) item.get(cogyKey);
		
		double distance = Math.sqrt(Math.pow((cogx - sourcex),2) + Math.pow((cogy - sourcey),2));

		item.put(outputKey, distance);
		return item;
	}
}
