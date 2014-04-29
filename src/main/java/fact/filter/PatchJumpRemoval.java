package fact.filter;

import java.util.LinkedList;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.EventUtils;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;
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
	String outputJumpsKey=null;
	@Parameter(required=true)
	String prevEventsKey=null;
	@Parameter(required=true)
	String startCellKey=null;
	@Parameter(required=true)
	double jumpLimit=5.0;
	
	int leftBorder = 10;
	
	double spikeLimit = 16.0;
	
	double[] result = null;
	double[] averJumpHeights = null;
	int roi = 300;
	
	PixelSet pixelWithSpikes;
	PixelSet pixelWithSignalFlanks;
	PixelSet pixelWithRinging;
	PixelSet pixelWithCorrectedJumps;
	
	IntervalMarker[] posMarker;
	
	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		EventUtils.mapContainsKeys(this.getClass(), input, dataKey,prevEventsKey+"_start",prevEventsKey+"_stop","NROI",startCellKey);
	
		int[] currentTime = (int[]) input.get("UnixTimeUTC");
		
		roi = (Integer) input.get("NROI");
		short[] currentStartCells = (short[]) input.get(startCellKey);
		double[] data = (double[]) input.get(dataKey);
		
		result = new double[data.length];
		System.arraycopy(data, 0, result, 0, data.length);
		int numberPatches = Constants.NUMBEROFPIXEL / 9;
		
		@SuppressWarnings("unchecked")
		LinkedList<short[]> previousStartCells = (LinkedList<short[]>) input.get(prevEventsKey+"_start");
		@SuppressWarnings("unchecked")
		LinkedList<short[]> previousStopCells = (LinkedList<short[]>) input.get(prevEventsKey+"_stop");
		@SuppressWarnings("unchecked")
		LinkedList<int[]> previousTimes = (LinkedList<int[]>) input.get(prevEventsKey+"_time");
		
		boolean stopLoop = false;
		
		log.info("Events in previousStartCells before JumpRemoval: " + previousStartCells.size());

		// Loop over all previous Events
		int prevEvent=1; // we start at 1, cause the startcells of the current Event are already filled in the previousStartCells list
		for ( ; prevEvent < previousStartCells.size() && stopLoop == false ; prevEvent++)
		{
			short[] currPrevStartCells = previousStartCells.get(prevEvent);
			short[] currPrevStopCells = previousStopCells.get(prevEvent);
			int[] currPrevTime = previousTimes.get(prevEvent);
			
			posMarker = new IntervalMarker[Constants.NUMBEROFPIXEL];
			
			double[] patchAverageCamera = new double[data.length];
			double[] patchAverageDerivCamera = new double[data.length];
			
			double deltaT = (double)(currentTime[0]-currPrevTime[0])*1000.0+(double)(currentTime[1]-currPrevTime[1])/1000.0;
			
			// we only want to go on when at least one pixel was corrected (so the jumpheight is larger than the jumpLimit) or
			// previous start and stop cells aren't in the ROI
			stopLoop = true;
			
			pixelWithSpikes = new PixelSet();
			pixelWithSignalFlanks = new PixelSet();
			pixelWithRinging = new PixelSet();
			pixelWithCorrectedJumps = new PixelSet();
			
			averJumpHeights = new double[numberPatches];
			
			// Correct Jumps for each patch individual
			for (int patch=0 ; patch < numberPatches ; patch++)
			{
				averJumpHeights[patch] = 0;
				// Check for an up going jump 2 slices after the previous startcell
				boolean isStartCell=true;
				boolean posInROI=false;
				short pos = (short) ((currPrevStartCells[patch*9] - currentStartCells[patch*9] + 1024 + 2)%1024);
				if (pos < (roi-2) && pos > leftBorder)
				{
					isStartCell=true;
					posInROI=true;
				}
				else
				{
					pos = (short) ((currPrevStopCells[patch*9] - (currentStartCells[patch*9])  + 1024 + 9)%1024);
					if (pos < (roi-2) && pos > leftBorder)
					{
						isStartCell=false;
						posInROI=true;
					}
				}
				if (posInROI == true)
				{
					HandleSpike(patch, pos);
					
					// create patch voltage average array and patch voltage derivation array:
					double[] patchAverage = new double[roi];
					double[] patchDerivationAverage = new double[roi];
					CreatePatchAverage(patchAverage, patchDerivationAverage, patch);
					
					for (int sl = 0 ; sl < roi ; sl++)
					{
						for (int px = 0 ; px < 9 ; px++)
						{
							int pixel = patch*9+px;
							patchAverageCamera[pixel*roi+sl] = patchAverage[sl];
							patchAverageDerivCamera[pixel*roi+sl] = patchDerivationAverage[sl];
						}
					}
					
					boolean isJump=false;
					
					isJump = CheckForJump(patchDerivationAverage,pos,isStartCell,patch);
					
					if (isJump == true)
					{
						isJump = CheckForRinging();
						if (isJump == true)
						{
							stopLoop = false;
							CorrectJump(pos, patch, isStartCell);
						}
					}
				}
				else
				{
					stopLoop = false;
				}
			}
			input.put(outputJumpsKey+prevEvent+"Jumps", averJumpHeights);
			input.put(outputJumpsKey+prevEvent+"Time", deltaT);
//			input.put(outputJumpsKey+prevEvent+"Spikes", pixelWithSpikes);
			input.put(outputJumpsKey+prevEvent+"SignalFlanks", pixelWithSignalFlanks);
			input.put(outputJumpsKey+prevEvent+"Ringing", pixelWithRinging);
			input.put(outputJumpsKey+prevEvent+"JumpsSet", pixelWithCorrectedJumps);
			input.put(outputJumpsKey+prevEvent+"Marker", posMarker);
			input.put(outputJumpsKey+prevEvent+"patchAverage", patchAverageCamera);
			input.put(outputJumpsKey+prevEvent+"patchAverageDeriv", patchAverageDerivCamera);
		}
		input.put(outputKey,result);
		
		return input;
		
	}
	
	// Check if there are spikes directly in front of the jump and/or after the jump.
	// Correct this jump if necessary
	public void HandleSpike(int patch,int pos){
		
		for (int px = 0 ; px < 8 ; px++)
		{
			int pixel = patch*9+px;
			
			posMarker[pixel] = new IntervalMarker(pos,pos+1);
			
			// Check for a double spike in front of the jump
			// properties of a double spike: large jump up, small step, large jump down
			double diff = result[pixel*roi+pos-1] - result[pixel*roi+pos-2];
			if (diff > spikeLimit)
			{
//				double diff2 = result[pixel*roi+pos] - result[pixel*roi+pos-1];
//				if (Math.abs(diff2) < spikeLimit*0.3)
//				{
					double diff3 = result[pixel*roi+pos+1] - result[pixel*roi+pos];
					if (-diff3 > spikeLimit)
					{
						// double spike in slices pos-1 and pos found, correct it with the average of the previous two slices:
						double correctionValue = (result[pixel*roi+pos-3] + result[pixel*roi+pos-2]) / 2.0;
						result[pixel*roi+pos-1] = correctionValue;
						result[pixel*roi+pos] = correctionValue;
//						log.info("Found Double Spike before Jump: pixel: " + pixel + " correct to " + correctionValue);
						pixelWithSpikes.add(new Pixel(pixel));
					}
//				}
			}
			
			// Check for a double spike on the jump
			// properties of a double spike: large jump up, small step, large jump down
			diff = result[pixel*roi+pos] - result[pixel*roi+pos-1];
			if (diff > spikeLimit)
			{
//				double diff2 = result[pixel*roi+pos+1] - result[pixel*roi+pos];
//				if (Math.abs(diff2) < spikeLimit*0.3)
//				{
					double diff3 = result[pixel*roi+pos+2] - result[pixel*roi+pos+1];
					if (-diff3 > spikeLimit)
					{
						// double spike in slices pos and pos+1 found, correct pos with the average of the previous two slices:
						double correctionValue = (result[pixel*roi+pos-2] + result[pixel*roi+pos-1]) / 2.0;
						result[pixel*roi+pos] = correctionValue;
						log.info("Found Double Spike On Jump: pixel: " + pixel + " correct to " + correctionValue);
						correctionValue = (result[pixel*roi+pos+2] + result[pixel*roi+pos+3]) / 2.0;
						// and correct pos+1 with the average of the following two slices:
						result[pixel*roi+pos+1] = correctionValue;
						log.info("Found Double Spike On Jump: pixel: " + pixel + " correct to " + correctionValue);
						pixelWithSpikes.add(new Pixel(pixel));
					}
//				}
			}
			
			
			// Check for a double spike after the jump
			// properties of a double spike: large jump up, small step, large jump down
			diff = result[pixel*roi+pos+1] - result[pixel*roi+pos];
			if (diff > spikeLimit)
			{
//				double diff2 = result[pixel*roi+pos+2] - result[pixel*roi+pos+1];
//				if (Math.abs(diff2) < spikeLimit*0.3)
//				{
					double diff3 = result[pixel*roi+pos+3] - result[pixel*roi+pos+2];
					if (-diff3 > spikeLimit)
					{
						// double spike in slices pos+1 and pos+2 found, correct it with the average of the following two slices:
						double correctionValue = (result[pixel*roi+pos+3] + result[pixel*roi+pos+4]) / 2.0;
						result[pixel*roi+pos+1] = correctionValue;
						result[pixel*roi+pos+2] = correctionValue;
//						log.info("Found Double Spike after Jump: pixel: " + pixel + " correct to " + correctionValue);
						pixelWithSpikes.add(new Pixel(pixel));
					}
//				}
			}
			

			
			// Check for a single spike in front of the jump
			// properties of a single spike: large jump up, large jump down
			diff = result[pixel*roi+pos] - result[pixel*roi+pos-1];
			if (diff > spikeLimit)
			{
				double diff2 = result[pixel*roi+pos+1] - result[pixel*roi+pos];
				if (-diff2 > spikeLimit)
				{
					// single spike in slices pos found, correct it with the average of the previous two slices:
					double correctionValue = (result[pixel*roi+pos-2] + result[pixel*roi+pos-1]) / 2.0;
					result[pixel*roi+pos] = correctionValue;
//					log.info("Found Single Spike befor Jump: pixel: " + pixel + " correct to " + correctionValue);
					pixelWithSpikes.add(new Pixel(pixel));
				}
			}
			
			// Check for a single spike in front of the jump
			// properties of a single spike: large jump up, large jump down
			diff = result[pixel*roi+pos+1] - result[pixel*roi+pos];
			if (diff > spikeLimit)
			{
				double diff2 = result[pixel*roi+pos+2] - result[pixel*roi+pos+1];
				if (-diff2 > spikeLimit)
				{
					// single spike in slices pos+1 found, correct it with the average of the following two slices:
					double correctionValue = (result[pixel*roi+pos+2] + result[pixel*roi+pos+3]) / 2.0;
					result[pixel*roi+pos+1] = correctionValue;
//					log.info("Found Single Spike after Jump: pixel: " + pixel + " correct to " + correctionValue);
					pixelWithSpikes.add(new Pixel(pixel));
				}
			}
		}
	}
	
	public void CreatePatchAverage(double[] patchAverage,double[] patchDerivationAverage,int patch){
		for (int sl=0 ; sl < roi ; sl++)
		{
			for (int px=0 ; px < 8 ; px++)
			{
				int slice = (patch*9+px)*roi+sl;
				patchAverage[sl] += result[slice];
			}
			patchAverage[sl] /= 8;
			if (sl>0)
			{
				patchDerivationAverage[sl] = patchAverage[sl] - patchAverage[sl-1];
			}
		}
	}
	
	// Check if there is a jump on pos, if the jump is large enough to correct it and if this jumps only occurs from a flank of a large photon signal
	// If so isJump is changed to true and jumpHeight to the calculated jumpHeight
	public boolean CheckForJump(double[] derivation,int pos,boolean isStartCell,int patch){
		boolean isJump = false;
		averJumpHeights[patch] = 0;
		
		double derivBeforeJump = derivation[pos];
		double derivAtJump = derivation[pos+1];
		double derivAfterJump = derivation[pos+2];
		
		// if we are checking for a jump down, we multiply the height with -1 and than use the same check as for a jump up
		if (isStartCell == false)
		{
			derivBeforeJump *= -1;
			derivAtJump *= -1;
			derivAfterJump *= -1;
		}
		
		if (derivAtJump > jumpLimit)
		{
			// if there is a flank of a photon signal, the derivation before and after the jump should be in the same order (or larger) as at the jump
			if (derivBeforeJump > derivAtJump*0.8 && derivAfterJump > derivAtJump*0.8)
			{
//				log.info("Signal Flank: pixel: " + patch*9 + " derivBeforeJump: " + derivBeforeJump + " derivAtJump: " + derivAtJump + " derivAfterJump: " + derivAfterJump + " startCell? " + isStartCell);
				for (int px = 0 ; px < 9 ; px++)
				{
					pixelWithSignalFlanks.add(new Pixel(patch*9+px));
				}
			}
			else
			{
				isJump = true;
				averJumpHeights[patch] = derivAtJump;
				log.info("Jump: pixel: " + patch*9 + " jumpHeight: " + averJumpHeights[patch] + " startCell? " + isStartCell);
			}
		}
		return isJump;
	}
	
	public boolean CheckForRinging(){
		
		return true;
	}

	public void CorrectJump(int pos,int patch,boolean isStartCell){
		int leftBorder = pos+1;
		int rightBorder = roi;
		
		double jumpHeight = Math.abs(averJumpHeights[patch]);
		
		for (int px = 0 ; px < 9 ; px++)
		{
			pixelWithCorrectedJumps.add(new Pixel(patch*9+px));
		}
		
		if (isStartCell == false)
		{
			leftBorder = 0;
			rightBorder = pos+1;
		}
		
		for (int px = 0 ; px < 9 ; px++)
		{
			int pixel = 9*patch + px;
			for (int slice=leftBorder ; slice < rightBorder ; slice++)
			{
				result[pixel*roi+slice] -= jumpHeight;
			}
		}
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

	public String getOutputJumpsKey() {
		return outputJumpsKey;
	}

	public void setOutputJumpsKey(String outputJumpsKey) {
		this.outputJumpsKey = outputJumpsKey;
	}

	public String getPrevEventsKey() {
		return prevEventsKey;
	}

	public void setPrevEventsKey(String prevEventsKey) {
		this.prevEventsKey = prevEventsKey;
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

	public int getLeftBorder() {
		return leftBorder;
	}

	public void setLeftBorder(int leftBorder) {
		this.leftBorder = leftBorder;
	}

	public double getSpikeLimit() {
		return spikeLimit;
	}

	public void setSpikeLimit(double spikeLimit) {
		this.spikeLimit = spikeLimit;
	}
	
}
