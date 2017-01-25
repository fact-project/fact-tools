package fact.photonstream;

import fact.Constants;
import fact.calibrationservice.SinglePulseGainCalibService;
import fact.photonstream.timeSeriesExtraction.AddFirstArrayToSecondArray;
import fact.photonstream.timeSeriesExtraction.TemplatePulse;
import fact.photonstream.timeSeriesExtraction.ElementWise;
import org.apache.commons.lang3.ArrayUtils;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;


/**
 * Created by jebuss on 28.10.16.
 */
public class ConvertSinglePulses2Timeseries implements StatefulProcessor {
    @Parameter(required = true, description = "The arrival slices of the single pulses.")
    private String singlePulsesKey = null;

    @Parameter(required = true, description = "The reconstruted time series.")
    private String timeSeriesKey = null;

    @Parameter(required = false, description = "The region of interest to be reconstructed.")
    private int roi = 300;

    @Parameter(required = false, description = "The reconstructed baseline of the original time series.")
    private String baseLineKey = null;

    @Service(
            required = false,
            description = "The calibration service for the integral single pulse gain"
    )
    SinglePulseGainCalibService gainService = null;
    protected double[] gainCorrection = null;
    private int npix = Constants.NUMBEROFPIXEL;

    @Override
    public Data process(Data input) {

        int[][] singlePulses = (int[][]) input.get(singlePulsesKey);

        double[] baseLine = new double[singlePulses.length];
        if(baseLineKey != null) {
            baseLine = (double[]) input.get(baseLineKey);
        }

        double[] pulseTemplate = TemplatePulse.factSinglePePulse(roi);

        double[] timeSeries = new double[0];

        for (int pix = 0; pix < singlePulses.length; pix++) {

            // create empty time series of length roi
            double[] currentTimeSeries = new double[roi];

            // Add the single pulses to the time series
            for (int pulse = 0; pulse < singlePulses[pix].length; pulse++) {
                AddFirstArrayToSecondArray.at(
                    pulseTemplate,
                    currentTimeSeries,
                    singlePulses[pix][pulse]);
            }

            // Add the baseline to the time series
            currentTimeSeries = ElementWise.add(currentTimeSeries, baseLine[pix]);

            timeSeries = ArrayUtils.addAll(timeSeries, currentTimeSeries);

            timeSeries = ElementWise.multiply(
                    timeSeries,
                    gainCorrection[pix]);
        }

        input.put(timeSeriesKey, timeSeries);

        return input;
    }

    public void setSinglePulsesKey(String singlePulsesKey) {
        this.singlePulsesKey = singlePulsesKey;
    }

    public void settimeSeriesKey(String timeSeriesKey) {
        this.timeSeriesKey = timeSeriesKey;
    }

    public void setBaseLineKey(String baseLineKey) {
        this.baseLineKey = baseLineKey;
    }

    public void setRoi(int roi) {
        this.roi = roi;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {

        double factSinglePePulseIntegral = TemplatePulse.factSinglePePulseIntegral();

        if(gainService == null) {
            gainCorrection = new double[npix];
            for(int i=0; i<npix; i++) {gainCorrection[i] = 1.0;}
        }else {
            gainCorrection = gainService.getIntegralSinglePulseGain();
            for(int i=0; i<npix; i++) {gainCorrection[i] /= factSinglePePulseIntegral;}
        }
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
