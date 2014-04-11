package fact.features.evaluate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.EventUtils;
import fact.utils.RemappingKeys;

public class PhotonchargeEvaluate implements Processor {
	static Logger log = LoggerFactory.getLogger(RemappingKeys.class);
	
	String photonchargeKey= null;
	String mcCherenkovWeightKey = null;
	String mcNoiseWeightKey = null;
	String outputKey = null;
	int NumberOfSimulatedSlices = 2430; // Be aware that this is not the region of interest which was digitized, but the simulated region in ceres
	int integrationWindow = 30;
	
	double[] photoncharge = null;
	double[] cherenkovWeight = null;
	double[] noiseWeight = null;
	
	double[] qualityFactor = new double[Constants.NUMBEROFPIXEL];
//	double[] qualityFactor = new double[30];

	@Override
	public Data process(Data input) {
		
		EventUtils.mapContainsKeys(PhotonchargeEvaluate.class, input, photonchargeKey,mcCherenkovWeightKey,mcNoiseWeightKey);
		
		photoncharge = EventUtils.toDoubleArray(input.get(photonchargeKey));
		cherenkovWeight = EventUtils.toDoubleArray(input.get(mcCherenkovWeightKey));
		noiseWeight = EventUtils.toDoubleArray(input.get(mcNoiseWeightKey));
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
//		for (int px = 0 ; px < 30 ; px++)
		{
//			log.info("Pixel: " + px + " PhCh: " + photoncharge[px] + " ChWe: "+ cherenkovWeight[px] + " NoWe_perWin: " + noiseWeight[px]* integrationWindow / NumberOfSimulatedSlices);
			double current_truth = cherenkovWeight[px] + noiseWeight[px] * integrationWindow / NumberOfSimulatedSlices;
			qualityFactor[px] = photoncharge[px] - current_truth / photoncharge[px];
		}
		
		input.put(outputKey, qualityFactor);

		// TODO Auto-generated method stub
		return input;
	}

	public String getPhotonchargeKey() {
		return photonchargeKey;
	}

	public void setPhotonchargeKey(String photonchargeKey) {
		this.photonchargeKey = photonchargeKey;
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

}
