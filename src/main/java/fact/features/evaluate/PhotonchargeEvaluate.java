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
	static Logger log = LoggerFactory.getLogger(PhotonchargeEvaluate.class);
	
	String photonchargeKey= null;
	String arrivalTimeKey = null;
	String mcCherenkovWeightKey = null;
	String mcCherenkovArrTimeMeanKey = null;
	String mcNoiseWeightKey = null;
	String outputKeyPhotonCharge = null;
	String outputKeyArrivalTime = null;
//	int NumberOfSimulatedSlices = 2430; // Be aware that this is not the region of interest which was digitized, but the simulated region in ceres
//	int integrationWindow = 30;
	
	double[] photoncharge = null;
	double[] arrivalTime = null;
	double[] cherenkovWeight = null;
	double[] cherenkovArrTimeMean = null;
	double[] noiseWeight = null;
	
	double[] qualityFactorPhotoncharge = new double[Constants.NUMBEROFPIXEL];
	double[] qualityFactorArrivalTime = new double[Constants.NUMBEROFPIXEL];

	@Override
	public Data process(Data input) {
		
		EventUtils.mapContainsKeys(PhotonchargeEvaluate.class, input, photonchargeKey,mcCherenkovWeightKey,mcNoiseWeightKey,mcCherenkovArrTimeMeanKey,arrivalTimeKey);
		
		photoncharge = EventUtils.toDoubleArray(input.get(photonchargeKey));
		arrivalTime = EventUtils.toDoubleArray(input.get(arrivalTimeKey));
		cherenkovWeight = EventUtils.toDoubleArray(input.get(mcCherenkovWeightKey));
		cherenkovArrTimeMean = EventUtils.toDoubleArray(input.get(mcCherenkovArrTimeMeanKey));
		noiseWeight = EventUtils.toDoubleArray(input.get(mcNoiseWeightKey));
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			qualityFactorPhotoncharge[px] = (photoncharge[px] - cherenkovWeight[px]) / photoncharge[px];
			if (cherenkovWeight[px] > 2.0)
			{
				System.out.println("Quality: " + qualityFactorPhotoncharge[px] + " phCh: " + photoncharge[px] + " mcphCh: " + cherenkovWeight[px]);
			}
			qualityFactorArrivalTime[px] = (arrivalTime[px] - cherenkovArrTimeMean[px]) / arrivalTime[px];
		}
		
		input.put(outputKeyPhotonCharge, qualityFactorPhotoncharge);
		input.put(outputKeyArrivalTime, qualityFactorArrivalTime);

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

	public String getArrivalTimeKey() {
		return arrivalTimeKey;
	}

	public void setArrivalTimeKey(String arrivalTimeKey) {
		this.arrivalTimeKey = arrivalTimeKey;
	}

	public String getMcCherenkovArrTimeMeanKey() {
		return mcCherenkovArrTimeMeanKey;
	}

	public void setMcCherenkovArrTimeMeanKey(String mcCherenkovArrTimeMeanKey) {
		this.mcCherenkovArrTimeMeanKey = mcCherenkovArrTimeMeanKey;
	}

	public String getOutputKeyPhotonCharge() {
		return outputKeyPhotonCharge;
	}

	public void setOutputKeyPhotonCharge(String outputKeyPhotonCharge) {
		this.outputKeyPhotonCharge = outputKeyPhotonCharge;
	}

	public String getOutputKeyArrivalTime() {
		return outputKeyArrivalTime;
	}

	public void setOutputKeyArrivalTime(String outputKeyArrivalTime) {
		this.outputKeyArrivalTime = outputKeyArrivalTime;
	}

}
