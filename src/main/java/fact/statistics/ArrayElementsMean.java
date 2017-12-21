package fact.statistics;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;

import java.io.Serializable;

/**
 * This operator calculates the mean value of each element in the array and puts out another array which holds the mean value for each element.
 * The output array is of the same length as the input array. Which means that the input array in each event has to be of the same length.
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class ArrayElementsMean implements Processor {
    static Logger log = LoggerFactory.getLogger(ArrayElementsMean.class);
    private String key;
    private String outputKey = "mean";
    private long counter = 1;
    private double[] result = null;

    @Override
    public Data process(Data input) {
        if (input.containsKey(key)) {
            try {
                Serializable data = input.get(key);
                double[] vals = Utils.toDoubleArray(data);
                //save the result in an array with the same length as the given array.
                if (result == null) {
                    result = new double[vals.length];
                }
                //for each element calulate the mean value
                for (int i = 0; i < vals.length; i++) {
                    result[i] = result[i] + (vals[i] - result[i]) / counter;
                }
                counter++;
                input.put(outputKey, result);
                return input;
            } catch (ArrayIndexOutOfBoundsException e) {
                log.error("Array lengths did not match. The length of the input array has to stay constant during the stream.");
                throw new ArrayIndexOutOfBoundsException("Array lengths did not match.");
            }
        } else {
            throw new RuntimeException("Key not found in event. " + key);
        }
    }
}
