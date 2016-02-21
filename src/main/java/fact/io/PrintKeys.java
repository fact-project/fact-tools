package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;

import java.text.DecimalFormat;

/**
 * Prints the specified keys to the console in a nicely formatted manner.
 */
public class PrintKeys implements Processor {

	@Parameter(required = true, description = "The keys to print out, supports wildcards and regex in / /")
	private Keys keys = new Keys("");

	@Parameter(description = "Separator between keys", defaultValue = "\n")
	private String sep = "\n\t";

	@Parameter(description = "The decimal format, see java.ext.DecimalFormat", defaultValue = "#.###")
	private String decimalFormat = "#.###";

	@Override
	public Data process(Data item) {
		final Logger log = LoggerFactory.getLogger(PrintKeys.class);

		DecimalFormat format = new DecimalFormat(decimalFormat);

		String output = sep;
		for(String key : keys.select(item))
		{
			String repr;
			try{
				Number value = (Number) item.get(key);
				repr = format.format(value);
			}
			catch(ClassCastException e) {
				repr = String.valueOf(item.get(key));
			}
			output += key + " = " + repr + sep;
		}
		System.out.println(output);
		return item;
	}
}
