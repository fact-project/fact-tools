package fact.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This operator returns the length of the array specified by the key
 *
 * @author Maximilian Noethe maximilian.noethe@tu-dortmund.de
 */
public class ArrayLength implements Processor {
    static Logger log = LoggerFactory.getLogger(ArrayLength.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey = "length";

    @Override
    public Data process(Data input) {
        Object[] data = (Object[]) input.get(key);
        int length = data.length;

        //get the sqrt of the sum of squares. Lets call it RMS. Cause we can.
        input.put(outputKey, length);
        return input;
    }
}
