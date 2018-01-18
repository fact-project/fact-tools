package fact.photonstream;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PhotonStream2NumberOfPhotons implements Processor {
    @Parameter(
            required = true,
            description = "The arrival slices of the single pulses.")
    public String singlePulsesKey = null;

    @Parameter(
            required = true,
            description = "The reconstruted arrival time")
    public String arrivalTimeKey = null;

    @Parameter(
            required = true,
            description = "The reconstruted number of photons")
    public String numberOfPhotonsKey = null;

    @Override
    public Data process(Data item) {
        int[][] singlePulses = (int[][]) item.get(singlePulsesKey);
        double[] arrivalTimes = (double[]) item.get(arrivalTimeKey);
        int[] numberOfPhotons = new int[singlePulses.length];

        for (int pix = 0; pix < singlePulses.length; pix++) {
            int[] pulses = singlePulses[pix];
            for (int time_of_pulse : pulses) {
                if ((time_of_pulse >= arrivalTimes[pix] - 5) &&
                        (time_of_pulse < arrivalTimes[pix] + 25)) {
                    numberOfPhotons[pix] += 1;
                }
            }
        }
        item.put(numberOfPhotonsKey, numberOfPhotons);
        return item;
    }
}
