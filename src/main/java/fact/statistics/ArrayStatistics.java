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
 * Calculates statistical properties about the given array to the data item.
 * The following properties are calculated:
 * * median
 * * mean
 * * max
 * * min
 * * stdDev - standard deviation
 * * variance
 * * kurtosis
 * * skewness
 * * sum
 * * pSigmaLow - lower 1 sigma percentile
 * * pSigmaHigh - upper 1 sigma percentile
 * * p25 - lower quartile
 * * p75 - upper quartile
 *
 * If <code>pixelSet</code> is given, these values are only calculated for the given pixels.
 *
 * Created by jbuss
 */
public class ArrayStatistics implements Processor {

    static Logger log = LoggerFactory.getLogger(ArrayStatistics.class);
    @Parameter(required = true, description = "Key to the array you want the information about")
    private String key = null;
    @Parameter(required = false, description = "The basename for the output, if not given, use 'key:'")
    private String outputKey = null;
    @Parameter(description = "Key pointing to a pixelSet, if given, only calculate values on these pixels")
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
        item.put(outputKey + ":" + "mean", s.getMean());
        item.put(outputKey + ":" + "max", s.getMax());
        item.put(outputKey + ":" + "min", s.getMin());
        item.put(outputKey + ":" + "stdDev", s.getStandardDeviation());
        item.put(outputKey + ":" + "variance", s.getVariance());
        item.put(outputKey + ":" + "kurtosis", s.getKurtosis());
        item.put(outputKey + ":" + "skewness", s.getSkewness());
        item.put(outputKey + ":" + "sum", s.getSum());
        item.put(outputKey + ":" + "pSigmaLow", s.getPercentile(15.87));
        item.put(outputKey + ":" + "pSimgaHigh", s.getPercentile(84.13));
        item.put(outputKey + ":" + "p25", s.getPercentile(25));
        item.put(outputKey + ":" + "p75", s.getPercentile(75));

        return item;

	}
}
