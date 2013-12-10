package fact.features;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;

public class NaiveShowerLineFit implements Processor {
	@Override
	public Data process(Data input)
	{
		EventUtils.mapContainsKeys(getClass(), input, showerPixel, hillasDelta);
		showerPixelArray = (int[]) input.get(showerPixel);
		hillasDeltaValue = (Double) input.get(hillasDelta);
		SimpleRegression reg = new SimpleRegression();
		for(int chid : showerPixelArray)
		{
			double[] pos = DefaultPixelMapping.rotate(chid, -hillasDeltaValue);
			
			reg.addData(pos[0],pos[1]);
		}
		
		input.put(outputKey + "_Intercept", reg.getIntercept());
		input.put(outputKey + "_InterceptStdErr", reg.getInterceptStdErr());
		input.put(outputKey + "_Slope", reg.getSlope());
		input.put(outputKey + "_SlopeStdErr", reg.getSlopeStdErr());
		input.put(outputKey + "_SlopeRelErr", reg.getSlopeStdErr() / reg.getSlope());
		input.put(outputKey + "_RSquare", reg.getRSquare());
		input.put(outputKey + "_RegressionSumSquares", reg.getRegressionSumSquares());
		
		return input;
	}
	
	public String getShowerPixel() {
		return showerPixel;
	}
	@Parameter(required = true, defaultValue="showerpixel", description="Key to the showerpixel array.")
	public void setShowerPixel(String showerPixel) {
		this.showerPixel = showerPixel;
	}
	public String getHillasDelta() {
		return hillasDelta;
	}
	@Parameter(required = true, defaultValue="hillasDelta", description="Key to the Hillas delta angle.")
	public void setHillasDelta(String hillasDelta) {
		this.hillasDelta = hillasDelta;
	}

	public String getOutputKey() {
		return outputKey;
	}
	@Parameter(required=true, defaultValue="NaiveShowerLineFit", description="Master output key.")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	private int[] showerPixelArray;
	private Double hillasDeltaValue;
	private String showerPixel;
	private String outputKey;
	private String hillasDelta;
	
}
