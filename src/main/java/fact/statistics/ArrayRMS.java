package fact.statistics;

import fact.Utils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;

import java.io.Serializable;

/**
 * This operator calculates the rms of the array specified by the key
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class ArrayRMS implements Processor {
    static Logger log = LoggerFactory.getLogger(ArrayRMS.class);
    private String key;
    private String outputKey = "rms";

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, Double[].class);
        Serializable data = input.get(key);
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(Utils.toDoubleArray(data));

        //get the sqrt of the sum of squares. Lets call it RMS. Cause we can.
        input.put(outputKey, Math.sqrt(descriptiveStatistics.getSumsq()));
        return input;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
