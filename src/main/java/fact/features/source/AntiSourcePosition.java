package fact.features.source;

import fact.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

//TODO: Documentation!?

public class AntiSourcePosition implements Processor {
	static Logger log = LoggerFactory.getLogger(AntiSourcePosition.class);
	
	@Parameter(required = false, defaultValue="source:x")
	private String sourcexKey = "source:x";
	@Parameter(required = false, defaultValue="source:y")
	private String sourceyKey = "source:y";
	@Parameter(required = false, defaultValue="5")
	private int numberOfAntiSourcePositions = 5;
	@Parameter(required=true)
	private int antiSourcePositionId;
	@Parameter(required = false, description = "The outputkey for the antiSourcePosition, if not specified antiSources:<id> is used")
	private String outputKey = null;

	@Override
	public Data process(Data item) {
		
		Utils.mapContainsKeys(item, sourcexKey, sourceyKey);
		
		double sourcex = (Double) item.get(sourcexKey);
		double sourcey = (Double) item.get(sourceyKey);
		
		double antisourcex = 0;
		double antisourcey = 0;
		
		double rotAngle = 2 * Math.PI * antiSourcePositionId / (numberOfAntiSourcePositions + 1);
		antisourcex = sourcex * Math.cos(rotAngle) - sourcey * Math.sin(rotAngle);
		antisourcey = sourcex * Math.sin(rotAngle) + sourcey * Math.cos(rotAngle);
		
		if (outputKey == null){
			outputKey = "antiSources:"+ antiSourcePositionId;
		}
				
		item.put(outputKey + ":x", antisourcex);
		item.put(outputKey + ":y", antisourcey);
		return item;
	}
}
