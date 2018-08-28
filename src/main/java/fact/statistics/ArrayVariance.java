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
 * This operator calculates the rms of the array specified by the key
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class ArrayVariance implements Processor {
    static Logger log = LoggerFactory.getLogger(ArrayVariance.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, Double[].class);
        Serializable data = item.get(key);
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(Utils.toDoubleArray(data));
        item.put(outputKey, descriptiveStatistics.getVariance());
        return item;
    }
}
