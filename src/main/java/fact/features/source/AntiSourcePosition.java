package fact.features.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;

//TODO: Documentation!?

public class AntiSourcePosition implements Processor {
	static Logger log = LoggerFactory.getLogger(AntiSourcePosition.class);
	
	private String sourcePositionKey = null;
	
	private int numberOfAntiSourcePositions;
	private int antiSourcePositionId;
	
	private String outputKey = null;

	@Override
	public Data process(Data input) {
		if(sourcePositionKey != null && !input.containsKey(sourcePositionKey)){
			log.error("No source position in data item. Can not calculate anti source position!");
			throw new RuntimeException("Missing parameter. Enter valid sourcePositionKey");
		}
		double[] source  = (double[]) input.get(sourcePositionKey);
		
		double[] antisource = {0,0};
		
		double rotAngle = 2 * Math.PI * antiSourcePositionId / (numberOfAntiSourcePositions + 1);
		antisource[0] = source[0] * Math.cos(rotAngle) - source[1] * Math.sin(rotAngle);
		antisource[1] = source[0] * Math.sin(rotAngle) + source[1] * Math.cos(rotAngle);
		
		//input.put("AntiSourcePosition_"+String.valueOf(antiSourcePositionId), new SourceOverlay((float) antisource[0], (float) antisource[1]) );
		
		input.put(outputKey, antisource);
		return input;
	}

	public String getSourcePositionKey() {
		return sourcePositionKey;
	}

	public void setSourcePositionKey(String sourcePositionKey) {
		this.sourcePositionKey = sourcePositionKey;
	}

	public int getNumberOfAntiSourcePositions() {
		return numberOfAntiSourcePositions;
	}

	public void setNumberOfAntiSourcePositions(int numberOfAntiSourcePositions) {
		this.numberOfAntiSourcePositions = numberOfAntiSourcePositions;
	}

	public int getAntiSourcePositionId() {
		return antiSourcePositionId;
	}

	public void setAntiSourcePositionId(int antiSourcePositionId) {
		this.antiSourcePositionId = antiSourcePositionId;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	
}
