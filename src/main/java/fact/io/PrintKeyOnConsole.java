package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;

public class PrintKeyOnConsole implements Processor {
	
	private String key;

	@Override
	public Data process(Data input) {
		final Logger log = LoggerFactory.getLogger(BinaryFactWriter.class);
		
		String output = String.valueOf(input.get(key));
		
		log.info(key + " = " + output);
		
		
		return input;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
