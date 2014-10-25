package fact.features.snake.video;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class SimpleThresholdCleaning implements Processor
{
	@Parameter(required = true, description = "Input: Slice data")
	private String inputKey = null;
	
	@Parameter(required = true, description = "Output: List of Pixel over Threshold")
	private String outputKey = null;
	
	@Parameter(required = false, description = "Input: Threshold for main Pixel")
	private double thresholdLv1 = 35;
	@Parameter(required = false, description = "Input: Threshold for neighbor Pixel")
	private double thresholdLv2 = 15;
	
		
	@Override
	public Data process(Data input) 
	{
		Utils.isKeyValid(input, inputKey, double[].class);		
		double[] data = (double[]) input.get(inputKey);
		
		int[] marks = new int[Constants.NUMBEROFPIXEL];
		int elementCounter = 0;
		
		FactPixelMapping PixelMapping_  = FactPixelMapping.getInstance();
		
		
		for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
		{
			if(data[i] > thresholdLv1)
			{
				FactCameraPixel[] neighbour = PixelMapping_.getNeighboursFromID(i);
				int thresholdViolationCounter = 0;
				
				for(int j=0; j<neighbour.length; j++)
				{
					if(data[neighbour[j].chid] < thresholdLv2) thresholdViolationCounter++;
				}
				
				if(thresholdViolationCounter <= 2)
				{
					marks[i] = 1;
					elementCounter++;
				}
			}			
		}
		
		int[] retArray = new int[elementCounter];
		int count = 0;
		for(int i=0; i<1440; i++)
		{
			if(marks[i] == 1)
			{
				retArray[count] = i;
				count++;
			}
		}
		
		input.put(outputKey, retArray);		
		return input;
	}

	public String getInputKey() {
		return inputKey;
	}

	public void setInputKey(String inputKey) {
		this.inputKey = inputKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public double getThresholdLv1() {
		return thresholdLv1;
	}

	public void setThresholdLv1(double thresholdLv1) {
		this.thresholdLv1 = thresholdLv1;
	}

	public double getThresholdLv2() {
		return thresholdLv2;
	}

	public void setThresholdLv2(double thresholdLv2) {
		this.thresholdLv2 = thresholdLv2;
	}
	
	
}
