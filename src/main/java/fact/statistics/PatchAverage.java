package fact.statistics;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import stream.Data;
import stream.Processor;

public class PatchAverage implements Processor {
	static Logger log = LoggerFactory.getLogger(PatchAverage.class);
	
	String key=null;
	String outputKey=null;
	
	String color = null;
	
	int numberOfPatches = Constants.NUMBEROFPIXEL / 9;
	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		Utils.mapContainsKeys(this.getClass(), input, key);
		
		double[] data = (double[])input.get(key);
		double[] result = new double[data.length];
		
		int currentRoi = data.length / Constants.NUMBEROFPIXEL;
		
		for (int patch = 0 ; patch < numberOfPatches ; patch++)
		{
			for (int sl = 0 ; sl < currentRoi ; sl++)
			{
				for (int px = 0 ; px < 8 ; px++)
				{
					int slice = (patch*9+px)*currentRoi+sl;
					result[patch*9*currentRoi+sl] += data[slice];
				}
				result[patch*9*currentRoi+sl] /= 8;
				for (int px = 0 ; px < 9 ; px++)
				{
					int slice = (patch*9+px)*currentRoi+sl;
					result[slice] = result[patch*9*currentRoi+sl];
				}
			}
		}
		input.put(outputKey,result);
		input.put("@"+Constants.KEY_COLOR + "_" +outputKey,color);
		
		return input;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	public int getNumberOfPatches() {
		return numberOfPatches;
	}
	public void setNumberOfPatches(int numberOfPatches) {
		this.numberOfPatches = numberOfPatches;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}

}
