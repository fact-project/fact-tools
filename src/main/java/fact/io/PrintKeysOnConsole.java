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

	private int repRate = 1;
	private int counter = 0;


	@Override
	public Data process(Data input) {
		final Logger log = LoggerFactory.getLogger(PrintKeysOnConsole.class);

		if(counter % repRate == 0){
			String output = "\n";
			for(String key : keys)
			{
				output += "\t" + key + " = " + String.valueOf(input.get(key)) + "\n";

			}
			log.info(output);
		}
		counter += 1;

		return input;
	}

	public String[] getKeys() {
		return keys;
	}

	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	public void setRepRate(int repRate) {
		this.repRate = repRate;
	}
}
