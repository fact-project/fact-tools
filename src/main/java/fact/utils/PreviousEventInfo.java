package fact.utils;

import fact.Utils;
import fact.container.PreviousEventInfoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


public class PreviousEventInfo implements Processor {
	static Logger log = LoggerFactory.getLogger(PreviousEventInfo.class);
	
	@Parameter(required=true)
	String startCellKey = null;
	@Parameter(required=true)
	String outputKey=null;
	
	int limitEvents=20;
	
	PreviousEventInfoContainer previousEventInfo = new PreviousEventInfoContainer();
	

	@Override
	public Data process(Data input) {
		
		Utils.isKeyValid(input, startCellKey, short[].class);
		Utils.isKeyValid(input, "NROI", Integer.class);
		Utils.isKeyValid(input, "UnixTimeUTC", int[].class);
				
		int[] eventTime = (int[]) input.get("UnixTimeUTC");
		short[] startCellArray = (short[])input.get(startCellKey);
		int length = (Integer) input.get("NROI");
		
		short[] stopCellArray = new short[startCellArray.length];
		//calculate the stopcellArray for the current event
		for (int i = 0; i < startCellArray.length; ++i){
			//there are 1024 capacitors in the ringbuffer
			stopCellArray[i] = (short) ((startCellArray[i] + length)% 1024);
		}
		
		previousEventInfo.addNewInfo(startCellArray, stopCellArray, eventTime);
		
		if (previousEventInfo.getListSize() > limitEvents)
		{
			previousEventInfo.removeLastInfo();
		}
		
		input.put(outputKey, previousEventInfo);
		return input;
	}

	public String getStartCellKey() {
		return startCellKey;
	}

	public void setStartCellKey(String startCellKey) {
		this.startCellKey = startCellKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public int getLimitEvents() {
		return limitEvents;
	}

	public void setLimitEvents(int limitEvents) {
		this.limitEvents = limitEvents;
	}

}
