package fact.photonstream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PhotonStream2ArrivalTime implements Processor {
    @Parameter(
            required = true,
            description = "The arrival slices of the single pulses.")
    public String singlePulsesKey = null;

    @Parameter(
            required = true,
            description = "The reconstruted arrival time")
    public String arrivalTimeKey = null;

    @Override
    public Data process(Data item) {
        int[][] singlePulses = (int[][]) item.get(singlePulsesKey);
        double[] arrivalTimes = new double[singlePulses.length];

        for (int pix = 0; pix < singlePulses.length; pix++) {
            if (singlePulses[pix].length == 0) {
                arrivalTimes[pix] = 0.; // use zero instead of NaN - plotter likes no NaN
            } else {
                DescriptiveStatistics stat = new DescriptiveStatistics();
                for (int slice : singlePulses[pix]) {
                    stat.addValue((double) slice);
                }
                arrivalTimes[pix] = stat.getPercentile(50);
            }
        }
        item.put(arrivalTimeKey, arrivalTimes);
        return item;
    }
}
