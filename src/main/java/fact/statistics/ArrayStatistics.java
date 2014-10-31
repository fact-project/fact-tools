package fact.statistics;

import fact.Utils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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
 * outputKey+"_" +"geommetricMean"
 * outputKey+"_" +"kurtosis"
 * outputKey+"_" +"variance"
 */
public class ArrayStatistics implements Processor {

    static Logger log = LoggerFactory.getLogger(ArrayStatistics.class);
    @Parameter(required = true, description = "Key to the array you want the information about")
    private String key = null;
    @Parameter(required = true, description = "The name of the data written to the stream")
    private String outputKey = null;

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys( input, key);

        double[] data = Utils.toDoubleArray(input.get(key));

        DescriptiveStatistics s = new DescriptiveStatistics();
        for(double value: data){
            s.addValue(value);
        }
        input.put(outputKey+"_" +"mean",s.getMean());
        input.put(outputKey+"_" +"max",s.getMax());
        input.put(outputKey+"_" +"min",s.getMin());
        input.put(outputKey+"_" +"geommetricMean",s.getGeometricMean());
        input.put(outputKey+"_" +"kurtosis",s.getKurtosis());
        input.put(outputKey+"_" +"variance",s.getVariance());


        return input;

	}

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }


}
