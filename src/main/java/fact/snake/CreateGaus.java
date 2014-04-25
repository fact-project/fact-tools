package fact.snake;

import fact.Constants;
import fact.viewer.ui.DefaultPixelMapping;
import stream.Data;
import stream.Processor;

public class CreateGaus implements Processor
{
	String key = null;

	@Override
	public Data process(Data input) 
	{
		double sigmaX = 20.0;
		double sigmaY = 20.0;
		double Amp = 15;		
		
		double x0 = 0;
		double y0 = 0;
		
		int NumberOfSlices = 1;
		int timeMax = 0;
		
		double[] erg = new double[Constants.NUMBEROFPIXEL * NumberOfSlices];
		
		for(int frame = 0; frame < NumberOfSlices; frame++)
		{
			for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
			{
				double x = DefaultPixelMapping.getPosXinMM(i);
				double y = DefaultPixelMapping.getPosYinMM(i);
				
				double AmpMod = Amp * Math.exp(- (((frame-timeMax)*(frame-timeMax))/(2.0*16.0)));

				erg[i + frame*Constants.NUMBEROFPIXEL] = AmpMod * Math.exp(- (((x-x0)*(x-x0))/(2*sigmaX*sigmaX)) - (((y-y0)*(y-y0))/(2*sigmaY*sigmaY)));		
				
			}
		}
		
		input.put(key, erg);
		return input;
	}

	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
}
