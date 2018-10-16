package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
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

        int n = shower.size();
        double sumT = 0.0;
        double sumW = 0.0;
        double sumWT = 0.0;
        double sumT2 = 0.0;
        double sumWT2 = 0.0;

        for (CameraPixel pixel: shower) {
            double t = arrivalTime[pixel.id];
            double w = weights[pixel.id];
            sumT += t;
            sumW += w;
            sumWT += w * t;
            sumT2 += t * t;
            sumWT2 += w * t * t;
        }

        double timespread = Math.sqrt(sumT2 / n - Math.pow(sumT / n, 2));
        double weightedTimespread = Math.sqrt(sumWT2 / sumW - Math.pow(sumWT / sumW, 2));

        item.put(outputKey, timespread);
        item.put(outputKey + "_weighted", weightedTimespread);

        return item;
    }
}
