package fact.utils;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PhotonStream2NumberOfPhotons implements Processor {
    @Parameter(
        required = true,
        description = "The arrival slices of the single pulses.")
    private String singlePulsesKey = null;

    @Parameter(
        required = true,
        description = "The reconstruted arrival time")
    private String arrivalTimeKey = null;

    @Parameter(
        required = true,
        description = "The reconstruted number of photons")
    private String numberOfPhotonsKey = null;

    @Override
    public Data process(Data input) {
        int[][] singlePulses = (int[][]) input.get(singlePulsesKey);
        double[] arrivalTimes = (double[]) input.get(arrivalTimeKey);
        int[] numberOfPhotons = new int[singlePulses.length];

        for (int pix = 0; pix < singlePulses.length; pix++) {
            int[] pulses = singlePulses[pix];
            for (int time_of_pulse: pulses){
                if ((time_of_pulse >= arrivalTimes[pix] - 5) &&
                    (time_of_pulse < arrivalTimes[pix] + 25)){
                    numberOfPhotons[pix] += 1;
                }
            }
        }
        input.put(numberOfPhotonsKey, numberOfPhotons);
        return input;
    }

    public void setSinglePulsesKey(String singlePulsesKey) {
        this.singlePulsesKey = singlePulsesKey;
    }

    public void setArrivalTimeKey(String arrivalTimeKey) {
        this.arrivalTimeKey = arrivalTimeKey;
    }

    public void setNumberOfPhotonsKey(String numberOfPhotonsKey) {
        this.numberOfPhotonsKey = numberOfPhotonsKey;
    }

}
