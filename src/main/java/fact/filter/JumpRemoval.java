package fact.filter;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.EventUtils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class JumpRemoval implements Processor {
	static Logger log = LoggerFactory.getLogger(JumpRemoval.class);
	
	@Parameter(required=true)
	String dataKey=null;
	@Parameter(required=true)
	String outputKey=null;
	@Parameter(required=true)
	String prevStartAndStopCellKey=null;
	@Parameter(required=true)
	String startCellKey=null;
	@Parameter(required=true)
	double jumpLimit=5.0;

	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		EventUtils.mapContainsKeys(this.getClass(), input, dataKey,prevStartAndStopCellKey+"_start",prevStartAndStopCellKey+"_stopp","NROI",startCellKey);
		
		int roi = (Integer) input.get("NROI");
		short[] currentStartCells = (short[]) input.get(startCellKey);
		short[] data = (short[]) input.get(dataKey);
		
		short[] result = new short[data.length];
		System.arraycopy(data, 0, result, 0, data.length);
		
		@SuppressWarnings("unchecked")
		LinkedList<short[]> previousStartCells = (LinkedList<short[]>) input.get(prevStartAndStopCellKey+"_start");
		@SuppressWarnings("unchecked")
		LinkedList<short[]> previousStopCells = (LinkedList<short[]>) input.get(prevStartAndStopCellKey+"_stopp");
		
		// Loop over all previous Events
		for (int prevEvent=0 ; prevEvent < previousStartCells.size(); prevEvent++)
		{
			short[] currPrevStartCells = previousStartCells.get(prevEvent);
			short[] currPrevStoppCells = previousStopCells.get(prevEvent);
			
			// Correct Jumps for each pixel individual
			for (int px=0 ; px < Constants.NUMBEROFPIXEL ; px++)
			{
				// Check for an upgoing jump 3 slices after the previous startcell
				short pos = (short) ((currentStartCells[px] - currPrevStartCells[px] + 1024)%1024 + 3);
				if (pos < roi)
				{
					double jumpHeight=data[px*roi+pos]-data[px*roi+pos+1];
					if (Math.abs(jumpHeight)>jumpLimit)
					{
						for (int slice=(int)pos + 1 ; slice < roi ; slice++)
						{
							result[px*roi+slice] -= jumpHeight;
						}
					}
				}
				// Check for a downgoing jump 9 slices after the previous stoppcell
				pos = (short) ((currentStartCells[px]+roi - currPrevStoppCells[px] + 1024)%1024 + 9);
				if (pos < roi)
				{
					double jumpHeight=data[px*roi+pos]-data[px*roi+pos+1];
					if (Math.abs(jumpHeight)>jumpLimit)
					{
						for (int slice=0 ; slice < (int)pos ; slice++)
						{
							result[px*roi+slice] -= Math.abs(jumpHeight);
						}
					}
				}
			}
		}
		
		
		input.put(outputKey, result);
		
		return input;
	}

}
