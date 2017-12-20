package fact.statistics;

import fact.Utils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.io.Serializable;

/**
 * This operator calculates the mean value of the values in of the array specified by the key.
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class ArrayMean implements Processor {
    static Logger log = LoggerFactory.getLogger(ArrayMean.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey = "mean";

    @Override
    public Data process(Data input) {
        if (input.containsKey(key)) {
            Serializable data = input.get(key);
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(Utils.toDoubleArray(data));

            input.put(outputKey, descriptiveStatistics.getMean());
            input.put(outputKey + "_deviation", descriptiveStatistics.getStandardDeviation());
            input.put(outputKey + "_N", descriptiveStatistics.getN());
            return input;
        } else {
            throw new RuntimeException("Key not found in event. " + key);
        }
    }
}
