package fact.features.source;

import fact.Utils;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Theta implements Processor {
	@Parameter(required=true)
	private String sourcePositionKey = null;
	@Parameter(required=true)
	private String dispKey = null;
	@Parameter(required=true)
	private String cogxKey = null;
	@Parameter(required=true)
	private String cogyKey = null;
	@Parameter(required=true)
	private String deltaKey = null;
	@Parameter(required=true)
	private String m3longKey = null;
	@Parameter(required=true)
	private String outputKey = null;
	
	private double[] sourcePosition = null;
	private double disp;
	private double cogx;
	private double cogy;
	private double delta;
	private double m3long;

	public Data process(Data input) {
		Utils.mapContainsKeys(input, sourcePositionKey,dispKey,cogxKey,cogyKey,deltaKey);
		
		sourcePosition = (double[]) input.get(sourcePositionKey);
		disp = (Double) input.get(dispKey);
		cogx = (Double) input.get(cogxKey);
		cogy = (Double) input.get(cogyKey);
		delta = (Double) input.get(deltaKey);
		m3long = (Double) input.get(m3longKey);
		
		double[] recPosition = CalculateRecPosition();
		double theta = Math.sqrt( Math.pow(recPosition[0]-sourcePosition[0], 2)
								+ Math.pow(recPosition[1]-sourcePosition[1], 2) );

        input.put("@reconstructedPostion", new SourcePositionOverlay(outputKey, recPosition));
        input.put(outputKey + "_recPos",  recPosition);
        input.put(outputKey, theta);
		
		return input;
	}

	private double[] CalculateRecPosition() {
		
		double[] result = new double[2];
		
		result[0] = cogx + disp * Math.cos(delta) * Math.signum(-m3long);
		result[1] = cogy + disp * Math.sin(delta) * Math.signum(-m3long);
		
		return result;
	}

	public String getSourcePositionKey() {
		return sourcePositionKey;
	}

	public void setSourcePositionKey(String sourcePositionKey) {
		this.sourcePositionKey = sourcePositionKey;
	}

	public String getDispKey() {
		return dispKey;
	}

	public void setDispKey(String dispKey) {
		this.dispKey = dispKey;
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

	public String getM3longKey() {
		return m3longKey;
	}

	public void setM3longKey(String m3longKey) {
		this.m3longKey = m3longKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

}
