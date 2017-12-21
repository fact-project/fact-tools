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
 * This operator calculates the sum of the values in of the array specified by the key.
 * <p>
 * This operator was created by refactoring ArrayMean
 *
 * @author Maximilian Noethe &lt;maximilian.noethe@tu-dortmund.de&gt;
 */
public class ArraySum implements Processor {
    static Logger log = LoggerFactory.getLogger(ArraySum.class);

    @Parameter(required = true)
    private String key;

    @Parameter(required = true)
    private String outputKey = "sum";

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, key);

        Serializable data = input.get(key);
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(Utils.toDoubleArray(data));

        input.put(outputKey, descriptiveStatistics.getSum());
        return input;
    }
}
