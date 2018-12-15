package fact.pedestalSuperposition;

import fact.Constants;
import fact.Utils;
import fact.gainservice.GainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.time.ZonedDateTime;
import java.util.Arrays;

/**
 * Rescale the amplitudes in a data array from one gain to annother.
 * This allows, e.g., to combine data arrays from data and MCs that have
 * different gains
 *
 * @author Jens Buß
 *
 */
public class rescaleDataArray implements StatefulProcessor {
    private static final Logger log = LoggerFactory.getLogger(rescaleDataArray.class);

    @Parameter(required = true, description = "key to the data array to read")
    public String dataKey = null;

    @Parameter(required = true, description = "key to the data array to store")
    public String outputKey = null;

    @Service(description = "Gain Service that delivers the integral gains")
    public GainService gainService = null;

    @Parameter(required = false, description = "Wheather to scale from data gain to mc" +
            "gain (true) or vice versa (false)", defaultValue = "true")
    public boolean dataToMc = true;

    @Parameter(required = false, description = "The key to the timestamp of the Event.")
    public String timeStampKey = "timestamp";

    private double[] mcGains;

    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, dataKey, "NROI");

        ZonedDateTime timestamp = Utils.getTimeStamp(item, timeStampKey);
        double[] dataGains = gainService.getGains(timestamp);

        int roi = (Integer) item.get("NROI");

        double[] data = (double[]) item.get(dataKey);


        double[] result = rescaleDataArray(data, dataGains, mcGains, dataToMc, roi);

        item.put(outputKey, result);

        return item;
    }

    public static double[] rescaleDataArray(double[] data, double[] dataGains, double[] mcGains, boolean dataToMc, int roi) {
        double[] result = Arrays.copyOf(data, data.length);

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            for (int sl = 0; sl < roi; sl++) {
                int pos = pix*roi+sl;

                // In case of bad pixels the gain calibration value might be 0
                // Don't scale in this case
                if ((dataGains[pix] == 0) || (dataGains[pix] == 0)){
                    continue;
                }


                if (dataToMc){
                    result[pos] /= dataGains[pix];
                    result[pos] *= mcGains[pix];
                } else {
                    result[pos] /= mcGains[pix];
                    result[pos] *= dataGains[pix];
                }
            }
        }
        return result;
    }


    @Override
    public void init(ProcessContext processContext) throws Exception {
        this.mcGains = gainService.getSimulationGains();
        log.warn("Bad pixel cannot be rescaled and "+
                "rescaling is skipped for these pixel. " +
                "Make sure to interpolate the bad pixels");

    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }


}
