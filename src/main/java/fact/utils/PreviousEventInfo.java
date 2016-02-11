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
	
	@Parameter(required=false, defaultValue = "meta:startCellData",
				description = "Key of the StartCellData in the data fits file")
	String startCellKey = "meta:startCellData";
	@Parameter(required=false, defaultValue = "meta:prevEvents",
				description = "Outputkey for the previous events Key")
	String outputKey = "meta:prevEvents";
	@Parameter(required=false, defaultValue = "20")
	int limitEvents=20;
	
	PreviousEventInfoContainer previousEventInfo = new PreviousEventInfoContainer();
	

	@Override
	public Data process(Data item) {
		
		Utils.isKeyValid(item, startCellKey, short[].class);
		Utils.isKeyValid(item, "NROI", Integer.class);
		Utils.isKeyValid(item, "UnixTimeUTC", int[].class);
				
		int[] eventTime = (int[]) item.get("UnixTimeUTC");
		short[] startCellArray = (short[])item.get(startCellKey);
		int length = (Integer) item.get("NROI");
		
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
		
		item.put(outputKey, previousEventInfo);
		return item;
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
