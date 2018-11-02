package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class TimeSpread implements Processor {

    @Parameter(required = true)
    public String arrivalTimeKey = null;

    @Parameter(required = true)
    public String weightsKey = null;

    @Parameter(required = true)
    public String pixelSetKey = null;

    @Parameter(required = false)
    public String outputKey = "timepread";

    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, arrivalTimeKey, weightsKey, pixelSetKey);

        double[] arrivalTime = (double[]) item.get(arrivalTimeKey);
        double[] weights = (double[]) item.get(weightsKey);
        PixelSet shower = (PixelSet) item.get(pixelSetKey);

        // ignore negative weights, should only happen when evaluated on pedestal pixel set
        double[] t = shower.stream().filter(p -> weights[p.id] > 0).mapToDouble(p -> arrivalTime[p.id]).toArray();
        double[] w = shower.stream().filter(p -> weights[p.id] > 0).mapToDouble(p -> weights[p.id]).toArray();

        Variance var = new Variance();
        var.setBiasCorrected(false);

        double timespread = Math.sqrt(var.evaluate(t));
        double weightedTimespread;
        if (t.length > 0) {
            var.clear();
            weightedTimespread = Math.sqrt(var.evaluate(t, w));
        } else {
            weightedTimespread = Double.NaN;
        }
        item.put(outputKey, timespread);
        item.put(outputKey + "_weighted", weightedTimespread);

        return item;
    }
}
