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
 *
 * outputKey+"_" +"mean"
 * outputKey+"_" +"max"
 * outputKey+"_" +"min"
 * outputKey+"_" +"kurtosis"
 * outputKey+"_" +"variance"
 * outputKey+"_" +"skewness"
 *
 * also a subarray can be defined by a given pixelSet that contains the pixel ids that should be used for the sub array.
 * If no set can be find for the given key NaN are returned for the values.
 *
 * Created by jbuss
 */
public class ArrayStatistics implements Processor {

    static Logger log = LoggerFactory.getLogger(ArrayStatistics.class);
    @Parameter(required = true, description = "Key to the array you want the information about")
    private String key = null;
    @Parameter(required = false, description = "The basename for the output, if not given, use inputKey")
    private String outputKey = null;
    @Parameter(description = "key of a pixelSet (PixelSetOverlay) containing the IDs of a desired Subset")
    private String pixelSetKey = null;


    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, key);

        if (outputKey == null){
            outputKey = key;
        }
        double[] values   = Utils.toDoubleArray(item.get(key));

        DescriptiveStatistics s = new DescriptiveStatistics();

        if (pixelSetKey == null){
            /* run over whole data array if no set is specified */
            for(double value : values){
                s.addValue(value);
            }
        }
        else if (item.containsKey(pixelSetKey)){
            /* if a set is specified, use only the pixel ids from the set */
            PixelSet pixelArray = (PixelSet) item.get(pixelSetKey);
            for(CameraPixel pix : pixelArray.set){
                s.addValue(values[pix.id]);
            }
        }
        item.put(outputKey + ":" + "median", s.getPercentile(50));
        item.put(outputKey + ":" + "pSigmaLow", s.getPercentile(15.87));
        item.put(outputKey + ":" + "pSimgaHigh", s.getPercentile(84.13));
        item.put(outputKey + ":" + "p25", s.getPercentile(25));
        item.put(outputKey + ":" + "p75", s.getPercentile(75));
        item.put(outputKey + ":" + "mean", s.getMean());
        item.put(outputKey + ":" + "max", s.getMax());
        item.put(outputKey + ":" + "min", s.getMin());
        item.put(outputKey + ":" + "stdDev", s.getStandardDeviation());
        item.put(outputKey + ":" + "variance", s.getVariance());
        item.put(outputKey + ":" + "kurtosis", s.getKurtosis());
        item.put(outputKey + ":" + "skewness", s.getSkewness());
        item.put(outputKey + ":" + "sum", s.getSum());

        return item;

	}
}
