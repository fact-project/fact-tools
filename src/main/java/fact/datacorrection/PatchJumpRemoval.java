package fact.datacorrection;

import java.util.LinkedList;

import fact.Utils;
import fact.mapping.ui.overlays.PixelSetOverlay;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
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
	String color=null;
	@Parameter(required=true)
	double jumpLimit=5.0;
	
	int leftBorder = 10;
	
	double spikeLimit = 7.0;
	
	double signalFlankLimit = 0.63;
	
	int lengthForFFT = 32;
	int lengthAfterPosForFFT = 10;
	
	int ringingPeriode = 11;
	
	double freqAmplLimit = 0.4;
	double freqCompAmplLimit = 2.0;
	
	double leftRingingFreq = 0.18;
	double rightRingingFreq = 0.22;
	
	double tau = -0.5;
	double constant = 14.454;
	double timeDependLimit = 10;
	
	double[] result = null;
	double[] averJumpHeights = null;
	int roi = 300;

    PixelSetOverlay pixelWithSpikes;
    PixelSetOverlay pixelWithSignalFlanks;
    PixelSetOverlay pixelWithRinging;
	PixelSetOverlay pixelWithCorrectedJumps;
	PixelSetOverlay pixelWithWrongTimeDepend;
	
	double[] fftResults = null;
	
	IntervalMarker[] posMarker;
	
	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		Utils.mapContainsKeys(this.getClass(), input, dataKey, prevEventsKey + "_start", prevEventsKey + "_stop", "NROI", startCellKey);
	
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
		
