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
    private String photonchargeKey = "photoncharge";

    @Parameter(description = "Key containing the arrivalTime array", defaultValue = "arrivalTime")
    private String arrivalTimeKey = "arrivalTime";

    @Parameter(description = "Key for the ring pixelset", defaultValue = "bestRingPixel")
    private String ringPixelSetKey = "bestRingPixel";

    @Parameter(description = "Key for the cleaning pixelset", defaultValue = "shower")
    private String cleaningPixelSetKey = "shower";

    @Parameter(description = "The threshold, only pixels above the threshold are considered", defaultValue = "8")
    private double threshold = 8.0;

    @Parameter(description = "The outputkey", defaultValue = "StdDevTime{threshold}")
    private String outputKey = null;

    @Override
    public Data process(Data item) {

        if (outputKey == null){
            outputKey = "StdDevTime" + String.valueOf(threshold);
        }

        Utils.mapContainsKeys(item, photonchargeKey, arrivalTimeKey, ringPixelSetKey, cleaningPixelSetKey);

        PixelSet cleaningPixel = (PixelSet) item.get(cleaningPixelSetKey);
        PixelSet ringPixel = (PixelSet) item.get(ringPixelSetKey);

        double[] photoncharge = (double[]) item.get(photonchargeKey);
        double[] arrivalTime = (double[]) item.get(arrivalTimeKey);


        Sets.SetView<CameraPixel> intersection = Sets.intersection(cleaningPixel.set, ringPixel.set);

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for(CameraPixel pixel: intersection){
            if(photoncharge[pixel.id] >= threshold){
                stats.addValue(arrivalTime[pixel.id]);
            }
        }

        item.put(outputKey, stats.getStandardDeviation());
        item.put("numPixel" + outputKey, stats.getN());
        return item;
    }

    public void setPhotonchargeKey(String photonchargeKey) {
        this.photonchargeKey = photonchargeKey;
    }

    public void setArrivalTimeKey(String arrivalTimeKey) {
        this.arrivalTimeKey = arrivalTimeKey;
    }

    public void setRingPixelSetKey(String ringPixelSetKey) {
        this.ringPixelSetKey = ringPixelSetKey;
    }

    public void setCleaningPixelSetKey(String cleaningPixelSetKey) {
        this.cleaningPixelSetKey = cleaningPixelSetKey;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
