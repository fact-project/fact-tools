package fact.utils;

import fact.Utils;
import fact.features.singlePulse.timeSeriesExtraction.AddFirstArrayToSecondArray;
import fact.features.singlePulse.timeSeriesExtraction.SinglePulseExtractor;
import fact.features.singlePulse.timeSeriesExtraction.TemplatePulse;
import fact.features.singlePulse.timeSeriesExtraction.ElementWise;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.function.Sin;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import javax.rmi.CORBA.Util;

/**
 * Created by jebuss on 28.10.16.
 */
public class ConvertSinglePulses2Timeseries implements Processor {
    @Parameter(required = true, description = "The arrival slices of the single pulses.")
    private String singlePulsesKey = null;

    @Parameter(required = true, description = "The reconstruted time series.")
    private String timeseriesKey = null;

    @Parameter(required = false, description = "The region of interest to be reconstructed.")
    private int roi = 300;

    @Parameter(required = false, description = "The reconstructed baseline of the original time series.")
    private String baseLineKey = null;

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
            double[] current_timeseries = new double[roi];

            // Add the single pulses to the time series
            for (int pulse = 0; pulse < singlePulses[pix].length; pulse++) {
                AddFirstArrayToSecondArray.at(
                    pulseTemplate, 
                    current_timeseries, 
                    singlePulses[pix][pulse]);
            }

            // Add the baseline to the time series
            current_timeseries = ElementWise.add(current_timeseries, baseLine[pix]);

            timeSeries = (double[]) ArrayUtils.addAll(timeSeries, current_timeseries);
        }

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        timeSeries = ElementWise.multiply(
            timeSeries, 
            config.factSinglePeAmplitudeInMv);

        input.put(timeseriesKey, timeSeries);

        return input;
    }

    public void setSinglePulsesKey(String singlePulsesKey) {
        this.singlePulsesKey = singlePulsesKey;
    }

    public void setTimeseriesKey(String timeseriesKey) {
        this.timeseriesKey = timeseriesKey;
    }

    public void setBaseLineKey(String baseLineKey) {
        this.baseLineKey = baseLineKey;
    }

    public void setRoi(int roi) {
        this.roi = roi;
    }
}
