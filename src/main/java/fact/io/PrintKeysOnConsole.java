package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;

/**
 * Prints the specified keys to the console in a nicely formatted manner.
 */
public class PrintKeysOnConsole implements Processor {

    private String[] keys;

    @Override
    public Data process(Data input) {
        final Logger log = LoggerFactory.getLogger(PrintKeysOnConsole.class);

        String output = "\n";
        for (String key : keys) {
            output += "\t" + key + " = " + String.valueOf(input.get(key)) + "\n";

        }
        log.info(output);

        return input;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

}
