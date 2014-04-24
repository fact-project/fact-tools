package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;

public class PrintKeysOnConsole implements Processor {
	
	private String[] keys;

	@Override
	public Data process(Data input) {
		final Logger log = LoggerFactory.getLogger(BinaryFactWriter.class);
		
		for(String key : keys)
		{
			String output = String.valueOf(input.get(key));
			log.info(key + " = " + output);
		}
		
		
		return input;
	}

	public String[] getKeys() {
		return keys;
	}

	public void setKey(String[] keys) {
		this.keys = keys;
	}

}
