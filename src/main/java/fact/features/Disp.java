package fact.features;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Disp implements Processor {
	@Parameter(required=false, description = "Key to width")
	private String widthKey = "shower:ellipse:width";
	@Parameter(required=false, description = "Key to length")
	private String lengthKey = "shower:ellipse:length";
	@Parameter(required=false, defaultValue="117.94")
	private double c0 = 117.94;
	@Parameter(required=false)
	private String outputKey = "shower:disp";

	public Data process(Data input) {
		
		Utils.mapContainsKeys(input, widthKey,lengthKey);
		
		double width = (Double) input.get(widthKey);
		double length = (Double) input.get(lengthKey);

		double disp = CalculateDisp(length, width);
		
		input.put(outputKey, disp);
		
		return input;
	}
	
	private double CalculateDisp(double width, double length)
	{
		double disp = c0 * (1 - width / length);
		return disp;
	}
}
