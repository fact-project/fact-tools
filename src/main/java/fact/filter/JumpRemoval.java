package fact.filter;

import java.util.LinkedList;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
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
		Utils.mapContainsKeys(this.getClass(), input, dataKey, prevStartAndStopCellKey + "_start", prevStartAndStopCellKey + "_stop", "NROI", startCellKey);
		
		int roi = (Integer) input.get("NROI");
		short[] currentStartCells = (short[]) input.get(startCellKey);
		double[] data = (double[]) input.get(dataKey);
		
		double[] result = new double[data.length];
		System.arraycopy(data, 0, result, 0, data.length);
		
		@SuppressWarnings("unchecked")
		LinkedList<short[]> previousStartCells = (LinkedList<short[]>) input.get(prevStartAndStopCellKey+"_start");
		@SuppressWarnings("unchecked")
		LinkedList<short[]> previousStopCells = (LinkedList<short[]>) input.get(prevStartAndStopCellKey+"_stop");
		
		boolean stopLoop = false;
		// Loop over all previous Events
		int prevEvent=0;
		for ( ; prevEvent < previousStartCells.size() && stopLoop == false ; prevEvent++)
		{
			short[] currPrevStartCells = previousStartCells.get(prevEvent);
			short[] currPrevStopCells = previousStopCells.get(prevEvent);
			
			// we only want to go on when at least one pixel was corrected (so the jumpheight is larger than the jumpLimit) or
			// previous start and stop cells aren't in the ROI
			stopLoop = true;
			
			// Correct Jumps for each pixel individual
			for (int px=0 ; px < Constants.NUMBEROFPIXEL ; px++)
			{
				// Check for an upgoing jump 3 slices after the previous startcell
				short pos = (short) ((currPrevStartCells[px] - currentStartCells[px] + 1024 + 3)%1024);
				if (pos < roi)
				{
					double jumpHeight=data[px*roi+pos]-data[px*roi+pos+1];
					if (Math.abs(jumpHeight)>jumpLimit)
					{
						stopLoop = false;
						for (int slice=(int)pos + 1 ; slice < roi ; slice++)
						{
							result[px*roi+slice] -= jumpHeight;
						}
					}
				}
				else
				{
					stopLoop = false;
				}
				// Check for a downgoing jump 9 slices after the previous stopcell
				pos = (short) ((currPrevStopCells[px]+roi - currentStartCells[px] + 1024 + 9)%1024);
				if (pos < roi)
				{
					double jumpHeight=data[px*roi+pos]-data[px*roi+pos+1];
					if (Math.abs(jumpHeight)>jumpLimit)
					{
						stopLoop = false;
						for (int slice=0 ; slice < (int)pos ; slice++)
						{
							result[px*roi+slice] -= Math.abs(jumpHeight);
						}
					}
				}
				else
				{
					stopLoop = false;
				}
			}
		}
		// If we stopped the for loop cause stopLoop was true, we want to remove the remaining previousStartCells
//		while (prevEvent < previousStartCells.size())
//		{
//			previousStartCells.removeLast();
//			previousStopCells.removeLast();
//		}
		
		input.put(outputKey, result);
		input.put(prevStartAndStopCellKey +"_start", previousStartCells);
		input.put(prevStartAndStopCellKey +"_stop", previousStopCells);
		
		return input;
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public String getPrevStartAndStopCellKey() {
		return prevStartAndStopCellKey;
	}

	public void setPrevStartAndStopCellKey(String prevStartAndStopCellKey) {
		this.prevStartAndStopCellKey = prevStartAndStopCellKey;
	}

	public String getStartCellKey() {
		return startCellKey;
	}

	public void setStartCellKey(String startCellKey) {
		this.startCellKey = startCellKey;
	}

	public double getJumpLimit() {
		return jumpLimit;
	}

	public void setJumpLimit(double jumpLimit) {
		this.jumpLimit = jumpLimit;
	}

}
