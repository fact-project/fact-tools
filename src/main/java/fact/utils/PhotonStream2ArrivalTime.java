package fact.utils;

import java.util.Arrays;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

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
            arrivalTimes[pix] = this.getMedian(singlePulses[pix]);
        }
        input.put(arrivalTimeKey, arrivalTimes);
        return input;
    }

    public double getMedian(int[] pulses){
        double median;
        Arrays.sort(pulses);  // acts in place, but I guess this is no problem.

        int len = pulses.length;
        if (len%2 == 1 ){
            median =  pulses[(len - 1) / 2];
        } else {
            median =(pulses[len/2] + pulses[len/2 - 1]) / 2.;
        }
        return median;
    }

    public void setSinglePulsesKey(String singlePulsesKey) {
        this.singlePulsesKey = singlePulsesKey;
    }

    public void setArrivalTimeKey(String arrivalTimeKey) {
        this.arrivalTimeKey = arrivalTimeKey;
    }

}
