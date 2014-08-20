package fact.features.source;

import fact.Utils;
import stream.Data;
import stream.Processor;

public class Theta implements Processor {
	
	private String sourcePositionKey = null;
	private String dispKey = null;
	private String cogxKey = null;
	private String cogyKey = null;
	private String deltaKey = null;
	private String m3longKey = null;
	
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
		
		input.put(outputKey, theta);
		
		return input;
	}

	private double[] CalculateRecPosition() {
		
		double[] result = new double[2];
		
		result[0] = cogx + disp * Math.cos(delta) * Math.signum(m3long);
		result[1] = cogy + disp * Math.sin(delta) * Math.signum(m3long);
		
		return result;
	}

}
