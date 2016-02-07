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
	private String m3lKey = null;
	@Parameter(required=true)
	private String cosDeltaAlphaKey = null;
	@Parameter(required=true)
	private String outputKey = null;
	@Parameter(required=true)
	private double signM3lConstant = 0;
	
	private double[] sourcePosition = null;
	private double disp;
	private double cogx;
	private double cogy;
	private double delta;
	private double m3l;
	private double cosDeltaAlpha;

	public Data process(Data input) {
		Utils.mapContainsKeys(input,sourcePositionKey,dispKey,cogxKey,cogyKey,deltaKey,cosDeltaAlphaKey);
		
		sourcePosition = (double[]) input.get(sourcePositionKey);
		disp = (Double) input.get(dispKey);
		cogx = (Double) input.get(cogxKey);
		cogy = (Double) input.get(cogyKey);
		delta = (Double) input.get(deltaKey);
		m3l = (Double) input.get(m3lKey);
		cosDeltaAlpha = (Double) input.get(cosDeltaAlphaKey);
		
		double[] recPosition = CalculateRecPosition();
		double theta = Math.sqrt( Math.pow(recPosition[0]-sourcePosition[0], 2)
								+ Math.pow(recPosition[1]-sourcePosition[1], 2) );

        input.put("@reconstructedPostion" + outputKey, new SourcePositionOverlay(outputKey, recPosition));
        input.put(outputKey + "_recPos",  recPosition);
        input.put(outputKey, theta);
		
		return input;
	}

	private double[] CalculateRecPosition() {
		
		double[] result = new double[2];
		
		// The orientation of the reconstructed source position depends on the third moment
		// (relativ to the suspected source position, m3l*sign(cosDeltaAlpha)) of the shower:
		// If it is larger than a constant (default -200) the reconstructed source position is 
		// orientated towards the suspected source position
		double sign = - Math.signum(cosDeltaAlpha) * Math.signum(m3l*Math.signum(cosDeltaAlpha)-signM3lConstant);
		
		result[0] = cogx + disp * Math.cos(delta) * sign;
		result[1] = cogy + disp * Math.sin(delta) * sign;
		
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


	public String getM3lKey() {
		return m3lKey;
	}

	public void setM3lKey(String m3lKey) {
		this.m3lKey = m3lKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public String getCosDeltaAlphaKey() {
		return cosDeltaAlphaKey;
	}

	public void setCosDeltaAlphaKey(String cosDeltaAlphaKey) {
		this.cosDeltaAlphaKey = cosDeltaAlphaKey;
	}

	public double getSignM3lConstant() {
		return signM3lConstant;
	}

	public void setSignM3lConstant(double signM3lConstant) {
		this.signM3lConstant = signM3lConstant;
	}


}
