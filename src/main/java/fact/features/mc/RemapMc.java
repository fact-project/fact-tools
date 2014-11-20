package fact.features.mc;

import fact.Constants;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class RemapMc implements Processor
{
	@Parameter(required = true, description = "Input: Mc Photon in Pixel")
	private String inkeyMcPhotonPix = null;
	
	@Parameter(required = true, description = "Output: Remapped Mc Photon in Pixel")
	private String outkeyMcPhotonPix = null;
	
	//McCherArrTimeMean -> float
	//McNoisePhotWeight	-> float
	@Parameter(required = true, description = "Input: Mc NoisePhoton in Pixel")
	private String inkeyMcNoisePix = null;
	
	@Parameter(required = true, description = "Output: Remapped Mc NoisePhoton in Pixel")
	private String outkeyMcNoisePix = null;
	
	
	@Override
	public Data process(Data input) 
	{
		float[] mcData = (float[]) input.get(inkeyMcPhotonPix);
		float[] mcNoise = (float[]) input.get(inkeyMcNoisePix);
		
		double[] remappedMcData = new double[1440];
		double[] remappedMcNoise = new double[1440];
		
		
		for(int softId = 0; softId < Constants.NUMBEROFPIXEL; softId++)
		{
	            int chid = FactPixelMapping.getInstance().getChidFromSoftID(softId);
	            remappedMcData[chid] = mcData[softId];
	            remappedMcNoise[chid] = mcNoise[softId];	    	    
		}
		
		input.put(outkeyMcPhotonPix, remappedMcData);
		input.put(outkeyMcNoisePix, remappedMcNoise);
		
		return input;
	}

	public String getInkeyMcPhotonPix() {
		return inkeyMcPhotonPix;
	}

	public void setInkeyMcPhotonPix(String inkeyMcPhotonPix) {
		this.inkeyMcPhotonPix = inkeyMcPhotonPix;
	}

	public String getOutkeyMcPhotonPix() {
		return outkeyMcPhotonPix;
	}

	public void setOutkeyMcPhotonPix(String outkeyMcPhotonPix) {
		this.outkeyMcPhotonPix = outkeyMcPhotonPix;
	}

	public String getInkeyMcNoisePix() {
		return inkeyMcNoisePix;
	}

	public void setInkeyMcNoisePix(String inkeyMcNoisePix) {
		this.inkeyMcNoisePix = inkeyMcNoisePix;
	}

	public String getOutkeyMcNoisePix() {
		return outkeyMcNoisePix;
	}

	public void setOutkeyMcNoisePix(String outkeyMcNoisePix) {
		this.outkeyMcNoisePix = outkeyMcNoisePix;
	}
	
	

}
