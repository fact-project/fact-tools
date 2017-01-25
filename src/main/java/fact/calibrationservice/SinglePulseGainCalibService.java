package fact.calibrationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import stream.io.CsvStream;
import stream.Data;
import fact.Constants;

/**
 *
 **/
public class SinglePulseGainCalibService implements CalibrationService {

    Logger log = LoggerFactory.getLogger(SinglePulseGainCalibService.class);

    boolean isInit = false;
    public double[] integralSinglePulseGain;

    @Parameter(
        required = false,
        description = "The path to the integral single pulse gain file."
    )
    SourceURL integralGainFile;

    public void init() {
        integralSinglePulseGain = new double[Constants.NUMBEROFPIXEL];
        Data integralGainData = null;
        try {
            CsvStream stream = new CsvStream(integralGainFile, " ");
            stream.setHeader(false);
            stream.init();
            integralGainData = stream.readNext();

            for (int i = 0 ; i < Constants.NUMBEROFPIXEL ; i++){
                String key = "column:" + (i);
                integralSinglePulseGain[i] = (Double) integralGainData.get(key);
            }

        } catch (Exception e) {
            log.error(
                "Failed to load the integral single pulse gain file: {}",
                e.getMessage());
            e.printStackTrace();
        }
    }

    public double[] getIntegralSinglePulseGain() {
        if (isInit == false){
            init();
            isInit = true;
        }
        return integralSinglePulseGain;
    }

    @Override
    public void reset() throws Exception {
    }

    public void setIntegralGainFile(SourceURL integralGainFile) {
        this.integralGainFile = integralGainFile;
    }

    public int[] getBadPixel(DateTime eventTimeStamp) {return new int[0];}

    public int[] getNotUsablePixels(DateTime eventTimeStamp) {return new int[0];}
}