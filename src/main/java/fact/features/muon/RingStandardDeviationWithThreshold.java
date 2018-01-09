package fact.features.muon;

import com.google.common.collect.Sets;
import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by max on 13.05.16.
 */
public class RingStandardDeviationWithThreshold implements Processor {

    @Parameter(description = "Key containing the photoncharge array", defaultValue = "photoncharge")
    public String photonchargeKey = "photoncharge";

    @Parameter(description = "Key containing the arrivalTime array", defaultValue = "arrivalTime")
    public String arrivalTimeKey = "arrivalTime";

    @Parameter(description = "Key for the ring pixelset", defaultValue = "bestRingPixel")
    public String ringPixelSetKey = "bestRingPixel";

    @Parameter(description = "Key for the cleaning pixelset", defaultValue = "shower")
    public String cleaningPixelSetKey = "shower";

    @Parameter(description = "The threshold, only pixels above the threshold are considered", defaultValue = "8")
    public double threshold = 8.0;

    @Parameter(description = "The outputkey", defaultValue = "StdDevTime{threshold}")
    public String outputKey = null;

    @Override
    public Data process(Data item) {

        if (outputKey == null) {
            outputKey = "StdDevTime" + String.valueOf(threshold);
        }

        Utils.mapContainsKeys(item, photonchargeKey, arrivalTimeKey, ringPixelSetKey, cleaningPixelSetKey);

        PixelSet cleaningPixel = (PixelSet) item.get(cleaningPixelSetKey);
        PixelSet ringPixel = (PixelSet) item.get(ringPixelSetKey);

        double[] photoncharge = (double[]) item.get(photonchargeKey);
        double[] arrivalTime = (double[]) item.get(arrivalTimeKey);


        Sets.SetView<CameraPixel> intersection = Sets.intersection(cleaningPixel.set, ringPixel.set);

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (CameraPixel pixel : intersection) {
            if (photoncharge[pixel.id] >= threshold) {
                stats.addValue(arrivalTime[pixel.id]);
            }
        }

        item.put(outputKey, stats.getStandardDeviation());
        item.put("numPixel" + outputKey, stats.getN());
        return item;
    }
}
