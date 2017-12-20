package fact.statistics;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;

import java.io.Serializable;

/**
 * This operator returns the length of the array specified by the key
 *
 * @author Maximilian Noethe maximilian.noethe@tu-dortmund.de
 */
public class ArrayLength implements Processor {
    static Logger log = LoggerFactory.getLogger(ArrayLength.class);
    private String key;
    private String outputKey = "length";

    @Override
    public Data process(Data input) {
        Serializable data = input.get(key);
        double[] dataArray = Utils.toDoubleArray(data);
        int length = dataArray.length;

        //get the sqrt of the sum of squares. Lets call it RMS. Cause we can.
        input.put(outputKey, length);
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
