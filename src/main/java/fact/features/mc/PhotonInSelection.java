package fact.features.mc;

import fact.Constants;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PhotonInSelection implements Processor
{
	@Parameter(required = true, description = "Input: Mc Photon in Pixel")
	private String inkeyMcPhoton;
	@Parameter(required = true, description = "Input: Mc Noise in Pixel")
	private String inkeyMcNoise;
	
	@Parameter(required = true, description = "Input: Photon in Pixel")
	private String inkeyShowerPhoton;
	
	@Parameter(required = true, description = "Input: Shower Pixel")
	private String inkeyShower;
	
	@Parameter(required = true, description = "Output: Ratio #All / #Selected")
	private String outkeyRatio;
	
	
	@Override
	public Data process(Data input) 
	{
		
		double[] mc		= (double[])input.get(inkeyMcPhoton);
		double[] noise = (double[])input.get(inkeyMcNoise);
		double[] data	= (double[])input.get(inkeyShowerPhoton);
		int[] shower 	= (int[])input.get(inkeyShower);
		
		
		double sumPhoton1_Mc = 0, sumPhoton2_Mc = 0;
		double sumPhoton1_Data = 0, sumPhoton2_Data = 0;

		double sumNoise1_Mc = 0, sumNoise2_Mc = 0;
		
		
		for(int i=0; i<shower.length; i++)
		{
			sumPhoton1_Mc += mc[shower[i]];
			sumPhoton1_Data += data[shower[i]];
			
			sumNoise1_Mc += noise[shower[i]];
		}
		for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
		{
			sumPhoton2_Mc += mc[i];
			sumPhoton2_Data += data[i];
			
			sumNoise2_Mc += noise[i];
		}
		
				
		double ratioMc = sumPhoton1_Mc / sumPhoton2_Mc;
		double ratioData = sumPhoton1_Data / sumPhoton2_Data;
		
		final double factor = 300.0 / 2200.0;
		
		double ratioNoise1 = sumNoise1_Mc / sumNoise2_Mc;
		double ratioNoise2 = sumPhoton1_Mc / (sumNoise1_Mc*factor);
		
		
		
		input.put(outkeyRatio + "_Mc", ratioMc);
		input.put(outkeyRatio + "_Data", ratioData);
		input.put(outkeyRatio + "_Noise", ratioNoise1);
		input.put(outkeyRatio + "_NoiseShower", ratioNoise2);
		
		return input;
	}


	public String getInkeyMcPhoton() {
		return inkeyMcPhoton;
	}


	public void setInkeyMcPhoton(String inkeyMcPhoton) {
		this.inkeyMcPhoton = inkeyMcPhoton;
	}


	public String getInkeyShowerPhoton() {
		return inkeyShowerPhoton;
	}


	public void setInkeyShowerPhoton(String inkeyShowerPhoton) {
		this.inkeyShowerPhoton = inkeyShowerPhoton;
	}


	public String getInkeyShower() {
		return inkeyShower;
	}


	public void setInkeyShower(String inkeyShower) {
		this.inkeyShower = inkeyShower;
	}


	public String getOutkeyRatio() {
		return outkeyRatio;
	}


	public void setOutkeyRatio(String outkeyRatio) {
		this.outkeyRatio = outkeyRatio;
	}


	public String getInkeyMcNoise() {
		return inkeyMcNoise;
	}


	public void setInkeyMcNoise(String inkeyMcNoise) {
		this.inkeyMcNoise = inkeyMcNoise;
	}
	
	
	
}
