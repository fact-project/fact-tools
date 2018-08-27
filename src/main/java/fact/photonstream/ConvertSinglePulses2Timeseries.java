package fact.photonstream;

import fact.photonstream.timeSeriesExtraction.AddFirstArrayToSecondArray;
import fact.photonstream.timeSeriesExtraction.SinglePulseExtractor;
import fact.photonstream.timeSeriesExtraction.TemplatePulse;
import fact.utils.ElementWise;
import org.apache.commons.lang3.ArrayUtils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * Created by jebuss on 28.10.16.
 */
public class ConvertSinglePulses2Timeseries implements Processor {
    @Parameter(required = true, description = "The arrival slices of the single pulses.")
    public String singlePulsesKey = null;

    @Parameter(required = true, description = "The reconstruted time series.")
    public String timeSeriesKey = null;

    @Parameter(required = false, description = "The region of interest to be reconstructed.")
    public int roi = 300;

    @Parameter(required = false, description = "The reconstructed baseline of the original time series.")
    public String baseLineKey = null;

    @Override
    public Data process(Data item) {

        int[][] singlePulses = (int[][]) item.get(singlePulsesKey);

        double[] baseLine = new double[singlePulses.length];
        if (baseLineKey != null) {
            baseLine = (double[]) item.get(baseLineKey);
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
        }

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        timeSeries = ElementWise.multiply(
                timeSeries,
                config.factSinglePeAmplitudeInMv);

        item.put(timeSeriesKey, timeSeries);

        return item;
    }
}
