package fact.statistics;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * This operator returns the length of the array specified by the key
 *
 *  @author  Maximilian Noethe maximilian.noethe@tu-dortmund.de
 */
public class ArrayLength implements Processor {
	static Logger log = LoggerFactory.getLogger(ArrayLength.class);
	@Parameter(required = true, description = "The key pointing to the input array")
	private String key;
	@Parameter(required = false, description = "The outputkey, if not given, use key:length")
	private String outputKey = null;

	@Override
	public Data process(Data input)
	{
		if (outputKey == null) {
			outputKey = key + ":length";
		}
		Serializable serializable = input.get(key);
		input.put(outputKey, Array.getLength(serializable));
		return input;
	}
}
