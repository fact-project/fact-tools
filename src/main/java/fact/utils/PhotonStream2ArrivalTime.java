package fact.utils;

import java.util.Arrays;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class PhotonStream2ArrivalTime implements Processor {
    @Parameter(
        required = true,
        description = "The arrival slices of the single pulses.")
    private String singlePulsesKey = null;

    @Parameter(
        required = true,
        description = "The reconstruted arrival time")
    private String arrivalTimeKey = null;

    @Override
    public Data process(Data input) {
        int[][] singlePulses = (int[][]) input.get(singlePulsesKey);
        double[] arrivalTimes = new double[singlePulses.length];

        for (int pix = 0; pix < singlePulses.length; pix++) {
            DescriptiveStatistics stat = new DescriptiveStatistics();
            for (int slice: singlePulses[pix]){
                stat.addValue((double) slice);
            }
            arrivalTimes[pix] = stat.getPercentile(50);
        }
        input.put(arrivalTimeKey, arrivalTimes);
        return input;
    }

    public void setSinglePulsesKey(String singlePulsesKey) {
        this.singlePulsesKey = singlePulsesKey;
    }

    public void setArrivalTimeKey(String arrivalTimeKey) {
        this.arrivalTimeKey = arrivalTimeKey;
    }

}
