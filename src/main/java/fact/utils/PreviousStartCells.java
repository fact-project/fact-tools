package fact.utils;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.LinkedList;

public class PreviousStartCells implements Processor {
	static Logger log = LoggerFactory.getLogger(PreviousStartCells.class);
	
	@Parameter(required=true)
	String startCellKey = null;
	@Parameter(required=true)
	String outputKey=null;
	
	int limitEvents=20;
	
	LinkedList<short[]> previousStartCells = new LinkedList<short[]>();
	LinkedList<short[]> previousStopCells = new LinkedList<short[]>();
	LinkedList<int[]> previousUnixTimes = new LinkedList<int[]>();

	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		
		Utils.mapContainsKeys( input, startCellKey, "NROI", "UnixTimeUTC");
		
		int[] eventTime = (int[]) input.get("UnixTimeUTC");
		
		short[] startCellArray = (short[])input.get(startCellKey);
		short[] stopCellArray = new short[startCellArray.length];
		int length = (Integer) input.get("NROI");
		//calculate the stopcellArray for the current event
		for (int i = 0; i < startCellArray.length; ++i){
			//there are 1024 capacitors in the ringbuffer
			stopCellArray[i] = (short) ((startCellArray[i] + length)% 1024);
		}
		
		previousStartCells.addFirst(startCellArray);
		previousStopCells.addFirst(stopCellArray);
		previousUnixTimes.addFirst(eventTime);
		
		if (previousStartCells.size() > limitEvents)
		{
			previousStartCells.removeLast();
		}
		if (previousStopCells.size() > limitEvents)
		{
			previousStopCells.removeLast();
		}
		if (previousUnixTimes.size() > limitEvents)
		{
			previousUnixTimes.removeLast();
		}
		
		
		input.put(outputKey +"_start", previousStartCells);
		input.put(outputKey +"_stop", previousStopCells);
		input.put(outputKey+"_time", previousUnixTimes);
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
