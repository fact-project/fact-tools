package fact.statistics;

import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Writes some information about the given array to the data item. The following information will be written:
 * <p>
 * outputKey+"_" +"mean"
 * outputKey+"_" +"max"
 * outputKey+"_" +"min"
 * outputKey+"_" +"kurtosis"
 * outputKey+"_" +"variance"
 * outputKey+"_" +"skewness"
 * <p>
 * also a subarray can be defined by a given pixelSet that contains the pixel ids that should be used for the sub array.
 * If no set can be find for the given key NaN are returned for the values.
 * <p>
 * Created by jbuss
 */
public class ArrayStatistics implements Processor {

    static Logger log = LoggerFactory.getLogger(ArrayStatistics.class);

    @Parameter(required = true, description = "Key to the array you want the information about")
    public String key = null;

    @Parameter(required = true, description = "The name of the data written to the stream")
    public String outputKey = null;

    @Parameter(description = "key of a pixelSet (PixelSetOverlay) containing the IDs of a desired Subset")
    public String pixelSetKey = null;

    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, key);

        double[] data = Utils.toDoubleArray(item.get(key));


        DescriptiveStatistics s = new DescriptiveStatistics();

        if (pixelSetKey == null) {
            /* run over whole data array if no set is specified */
            for (double value : data) {
                s.addValue(value);
            }
        } else if (item.containsKey(pixelSetKey)) {
            /* if a set is specified, use only the pixel ids from the set */
            PixelSet pixelArray = (PixelSet) item.get(pixelSetKey);
            for (CameraPixel pix : pixelArray.set) {
                s.addValue(data[pix.id]);
            }
        }
        item.put(outputKey + "_" + "median", s.getPercentile(50));
        item.put(outputKey + "_" + "p25", s.getPercentile(25));
        item.put(outputKey + "_" + "p75", s.getPercentile(75));
        item.put(outputKey + "_" + "mean", s.getMean());
        item.put(outputKey + "_" + "max", s.getMax());
        item.put(outputKey + "_" + "min", s.getMin());
        item.put(outputKey + "_" + "kurtosis", s.getKurtosis());
        item.put(outputKey + "_" + "variance", s.getVariance());
        item.put(outputKey + "_" + "skewness", s.getSkewness());

        return item;

    }
}
