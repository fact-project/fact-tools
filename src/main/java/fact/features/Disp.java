package fact.features;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Disp implements Processor {
	@Parameter(required=true)
	private String widthKey = null;
	@Parameter(required=true)
	private String lengthKey = null;

//	private String slopeKey = null;
//	
//	private String leakageKey = null;
//	
//	private String sizeKey = null;
	@Parameter(required=true, defaultValue="117.94")
	private double c0 = 117.94;
//	private double c1 = 117.94;
//	private double c2 = 117.94;
//	private double c3 = 117.94;	
//	private double c4 = 117.94;
	@Parameter(required=true)
	private String outputKey = null;
	
	private double width;
	private double length;
//	private double slope;
//	private double leakage;
//	private double size;
	

	public Data process(Data input) {
		
		Utils.mapContainsKeys(input, widthKey,lengthKey);
		
		width = (Double) input.get(widthKey);
		length = (Double) input.get(lengthKey);
//		slope = (Double) input.get(slopeKey);
//		leakage = (Double) input.get(leakageKey);
//		size = (Double) input.get(sizeKey);
		
		double disp = CalculateDisp();
		
		input.put(outputKey, disp);
		
		return input;
	}
	
	private double CalculateDisp()
	{
		double disp = c0 * (1 - width / length);
		
//		int k = 0;
//		if (Math.log10(size) >= c4)
//		{
//			k = 1;
//		}
		
//		double disp = (c0 + c1*slope + c2*leakage+k*c3*Math.pow(Math.log10(size)-c4,2)) * (1 - width / length);
		
		return disp;	
	}

	public String getWidthKey() {
		return widthKey;
	}

	public void setWidthKey(String widthKey) {
		this.widthKey = widthKey;
	}

	public String getLengthKey() {
		return lengthKey;
	}

	public void setLengthKey(String lengthKey) {
		this.lengthKey = lengthKey;
	}

	public double getC0() {
		return c0;
	}

	public void setC0(double c0) {
		this.c0 = c0;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

}
