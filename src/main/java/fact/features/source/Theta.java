package fact.features.source;

import fact.Utils;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Theta implements Processor {
	@Parameter(required = false, defaultValue="source:x")
	private String sourcexKey = "source:x";
	@Parameter(required = false, defaultValue="source:y")
	private String sourceyKey = "source:y";
	@Parameter(required = false, defaultValue="shower:disp")
	private String dispKey = "shower:disp";
	@Parameter(required = false, defaultValue = "shower:ellipse:cog:x")
	private String cogxKey = "shower:ellipse:cog:x";
	@Parameter(required = false, defaultValue = "shower:ellipse:cog:y")
	private String cogyKey = "shower:ellipse:cog:y";
	@Parameter(required = false, defaultValue = "shower:ellipse:delta")
	private String deltaKey = "shower:ellipse:delta";
	@Parameter(required = false, defaultValue = "shower:ellipse:m3l")
	private String m3lKey = "shower:ellipse:m3l";
	@Parameter(required = false, defaultValue = "shower:ellipse:delta")
	private String cosDeltaAlphaKey = "shower:source:cosDeltaAlpha";
	@Parameter(required = false, defaultValue = "shower:source:theta")
	private String outputKey = "shower:source:theta";
	@Parameter(required=false, defaultValue = "-200")
	private double signM3lConstant = -200;
	
	public Data process(Data item) {
		Utils.mapContainsKeys(item, sourcexKey, sourceyKey, dispKey, cogxKey, cogyKey, deltaKey, m3lKey, cosDeltaAlphaKey);
		
		double sourcex = (Double) item.get(sourcexKey);
		double sourcey = (Double) item.get(sourceyKey);
		double disp = (Double) item.get(dispKey);
		double cogx = (Double) item.get(cogxKey);
		double cogy = (Double) item.get(cogyKey);
		double delta = (Double) item.get(deltaKey);
		double m3l = (Double) item.get(m3lKey);
		double cosDeltaAlpha = (Double) item.get(cosDeltaAlphaKey);
		
		double[] recPosition = CalculateRecPosition(cogx, cogy, disp, delta, m3l, cosDeltaAlpha);
		double theta = Math.sqrt( Math.pow(recPosition[0]-sourcex, 2)
								+ Math.pow(recPosition[1]-sourcey, 2) );

        item.put("gui:sourceOverlay:reconstructedPosition:" + outputKey, new SourcePositionOverlay("gui:sourceOverlay:reconstructedPosition:" + outputKey, recPosition));
        item.put(outputKey + ":recPos:x",  recPosition[0]);
        item.put(outputKey + ":recPos:y",  recPosition[1]);
        item.put(outputKey, theta);
		
		return item;
	}

	private double[] CalculateRecPosition(double cogx, double cogy, double disp, double delta, double m3l, double cosDeltaAlpha) {
		
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
}
