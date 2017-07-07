package fact.features.source;

import fact.coordinates.CameraCoordinate;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

//TODO: Documentation!?

public class AntiSourcePosition implements Processor {
	static Logger log = LoggerFactory.getLogger(AntiSourcePosition.class);
	@Parameter(required=true)
	private String sourcePositionKey = null;
	@Parameter(required=true)
	private int numberOfAntiSourcePositions;
	@Parameter(required=true)
	private int antiSourcePositionId;
	@Parameter(required=true)
	private String outputKey = null;

	@Override
	public Data process(Data input) {
		if(sourcePositionKey != null && !input.containsKey(sourcePositionKey)){
			log.error("No source position in data item. Can not calculate anti source position!");
			throw new RuntimeException("Missing parameter. Enter valid sourcePositionKey");
		}
		CameraCoordinate source  = (CameraCoordinate) input.get(sourcePositionKey);

		double rotAngle = 2 * Math.PI * antiSourcePositionId / (numberOfAntiSourcePositions + 1);

		CameraCoordinate antiSource = new CameraCoordinate(
			source.xMM * Math.cos(rotAngle) - source.yMM * Math.sin(rotAngle),
			source.xMM * Math.sin(rotAngle) + source.yMM * Math.cos(rotAngle)
		);

		input.put("@Source" + outputKey, new SourcePositionOverlay(outputKey, antiSource));
		input.put(outputKey, antiSource);
		input.put(outputKey + "_x", antiSource.xMM);
		input.put(outputKey + "_y", antiSource.yMM);
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
