package fact.filter;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PatchJumpRemoval implements Processor {
	static Logger log = LoggerFactory.getLogger(PatchJumpRemoval.class);
	
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
		EventUtils.mapContainsKeys(this.getClass(), input, dataKey,prevStartAndStopCellKey+"_start",prevStartAndStopCellKey+"_stop","NROI",startCellKey);
		
		int eventNum = (Integer) input.get("EventNum");
		log.info("EventNum: " + eventNum);
		
		int roi = (Integer) input.get("NROI");
		short[] currentStartCells = (short[]) input.get(startCellKey);
		double[] data = (double[]) input.get(dataKey);
		
		double[] result = new double[data.length];
		System.arraycopy(data, 0, result, 0, data.length);
		int numberPatches = Constants.NUMBEROFPIXEL / 9;
//		int numberPatches = 1;
		
		@SuppressWarnings("unchecked")
		LinkedList<short[]> previousStartCells = (LinkedList<short[]>) input.get(prevStartAndStopCellKey+"_start");
		@SuppressWarnings("unchecked")
		LinkedList<short[]> previousStopCells = (LinkedList<short[]>) input.get(prevStartAndStopCellKey+"_stop");
		
		boolean stopLoop = false;
		
		log.info("Events in previousStartCells before JumpRemoval: " + previousStartCells.size());

		// Loop over all previous Events
		int prevEvent=1;
		for ( ; prevEvent < previousStartCells.size() && stopLoop == false ; prevEvent++)
		{
			short[] currPrevStartCells = previousStartCells.get(prevEvent);
			short[] currPrevStopCells = previousStopCells.get(prevEvent);
			
			// we only want to go on when at least one pixel was corrected (so the jumpheight is larger than the jumpLimit) or
			// previous start and stop cells aren't in the ROI
			stopLoop = true;
			boolean printedLogMessage=false;
			
			// Correct Jumps for each patch individual
			for (int patch=0 ; patch < numberPatches ; patch++)
			{
				// Check for an upgoing jump 2 slices after the previous startcell
//				System.out.println("Patch: " + patch + " StaC: " + currentStartCells[patch*9] + " pStaC: " + currPrevStartCells[patch*9]);
				short pos = (short) ((currPrevStartCells[patch*9] - currentStartCells[patch*9] + 1024 + 2)%1024);
//				System.out.println("Patch: " + patch + " StartCellPosition: " + pos);
				if (pos < roi)
				{
					double averJumpHeight = 0;
					// calculate the average jumpHeight for a patch, leave out the timemarker channel
					for (int px = 0 ; px < 8 ; px++)
					{
						int pixel = 9*patch + px;
						averJumpHeight+=result[pixel*roi+pos+1]-result[pixel*roi+pos];
					}
					averJumpHeight /= 8; 
//					System.out.println("Patch: " + patch + " averJumpheight: " + averJumpHeight);

					if (averJumpHeight>jumpLimit)
					{
						if (printedLogMessage == false)
						{
							int pixel = 9*patch;
							log.info("Jump at StartCell, prevEvent: " + prevEvent + " pos: " + pos + " height: " + averJumpHeight);
							log.info("Pixel: " + pixel + " SoftID: " + DefaultPixelMapping.getSoftwareID(pixel) + " geom: (" + DefaultPixelMapping.getGeomX(pixel) + "," + DefaultPixelMapping.getGeomY(pixel) + ")");
							printedLogMessage = true;
						}
						stopLoop = false;
						for (int px = 0 ; px < 9 ; px++)
						{
							int pixel = 9*patch + px;
							for (int slice=(int)pos + 1 ; slice < roi ; slice++)
							{
								result[pixel*roi+slice] -= averJumpHeight;
							}
						}
//						System.out.println("Substract Startjump, result nachher: " + result[9*patch+pos]);
					}
				}
				else
				{
					stopLoop = false;
				}
				// Check for a downgoing jump 9 slices after the previous stopcell
//				System.out.println("Patch: " + patch + " StaC: " + (currentStartCells[patch*9]) + " pStoC: " + currPrevStopCells[patch*9]);
				pos = (short) ((currPrevStopCells[patch*9] - (currentStartCells[patch*9])  + 1024 + 9)%1024);
//				System.out.println("Patch: " + patch + " StopCellPosition: " + pos);
				if (pos < roi)
				{
					double averJumpHeight = 0;
					for (int px = 0 ; px < 8 ; px++)
					{
						int pixel = 9*patch + px;
						averJumpHeight+=(result[pixel*roi+pos+1]-result[pixel*roi+pos]);
					}
					averJumpHeight /= 8; 
//					System.out.println("Patch: " + patch + " averJumpheight: " + averJumpHeight);
					if ((-averJumpHeight)>jumpLimit)
					{
						if (printedLogMessage == false)
						{
							int pixel = 9*patch;
							log.info("Jump at StopCell, prevEvent: " + prevEvent + " pos: " + pos + " height: " + averJumpHeight);
							log.info("Pixel: " + pixel + " SoftID: " + DefaultPixelMapping.getSoftwareID(pixel) + " geom: (" + DefaultPixelMapping.getGeomX(pixel) + "," + DefaultPixelMapping.getGeomY(pixel) + ")");
							printedLogMessage = true;
						}
						stopLoop = false;
//						System.out.println("Substract Stopjump, result vorher: " + result[9*patch]);
						for (int px = 0 ; px < 9 ; px++)
						{
							int pixel = 9*patch + px;
							for (int slice=0 ; slice <= (int)pos ; slice++)
							{
								result[pixel*roi+slice] += averJumpHeight;
							}
						}
//						System.out.println("Substract Stopjump, result nachher: " + result[9*patch]);
					}
				}
				else
				{
					stopLoop = false;
				}
			}
		}
//		 If we stopped the for loop cause stopLoop was true, we want to remove the remaining previousStartCells
		while ((prevEvent+1) < previousStartCells.size())
		{
			log.info("Remove Event nr. " + prevEvent + " from previousStartCells");
			previousStartCells.removeLast();
			previousStopCells.removeLast();
		}
		log.info("Events in previousStartCells after JumpRemoval: " + previousStartCells.size());
		
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
