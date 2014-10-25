package fact.cleaning.snake;

import fact.Constants;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class CreateGaus implements Processor
{
	@Parameter(required = false, description = "SigmaX ")
	private double sigmaX = 20.0;
	@Parameter(required = false, description = "SigmaX ")
	private double sigmaY = 20.0;
	@Parameter(required = false, description = "Amplitude ")
	private double Amp = 15;		
	
	@Parameter(required = false, description = "CenterX ")
	private double x0 = 0;
	@Parameter(required = false, description = "CenterX ")
	private double y0 = 0;
	
	@Parameter(required = true, description = "Output: Name of the created image")
	private String outputKey = null;

	@Override
	public Data process(Data input) 
	{		
		FactPixelMapping PixelMapping_  = FactPixelMapping.getInstance();		
		
		int NumberOfSlices = 1;
		int timeMax = 0;
		
		double[] erg = new double[Constants.NUMBEROFPIXEL * NumberOfSlices];
		
		for(int frame = 0; frame < NumberOfSlices; frame++)
		{
			for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
			{
				double x = PixelMapping_.getPixelFromId(i).getXPositionInMM();
				double y = PixelMapping_.getPixelFromId(i).getYPositionInMM();
				
				double AmpMod = Amp * Math.exp(- (((frame-timeMax)*(frame-timeMax))/(2.0*16.0)));

				erg[i + frame*Constants.NUMBEROFPIXEL] = AmpMod * Math.exp(- (((x-x0)*(x-x0))/(2*sigmaX*sigmaX)) - (((y-y0)*(y-y0))/(2*sigmaY*sigmaY)));		
				
			}
		}
		
		input.put(outputKey, erg);
		return input;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outkey) {
		this.outputKey = outkey;
	}

	public void setSigmaX(double sigmaX) {
		this.sigmaX = sigmaX;
	}

	public void setSigmaY(double sigmaY) {
		this.sigmaY = sigmaY;
	}

	public void setAmp(double amp) {
		Amp = amp;
	}

	public void setX0(double x0) {
		this.x0 = x0;
	}

	public void setY0(double y0) {
		this.y0 = y0;
	}	
}
