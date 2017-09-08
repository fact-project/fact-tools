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
    @Parameter(required = true, description = "The name of the data written to the stream")
    private String outputKey = null;
    @Parameter(description = "key of a pixelSet (PixelSetOverlay) containing the IDs of a desired Subset")
    private String pixelSetKey = null;


    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys( input, key);

        double[] data   = Utils.toDoubleArray(input.get(key));


        DescriptiveStatistics s = new DescriptiveStatistics();

        if (pixelSetKey == null){
            /* run over whole data array if no set is specified */
            for(double value : data){
                s.addValue(value);
            }
        }
        else if (input.containsKey(pixelSetKey)){
            /* if a set is specified, use only the pixel ids from the set */
            PixelSet pixelArray = (PixelSet) input.get(pixelSetKey);
            for(CameraPixel pix : pixelArray.set){
                s.addValue(data[pix.id]);
            }
        }
        input.put(outputKey+"_" +"median",s.getPercentile(50));
        input.put(outputKey+"_" +"p25",s.getPercentile(25));
        input.put(outputKey+"_" +"p75",s.getPercentile(75));
        input.put(outputKey+"_" +"mean",s.getMean());
        input.put(outputKey+"_" +"max",s.getMax());
        input.put(outputKey+"_" +"min",s.getMin());
//        input.put(outputKey+"_" +"geometricMean",s.getGeometricMean());
        input.put(outputKey+"_" +"kurtosis",s.getKurtosis());
        input.put(outputKey+"_" +"variance",s.getVariance());
        input.put(outputKey+"_" +"skewness",s.getSkewness());

        return input;

	}

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }

}