//		log.info("Events in previousStartCells before JumpRemoval: " + previousStartCells.size());

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
			
			pixelWithSpikes = new PixelSetOverlay();
			pixelWithSignalFlanks = new PixelSetOverlay();
			pixelWithRinging = new PixelSetOverlay();
			pixelWithCorrectedJumps = new PixelSetOverlay();
			pixelWithWrongTimeDepend = new PixelSetOverlay();
			
			averJumpHeights = new double[numberPatches];
			
			fftResults = new double[data.length];
			
			// Correct Jumps for each patch individual
			for (int patch=0 ; patch < numberPatches ; patch++)
			{
				averJumpHeights[patch] = 0;
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
					CreatePatchAverage(patch, patchAverage, patchDerivationAverage);
					
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
					isJump = CheckForJump(patch,pos,patchDerivationAverage,isStartCell);
					if (isJump == true)
					{
						isJump = CheckForSignalFlank(patch, pos, patchDerivationAverage,isStartCell);
						if (isJump == true)
						{
							isJump = CheckForRingingFFT(patch,pos,patchAverage,patchDerivationAverage);
								if (isJump == true)
								{
								isJump = CheckForTimeDependency(patch, deltaT);
								if (isJump == true)
								{
									stopLoop = false;
									CorrectJump(patch, pos, isStartCell);
								}
							}
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
////			input.put(outputJumpsKey+prevEvent+"Spikes", pixelWithSpikes);
////			input.put(outputJumpsKey+prevEvent+"SignalFlanks", pixelWithSignalFlanks);
////			input.put(outputJumpsKey+prevEvent+"Ringing", pixelWithRinging);
//			input.put(outputJumpsKey+prevEvent+"JumpsSet", pixelWithCorrectedJumps);
//			if (pixelWithWrongTimeDepend.size() > 0)
//			{
//				input.put(outputJumpsKey+prevEvent+"TimeSet", pixelWithWrongTimeDepend);
//			}
//			input.put(outputJumpsKey+prevEvent+"Marker", posMarker);
//			input.put(outputJumpsKey+prevEvent+"patchAverage", patchAverageCamera);
//			input.put(outputJumpsKey+prevEvent+"patchAverageDeriv", patchAverageDerivCamera);
//			input.put(outputJumpsKey+prevEvent+"fftResults", fftResults);
//			input.put("@"+Constants.KEY_COLOR + "_" +outputJumpsKey+prevEvent+"fftResults", "#0ACF1B");
		}
//		log.info("prevEvent: " + prevEvent);
		input.put(outputKey,result);
		input.put("@"+Constants.KEY_COLOR + "_" +outputKey,color);
		
		return input;
		
	}
	
	// Checks if there are spikes directly in front of the jump and/or after the jump.
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
						pixelWithSpikes.addById(pixel);
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
//						log.info("Found Double Spike On Jump: pixel: " + pixel + " correct to " + correctionValue);
						correctionValue = (result[pixel*roi+pos+2] + result[pixel*roi+pos+3]) / 2.0;
						// and correct pos+1 with the average of the following two slices:
						result[pixel*roi+pos+1] = correctionValue;
//						log.info("Found Double Spike On Jump: pixel: " + pixel + " correct to " + correctionValue);
						pixelWithSpikes.addById(pixel);
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
						pixelWithSpikes.addById(pixel);
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
					pixelWithSpikes.addById(pixel);
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
					pixelWithSpikes.addById(pixel);
				}
			}
		}
	}
	
	// Calculates the average of the result data array for a given patch. Also calulates the derivation of this patch average.
	// The timemarker channels are excluded from the averaging
	public void CreatePatchAverage(int patch, double[] patchAverage,double[] patchDerivationAverage){
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
	
	// Checks if there is a jump from the slices pos to pos+1
	public boolean CheckForJump(int patch,int pos,double[] derivation,boolean isStartCell){
		boolean isJump = false;
		averJumpHeights[patch] = 0;
		
		double derivAtJump = derivation[pos+1];
		
		if (isStartCell == false)
		{
			derivAtJump *= -1;
		}
		
		if (derivAtJump > jumpLimit)
		{
				isJump = true;
				averJumpHeights[patch] = derivation[pos+1];
		}
		return isJump;
	}
	
	public boolean CheckForTimeDependency(int patch, double deltaT){
		boolean timeDependIsCorrect = true;
		
		double predictedJumpHeight = constant*Math.pow(deltaT, tau);
		
//		log.info("Patch: " + patch + "deltaT: " + deltaT + " predJumpHeight: " + predictedJumpHeight + " averJumpHeight: " + averJumpHeights[patch]);
		
		if (Math.abs(averJumpHeights[patch]) > timeDependLimit * predictedJumpHeight)
		{
			timeDependIsCorrect = false;
			for (int px = 0 ; px < 9 ; px++)
			{
				pixelWithWrongTimeDepend.addById(patch*9+px);
			}
			averJumpHeights[patch] = 0;
		}
		
		return timeDependIsCorrect;
	}
	
	// Checks if the detected jumps comes from a photon signal flank 
	public boolean CheckForSignalFlank(int patch, int pos, double[] derivation, boolean isStartCell){
		boolean noSignalFlank = true;
		
		// Calculate the average derivation over 3 slices before and 3 slices after the jump
//		double averDerivBeforeJump = 0;
//		double averDerivAfterJump = 0;
		double averDerivAroundJump = 0;
		
		int counter = 0;
		for (int sl = pos ; sl > pos-2 && sl > leftBorder ; sl--)
		{
//			averDerivBeforeJump += derivation[sl];
			averDerivAroundJump += derivation[sl];
			counter += 1;
		}
//		averDerivBeforeJump /= counter;
		
//		counter = 0;
		for (int sl = pos+2 ; sl < pos+4 && sl < roi ; sl++)
		{
//			averDerivAfterJump += derivation[sl];
			averDerivAroundJump += derivation[sl];
			counter += 1;
		}
//		averDerivAfterJump /= counter;
		averDerivAroundJump /= counter;
		
		// If we have a stop cell, we only want to check if we have a falling flank. Therefore we multiply with -1, to have positive derivations.
		if (isStartCell == false)
		{
//			averDerivBeforeJump *= -1;
//			averDerivAfterJump *= -1;
			averDerivAroundJump *= -1;
		}
		
		// if either the average derivation before or after the jump is at the same level of the jump height we have a signal flank not a jump
//		if (averDerivBeforeJump > signalFlankLimit*Math.abs(averJumpHeights[patch]) || averDerivAfterJump > signalFlankLimit*Math.abs(averJumpHeights[patch]))
		if (averDerivAroundJump > signalFlankLimit*Math.abs(averJumpHeights[patch]))
		{
//			log.info("Flank found: patch: "+patch*9+" pos: "+pos+" averDerivBeforeJump: "+averDerivBeforeJump+" averJumpHeights: "+averJumpHeights[patch]+" averDerivAfterJump: "+averDerivAfterJump);
//			log.info("Flank found: patch: "+patch*9+" pos: "+pos+" averDerivAroundJump: "+averDerivAroundJump+" averJumpHeights: "+averJumpHeights[patch]);
			noSignalFlank = false;
			for (int px = 0 ; px < 9 ; px++)
			{
				pixelWithSignalFlanks.addById(patch*9+px);
			}
			averJumpHeights[patch] = 0;
		}
		
		return noSignalFlank;
	}
	
	// First check if there is a significant ringing in the dataArray, by performing a FFT on the dataArray, and checking the amplitude of the
	// frequency bins between 0.18 GHz and 0.22 GHz.
	// If there is ringing, reduce the jumpHeight by the average of the derivation one period of the ringing before and one period after the jump.
	// Return false if the resulting jumpHeight is smaller than the jumpLimit, and true if it is higher or no ringing was found.
	public boolean CheckForRingingFFT(int patch, int pos,double[] dataArray, double[] derivation){
		boolean noRinging = true;
		
		FastFourierTransformer fftObject = new FastFourierTransformer(DftNormalization.STANDARD);
	
		
		// we perform a FFT on 32 slices of the data array. This 32 slices have to be between ]leftBorder,roi[
		// if they are not in this region, the slices are shifted.
		int right = pos + lengthAfterPosForFFT;
		if (right > roi)
		{
			right = roi;
		}
		int left = right - lengthForFFT;
		if (left < leftBorder)
		{
			left = leftBorder;
			right = left + lengthForFFT;
		}
		double[] arrayForFFT = new double[lengthForFFT];
		for (int sl=0 ; sl < lengthForFFT ; sl++)
		{
			arrayForFFT[sl] = derivation[left+sl];
		}
		
		// Calculating the FFT of the data array
		Complex[] fftResult = fftObject.transform(arrayForFFT, TransformType.INVERSE);
		
		// Checking the average amplitude in the frequency bins corresponding to a frequency between [0.18 GHz,0.22 GHz]
		// Also the average amplitude in the lower frequency bins ([0 GHz, 0.18 GHz]) is calculated.  
		int freqBorderBinLeft = (int) (leftRingingFreq*lengthForFFT/2);
		int freqBorderBinRight = (int) (rightRingingFreq*lengthForFFT/2);
		double ringingFreqAmpl = 0.0;
		double comparisonFreqAmpl = 0.0;
		int bin = 0;
		for (; bin < freqBorderBinLeft ; bin++)
		{
			double real = fftResult[bin].getReal();
			double imag = fftResult[bin].getImaginary();
			comparisonFreqAmpl += Math.sqrt(real*real+imag*imag);
		}
		comparisonFreqAmpl /= freqBorderBinLeft;
		for ( ; bin <= freqBorderBinRight ; bin++)
		{
			double real = fftResult[bin].getReal();
			double imag = fftResult[bin].getImaginary();
			ringingFreqAmpl += Math.sqrt(real*real+imag*imag);
		}
		ringingFreqAmpl /= (freqBorderBinRight - freqBorderBinLeft + 1);
		
		// this is only for getting the results of the FFT in the viewer
		for (int sl=0 ; sl <= lengthForFFT/2 ; sl++)
		{
			double real = fftResult[sl].getReal();
			double imag = fftResult[sl].getImaginary();
			for (int px = 0 ; px < 9 ; px++)
			{
				fftResults[(patch*9+px)*roi+sl+left] = Math.sqrt(real*real+imag*imag);
			}
		}
		
		// now check whether the amplitude of the ringing frequencies are larger than a given limit and also the comparison frequency
		// is smaller than another limit, to exclude signals with a large deviation, which could fake high amplitude also in the ringing
		// frequencies
		if (ringingFreqAmpl > freqAmplLimit && comparisonFreqAmpl < freqCompAmplLimit)
		{		
			// Now we calculate the average derivation one ringing period before the jump (if it is not smaller than the leftBorder) and
			// one period after the jump (if it is not larger than the roi)
			double averRingingDerviation = 0.0;
			double counter = 0;
			if ((pos+1-ringingPeriode) > leftBorder )
			{
				averRingingDerviation += derivation[pos+1-11];
				counter += 1;
			}
			if ((pos+1+ringingPeriode) < roi )
			{
				averRingingDerviation += derivation[pos+1+11];
				counter += 1;
			}
			averRingingDerviation /= counter;
			
			averJumpHeights[patch] -= averRingingDerviation;
			for (int px = 0 ; px < 9 ; px++)
			{
				pixelWithRinging.addById(patch*9+px);
			}

//			log.info("Ringing found, pixel: " + patch*9 +  " freqAmpl: " + ringingFreqAmpl + " compAmpl: " + comparisonFreqAmpl);
//			log.info("FreqBins: (" + freqBorderBinLeft + "," + freqBorderBinRight + ")");
//			log.info("Reduced jump Height from: " + (averJumpHeights[patch]+averRingingDerviation) + " to " + averJumpHeights[patch] + " (jumplimit is: " + jumpLimit + ")");
			if (Math.abs(averJumpHeights[patch]) < jumpLimit)
			{
				averJumpHeights[patch] = 0;
				noRinging = false;
			}
		}
		return noRinging;
	}
	
	// Correct the data array for a jump. In case of a jump at a previous start cells, all slices beginning with pos+1 are reduced
	// about the jump height, in case of a jump at a previous stopp cell, all slices up to pos are reduced about the jump height.

	public void CorrectJump(int patch,int pos,boolean isStartCell){
		int leftBorder = pos+1;
		int rightBorder = roi;
		
		double jumpHeight = Math.abs(averJumpHeights[patch]);
		
		for (int px = 0 ; px < 9 ; px++)
		{
			pixelWithCorrectedJumps.addById(patch*9+px);
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

	public double getSignalFlankLimit() {
		return signalFlankLimit;
	}

	public void setSignalFlankLimit(double signalFlankLimit) {
		this.signalFlankLimit = signalFlankLimit;
	}

	public int getLengthForFFT() {
		return lengthForFFT;
	}

	public void setLengthForFFT(int lengthForFFT) {
		this.lengthForFFT = lengthForFFT;
	}

	public int getLengthAfterPosForFFT() {
		return lengthAfterPosForFFT;
	}

	public void setLengthAfterPosForFFT(int lengthAfterPosForFFT) {
		this.lengthAfterPosForFFT = lengthAfterPosForFFT;
	}

	public int getRingingPeriode() {
		return ringingPeriode;
	}

	public void setRingingPeriode(int ringingPeriode) {
		this.ringingPeriode = ringingPeriode;
	}

	public double getLeftRingingFreq() {
		return leftRingingFreq;
	}

	public void setLeftRingingFreq(double leftRingingFreq) {
		this.leftRingingFreq = leftRingingFreq;
	}

	public double getRightRingingFreq() {
		return rightRingingFreq;
	}

	public void setRightRingingFreq(double rightRingingFreq) {
		this.rightRingingFreq = rightRingingFreq;
	}

	public double getFreqAmplLimit() {
		return freqAmplLimit;
	}

	public void setFreqAmplLimit(double freqAmplLimit) {
		this.freqAmplLimit = freqAmplLimit;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public double getTau() {
		return tau;
	}

	public void setTau(double tau) {
		this.tau = tau;
	}

	public double getConstant() {
		return constant;
	}

	public void setConstant(double constant) {
		this.constant = constant;
	}

	public double getTimeDependLimit() {
		return timeDependLimit;
	}

	public void setTimeDependLimit(double timeDependLimit) {
		this.timeDependLimit = timeDependLimit;
	}
	
}
