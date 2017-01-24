package fact.datacorrection;

import fact.Constants;
import fact.calibrationservice.SinglePulseGainCalibService;
import fact.features.singlePulse.timeSeriesExtraction.TemplatePulse;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

/**
 * Calibrates the unit of TimeLines from mV to SinglePulseAmplitude.
 *
 * This means, after this calibration the *height* or *amplitude* of a single pulse is 1.
 * This means further, that the integral typically performed by BasicExtraction,
 * will not be 1. It will instead have the same value, the *TemplatePulse* has, when integrated
 * the same way.
 */
public class SinglePulseGainCalibration implements StatefulProcessor{

    @Parameter(
            required = true,
            description = "Name of the output array"
    )
    private String dataKey = "DataCalibrated";

    @Parameter(
            required = true,
            description = "Name of input array"
    )
    private String outputKey = "DataCalibrated";

    @Parameter(
            required = true,
            description = "The calibration service for the integral single pulse gain"
    )
    private SinglePulseGainCalibService gainService;
    private double[] gainCorrection;

    @Override
    public void init(ProcessContext processContext) throws Exception {
        double factSinglePePulseIntegral = TemplatePulse.factSinglePePulseIntegral();  // unit: SinglePulseAmplitude * slices
        if(gainService == null) {
            gainCorrection = new double[Constants.NUMBEROFPIXEL];
            for(int i=0; i<gainCorrection.length; i++) {gainCorrection[i] = 1.0;}
        }else {
            gainCorrection = gainService.getIntegralSinglePulseGain();  // unit: mV * slices
            for(int i=0; i<gainCorrection.length; i++) {gainCorrection[i] /= factSinglePePulseIntegral;}
            // now gainCorrection unit: mV / SinglePulseAmplitude  -> value: ~10
        }

    }

    @Override
    public Data process(Data input) {
        double[] TimeLines = ((double[]) input.get(dataKey)).clone();

        final int npix = Constants.NUMBEROFPIXEL;
        final int roi = TimeLines.length / npix;

        for (int pix = 0; pix < npix; pix++) {
            for (int slice =0; slice < roi; slice++){
                TimeLines[pix*roi + slice] /= gainCorrection[pix];
                // now TimeLines unit: SinglePulseAmplitude, i.e. a single pulse has amplitude 1 in this units.
            }
        }

        input.put(outputKey, TimeLines);
        return input;
    }


    @Override
    public void resetState() throws Exception {
        // empty on purpose
    }

    @Override
    public void finish() throws Exception {
        // empty on purpose
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setGainService(SinglePulseGainCalibService gainService) {
        this.gainService = gainService;
    }
}
