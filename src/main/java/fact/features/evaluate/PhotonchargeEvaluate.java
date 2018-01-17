package fact.features.evaluate;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PhotonchargeEvaluate implements Processor {
    static Logger log = LoggerFactory.getLogger(PhotonchargeEvaluate.class);

    @Parameter(required = true)
    public String photonchargeKey = null;

    @Parameter(required = true)
    public String arrivalTimeKey = null;

    @Parameter(required = true)
    public  String mcCherenkovWeightKey = null;

    @Parameter(required = true)
    public String mcCherenkovArrTimeMeanKey = null;

    @Parameter(required = true)
    public String mcNoiseWeightKey = null;

    @Parameter(required = true)
    public String outputKeyPhotonCharge = null;

    @Parameter(required = true)
    public String outputKeyArrivalTime = null;


    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, photonchargeKey, mcCherenkovWeightKey, mcNoiseWeightKey, mcCherenkovArrTimeMeanKey, arrivalTimeKey);

        double[] qualityFactorPhotoncharge = new double[Constants.N_PIXELS];
        double[] qualityFactorArrivalTime = new double[Constants.N_PIXELS];

        double[] photoncharge = Utils.toDoubleArray(input.get(photonchargeKey));
        double[] arrivalTime = Utils.toDoubleArray(input.get(arrivalTimeKey));
        double[] cherenkovWeight = Utils.toDoubleArray(input.get(mcCherenkovWeightKey));
        double[] cherenkovArrTimeMean = Utils.toDoubleArray(input.get(mcCherenkovArrTimeMeanKey));

        for (int px = 0; px < Constants.N_PIXELS; px++) {
            qualityFactorPhotoncharge[px] = photoncharge[px] / cherenkovWeight[px];
            qualityFactorArrivalTime[px] = arrivalTime[px] / cherenkovArrTimeMean[px];
        }

        input.put(outputKeyPhotonCharge, qualityFactorPhotoncharge);
        input.put(outputKeyArrivalTime, qualityFactorArrivalTime);

        return input;
    }
}
