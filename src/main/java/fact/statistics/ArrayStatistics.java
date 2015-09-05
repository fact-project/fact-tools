package fact.statistics;

import fact.Utils;
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
 * also a subarray can be defined by a given pixelSetArray that contains the array ids that should be used for the sub array    
 */
public class ArrayStatistics implements Processor {

    static Logger log = LoggerFactory.getLogger(ArrayStatistics.class);
    @Parameter(required = true, description = "Key to the array you want the information about")
    private String key = null;
    @Parameter(required = true, description = "The name of the data written to the stream")
    private String outputKey = null;
    @Parameter(description = "key of an array containing the IDs of a desired Subset")
    private String pixelSetKey = null;

    private int []      pixelArray  = null;


    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys( input, key);

        double[]    subset      = null;
        double[] data = Utils.toDoubleArray(input.get(key));

        if (pixelSetKey == null){
            subset = data;
        }
        else{
        	if (!input.containsKey(pixelSetKey))
        	{
        		input.put(outputKey+"_" +"mean",-Double.MAX_VALUE);
                input.put(outputKey+"_" +"max",-Double.MAX_VALUE);
                input.put(outputKey+"_" +"min",-Double.MAX_VALUE);
//                input.put(outputKey+"_" +"geometricMean",-Double.MAX_VALUE);
                input.put(outputKey+"_" +"kurtosis",-Double.MAX_VALUE);
                input.put(outputKey+"_" +"variance",-Double.MAX_VALUE);
                input.put(outputKey+"_" +"skewness",-Double.MAX_VALUE);

                return input;
        	}
            pixelArray  = (int[]) input.get(pixelSetKey);
            subset = new double[pixelArray.length];

            //Loop over array containing the pixel ids of the desired subset
            for (int i = 0; i < pixelArray.length; i++){
                subset[i] = data[pixelArray[i]];
            }
        }


        DescriptiveStatistics s = new DescriptiveStatistics();
        for(double value: subset){
            s.addValue(value);
        }
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
