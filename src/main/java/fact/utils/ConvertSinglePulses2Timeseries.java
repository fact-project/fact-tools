package fact.utils;

import fact.Utils;
import fact.features.singlePulse.timeLineExtraction.AddFirstArrayToSecondArray;
import fact.features.singlePulse.timeLineExtraction.SinglePulseExtractor;
import fact.features.singlePulse.timeLineExtraction.TemplatePulse;
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
    @Parameter(required = true, description = "")
    private String singlePulsesKey = null;

    @Parameter(required = true, description = "")
    private String timeseriesKey = null;

    @Parameter(required = false, description = "")
    private int roi = 300;

    @Override
    public Data process(Data input) {

        int[][] singlePulses = (int[][]) input.get(singlePulsesKey);

        double[] pulseTemplate = TemplatePulse.factSinglePePulse(roi);

        double[] timeseries = new double[0];

        for (int pix = 0; pix < singlePulses.length; pix++) {

            //create empty time series of length roi
            double[] current_timeseries = new double[roi];

            for (int pulse = 0; pulse < singlePulses[pix].length; pulse++) {
                AddFirstArrayToSecondArray.at(pulseTemplate, current_timeseries, singlePulses[pix][pulse]);
            }

            timeseries = (double[]) ArrayUtils.addAll(timeseries, current_timeseries);
        }

        input.put(timeseriesKey, timeseries);

        return input;
    }

    public void setSinglePulsesKey(String singlePulsesKey) {
        this.singlePulsesKey = singlePulsesKey;
    }

    public void setTimeseriesKey(String timeseriesKey) {
        this.timeseriesKey = timeseriesKey;
    }

    public void setRoi(int roi) {
        this.roi = roi;
    }
}
