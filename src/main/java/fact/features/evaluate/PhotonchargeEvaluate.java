package fact.features.evaluate;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;

public class PhotonchargeEvaluate implements Processor {
    static Logger log = LoggerFactory.getLogger(PhotonchargeEvaluate.class);

    public String photonchargeKey = null;
    public String arrivalTimeKey = null;
    public  String mcCherenkovWeightKey = null;
    public String mcCherenkovArrTimeMeanKey = null;
    public String mcNoiseWeightKey = null;
    public String outputKeyPhotonCharge = null;
    public String outputKeyArrivalTime = null;
//	int NumberOfSimulatedSlices = 2430; // Be aware that this is not the region of interest which was digitized, but the simulated region in ceres
//	int integrationWindow = 30;

    double[] photoncharge = null;
    double[] arrivalTime = null;
    double[] cherenkovWeight = null;
    double[] cherenkovArrTimeMean = null;
    double[] noiseWeight = null;

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        Utils.mapContainsKeys(input, photonchargeKey, mcCherenkovWeightKey, mcNoiseWeightKey, mcCherenkovArrTimeMeanKey, arrivalTimeKey);

        double[] qualityFactorPhotoncharge = new double[npix];
        double[] qualityFactorArrivalTime = new double[npix];

        photoncharge = Utils.toDoubleArray(input.get(photonchargeKey));
        arrivalTime = Utils.toDoubleArray(input.get(arrivalTimeKey));
        cherenkovWeight = Utils.toDoubleArray(input.get(mcCherenkovWeightKey));
        cherenkovArrTimeMean = Utils.toDoubleArray(input.get(mcCherenkovArrTimeMeanKey));
        noiseWeight = Utils.toDoubleArray(input.get(mcNoiseWeightKey));

        for (int px = 0; px < npix; px++) {
            qualityFactorPhotoncharge[px] = photoncharge[px] / cherenkovWeight[px];
            qualityFactorArrivalTime[px] = arrivalTime[px] / cherenkovArrTimeMean[px];
        }

        input.put(outputKeyPhotonCharge, qualityFactorPhotoncharge);
        input.put(outputKeyArrivalTime, qualityFactorArrivalTime);

        // TODO Auto-generated method stub
        return input;
    }
}
