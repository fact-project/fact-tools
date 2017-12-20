package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;

import java.io.Serializable;

/**
 * Prints the specified keys to the console in a nicely formatted manner.
 */
public class PrintKeys implements Processor {

    @Parameter
    public Keys keys = new Keys("*");

    @Override
    public Data process(Data input) {
        final Logger log = LoggerFactory.getLogger(PrintKeys.class);

        String output = "\n";


        for (String key : keys.select(input)) {
            Serializable value = input.get(key);
            output += "\t" + key + " = " + String.valueOf(value) + "\n";
        }
        log.info(output);

        return input;
    }

}
