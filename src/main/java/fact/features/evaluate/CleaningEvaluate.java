package fact.features.evaluate;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;

import java.util.ArrayList;

public class CleaningEvaluate implements Processor {

	String showerKey = null;
	String mcCherenkovWeightKey = null;
	String mcNoiseWeightKey = null;
	String outputKey = null;
	int NumberOfSimulatedSlices = 2430; // Be aware that this is not the region of interest which was digitized, but the simulated region in ceres
	int integrationWindow = 30;
	double mcShowerThreshold = 2.0;
	
	
	
	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(getClass(), input, showerKey, mcCherenkovWeightKey, mcNoiseWeightKey);
		
		int[] shower 	= (int[])input.get(showerKey);
		double[] cherenkovWeight = Utils.toDoubleArray(input.get(mcCherenkovWeightKey));
		double[] noiseWeight = Utils.toDoubleArray(input.get(mcNoiseWeightKey));
		
		ArrayList<Integer> correctIdentifiedShowerPixel = new ArrayList<Integer>();
		ArrayList<Integer> wrongIdentifiedShowerPixel = new ArrayList<Integer>();
		ArrayList<Integer> notIdentifiedShowerPixel = new ArrayList<Integer>();
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			double cherSignalOverNoise = cherenkovWeight[px] / (noiseWeight[px] *integrationWindow / NumberOfSimulatedSlices);
			if (cherSignalOverNoise > mcShowerThreshold)
			{
				notIdentifiedShowerPixel.add(px);
			}
		}
		
		for (int i=0 ; i < shower.length ; i++)
		{
			int sh_px = shower[i];
			double cherSignalOverNoise = cherenkovWeight[sh_px] / (noiseWeight[sh_px] *integrationWindow / NumberOfSimulatedSlices);
			if (cherSignalOverNoise > mcShowerThreshold)
			{
				correctIdentifiedShowerPixel.add(sh_px);
				notIdentifiedShowerPixel.remove(sh_px);
			}
			else
			{
				wrongIdentifiedShowerPixel.add(sh_px);
			}
		}
		
		input.put(outputKey+"_correct", correctIdentifiedShowerPixel);
		input.put(outputKey+"_Numbercorrect", correctIdentifiedShowerPixel.size());
		input.put(outputKey+"_wrong", wrongIdentifiedShowerPixel);
		input.put(outputKey+"_Numberwrong", wrongIdentifiedShowerPixel.size());
		input.put(outputKey+"_not", notIdentifiedShowerPixel);
		input.put(outputKey+"_Numbernot", notIdentifiedShowerPixel.size());
		
		// TODO Auto-generated method stub
		return input;
	}



	public String getShowerKey() {
		return showerKey;
	}



	public void setShowerKey(String showerKey) {
		this.showerKey = showerKey;
	}



	public String getMcCherenkovWeightKey() {
		return mcCherenkovWeightKey;
	}



	public void setMcCherenkovWeightKey(String mcCherenkovWeightKey) {
		this.mcCherenkovWeightKey = mcCherenkovWeightKey;
	}



	public String getMcNoiseWeightKey() {
		return mcNoiseWeightKey;
	}



	public void setMcNoiseWeightKey(String mcNoiseWeightKey) {
		this.mcNoiseWeightKey = mcNoiseWeightKey;
	}



	public String getOutputKey() {
		return outputKey;
	}



	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}



	public int getNumberOfSimulatedSlices() {
		return NumberOfSimulatedSlices;
	}



	public void setNumberOfSimulatedSlices(int numberOfSimulatedSlices) {
		NumberOfSimulatedSlices = numberOfSimulatedSlices;
	}



	public int getIntegrationWindow() {
		return integrationWindow;
	}



	public void setIntegrationWindow(int integrationWindow) {
		this.integrationWindow = integrationWindow;
	}



	public double getMcShowerThreshold() {
		return mcShowerThreshold;
	}



	public void setMcShowerThreshold(double mcShowerThreshold) {
		this.mcShowerThreshold = mcShowerThreshold;
	}

}
