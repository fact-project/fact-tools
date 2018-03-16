package fact.datacorrection;

import fact.Constants;
import fact.Utils;
import fact.container.JumpInfos;
import fact.container.PreviousEventInfoContainer;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Removes artificial effects called "jumps" on a per patch basis.
 * TODO: Refactor previous startcell stuff to be put into a container class
 *
 * @author Fabian Temme
 */
public class PatchJumpRemoval implements Processor {
    static Logger log = LoggerFactory.getLogger(PatchJumpRemoval.class);

    @Parameter(required = true)
    public String dataKey = null;

    @Parameter(required = true)
    public String outputKey = null;

    @Parameter(required = false, description = "Useful for jump studies")
    public String outputJumpsKey = null;

    @Parameter(required = true)
    public String prevEventsKey = null;

    @Parameter(required = true)
    public String startCellKey = null;

    @Parameter(required = false, description = "The key containing the UnixTimeUTC")
    public String unixTimeKey = "UnixTimeUTC";

    @Parameter
    public double jumpLimit = 5.0;

    @Parameter
    double spikeLimit = 7.0;

    @Parameter
    int leftBorder = 10;

    @Parameter
    double signalFlankLimit = 0.63;

    @Parameter
    int lengthForFFT = 32;

    @Parameter
    int lengthAfterPosForFFT = 10;

    @Parameter
    int ringingPeriode = 11;

    @Parameter
    double freqAmplLimit = 0.4;

    @Parameter
    double freqCompAmplLimit = 2.0;

    @Parameter
    double leftRingingFreq = 0.18;

    @Parameter
    double rightRingingFreq = 0.22;

    @Parameter
    double tau = -0.5;

    @Parameter
    double constant = 14.454;

    @Parameter
    double timeDependLimit = 10;

    @Parameter
    boolean addJumpInfos = false;

    JumpInfos jumpInfos;

    int roi;

    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, dataKey, double[].class);
        Utils.isKeyValid(item, prevEventsKey, PreviousEventInfoContainer.class);
        Utils.isKeyValid(item, startCellKey, short[].class);
        Utils.isKeyValid(item, "NROI", Integer.class);
        Utils.isKeyValid(item, unixTimeKey, int[].class);

        // Get variables out of data item
        int[] currentTime = (int[]) item.get(unixTimeKey);
        roi = (Integer) item.get("NROI");
        short[] currentStartCells = (short[]) item.get(startCellKey);
        double[] data = (double[]) item.get(dataKey);
        PreviousEventInfoContainer prevEventInfo = (PreviousEventInfoContainer) item.get(prevEventsKey);

        double[] result = new double[data.length];
        System.arraycopy(data, 0, result, 0, data.length);

        int numberPatches = Constants.N_PIXELS / 9;

        boolean stopLoop = false;

        // Loop over all previous Events
        int prevEvent = 1; // we start at 1, cause the startcells of the current Event are already filled in the prevEventInfo
        for (; prevEvent < prevEventInfo.getListSize() && stopLoop == false; prevEvent++) {
            short[] currPrevStartCells = prevEventInfo.getPrevStartCells(prevEvent);
            short[] currPrevStopCells = prevEventInfo.getPrevStoppCells(prevEvent);
            int[] currPrevTime = prevEventInfo.getPrevUnixTimeCells(prevEvent);

            double deltaT = (double) (currentTime[0] - currPrevTime[0]) * 1000.0 + (double) (currentTime[1] - currPrevTime[1]) / 1000.0;
            jumpInfos = new JumpInfos(Constants.N_PIXELS, numberPatches, roi);

            // we only want to go on when at least one pixel was corrected (so the jumpheight is larger than the jumpLimit) or
            // previous start and stop cells aren't in the ROI
            stopLoop = true;

            // Correct Jumps for each patch individual
            for (int patch = 0; patch < numberPatches; patch++) {
                boolean isStartCell = true;
                boolean posInROI = false;
                short pos = (short) ((currPrevStartCells[patch * 9] - currentStartCells[patch * 9] + 1024 + 2) % 1024);
                if (pos < (roi - 2) && pos > leftBorder) {
                    isStartCell = true;
                    posInROI = true;
                } else {
                    pos = (short) ((currPrevStopCells[patch * 9] - (currentStartCells[patch * 9]) + 1024 + 9) % 1024);
                    if (pos < (roi - 2) && pos > leftBorder) {
                        isStartCell = false;
                        posInROI = true;
                    }
                }
                if (posInROI == true) {
                    if (addJumpInfos == true) {
                        jumpInfos.addPosMarkerForPatch(patch, pos, isStartCell);
                    }
                    result = HandleSpike(patch, pos, result, jumpInfos);


                    // create patch voltage average array and patch voltage derivation array:
                    double[] patchAverage = new double[roi];
                    double[] patchDerivationAverage = new double[roi];
                    createPatchAverage(patch, patchAverage, patchDerivationAverage, result);

                    double jumpHeight = checkForJump(patch, pos, patchDerivationAverage, isStartCell);
                    if (jumpHeight != 0) {
                        boolean isJump = false;
                        isJump = checkForSignalFlank(patch, pos, patchDerivationAverage, isStartCell, jumpHeight);
                        if (isJump == true) {
                            jumpHeight = checkForRingingFFT(patch, pos, patchAverage, patchDerivationAverage, jumpHeight, jumpInfos);
                            if (jumpHeight != 0) {
                                isJump = checkForTimeDependency(patch, deltaT, jumpHeight, jumpInfos);
                                if (isJump == true) {
                                    stopLoop = false;
                                    result = correctJump(patch, pos, isStartCell, result, jumpHeight);
                                }
                            }
                        }
                    }
                } else {
                    stopLoop = false;
                }
            }
            if (addJumpInfos == true) {
                jumpInfos.addInfosToDataItem(item, prevEvent, outputJumpsKey, deltaT);
            }
        }
        item.put(outputKey, result);

        return item;

    }

    /**
     * Checks if there are spikes directly in front of the jump and/or after the jump. Correct this jump if necessary
     *
     * @param patch
     * @param pos
     * @return
     */
    public double[] HandleSpike(int patch, int pos, double[] result, JumpInfos jumpInfo) {

        for (int px = 0; px < 8; px++) {
            int pixel = patch * 9 + px;
            // Check for a double spike in front of the jump
            // properties of a double spike: large jump up, small step, large jump down
            double diff = result[pixel * roi + pos - 1] - result[pixel * roi + pos - 2];
            if (diff > spikeLimit) {
                double diff3 = result[pixel * roi + pos + 1] - result[pixel * roi + pos];
                if (-diff3 > spikeLimit) {
                    // double spike in slices pos-1 and pos found, correct it with the average of the previous two slices:
                    double correctionValue = (result[pixel * roi + pos - 3] + result[pixel * roi + pos - 2]) / 2.0;
                    result[pixel * roi + pos - 1] = correctionValue;
                    result[pixel * roi + pos] = correctionValue;
                    if (addJumpInfos == true) {
                        jumpInfo.addPixelWithSpikes(pixel);
                    }
                }
            }
            // Check for a double spike on the jump
            // properties of a double spike: large jump up, small step, large jump down
            diff = result[pixel * roi + pos] - result[pixel * roi + pos - 1];
            if (diff > spikeLimit) {
                double diff3 = result[pixel * roi + pos + 2] - result[pixel * roi + pos + 1];
                if (-diff3 > spikeLimit) {
                    // double spike in slices pos and pos+1 found, correct pos with the average of the previous two slices:
                    double correctionValue = (result[pixel * roi + pos - 2] + result[pixel * roi + pos - 1]) / 2.0;
                    result[pixel * roi + pos] = correctionValue;
                    correctionValue = (result[pixel * roi + pos + 2] + result[pixel * roi + pos + 3]) / 2.0;
                    // and correct pos+1 with the average of the following two slices:
                    result[pixel * roi + pos + 1] = correctionValue;
                    if (addJumpInfos == true) {
                        jumpInfo.addPixelWithSpikes(pixel);
                    }
                }
            }
            // Check for a double spike after the jump
            // properties of a double spike: large jump up, small step, large jump down
            diff = result[pixel * roi + pos + 1] - result[pixel * roi + pos];
            if (diff > spikeLimit) {
                double diff3 = result[pixel * roi + pos + 3] - result[pixel * roi + pos + 2];
                if (-diff3 > spikeLimit) {
                    // double spike in slices pos+1 and pos+2 found, correct it with the average of the following two slices:
                    double correctionValue = (result[pixel * roi + pos + 3] + result[pixel * roi + pos + 4]) / 2.0;
                    result[pixel * roi + pos + 1] = correctionValue;
                    result[pixel * roi + pos + 2] = correctionValue;
                    if (addJumpInfos == true) {
                        jumpInfo.addPixelWithSpikes(pixel);
                    }
                }
            }
            // Check for a single spike in front of the jump
            // properties of a single spike: large jump up, large jump down
            diff = result[pixel * roi + pos] - result[pixel * roi + pos - 1];
            if (diff > spikeLimit) {
                double diff2 = result[pixel * roi + pos + 1] - result[pixel * roi + pos];
                if (-diff2 > spikeLimit) {
                    // single spike in slices pos found, correct it with the average of the previous two slices:
                    double correctionValue = (result[pixel * roi + pos - 2] + result[pixel * roi + pos - 1]) / 2.0;
                    result[pixel * roi + pos] = correctionValue;
                    if (addJumpInfos == true) {
                        jumpInfo.addPixelWithSpikes(pixel);
                    }
                }
            }
            // Check for a single spike in front of the jump
            // properties of a single spike: large jump up, large jump down
            diff = result[pixel * roi + pos + 1] - result[pixel * roi + pos];
            if (diff > spikeLimit) {
                double diff2 = result[pixel * roi + pos + 2] - result[pixel * roi + pos + 1];
                if (-diff2 > spikeLimit) {
                    // single spike in slices pos+1 found, correct it with the average of the following two slices:
                    double correctionValue = (result[pixel * roi + pos + 2] + result[pixel * roi + pos + 3]) / 2.0;
                    result[pixel * roi + pos + 1] = correctionValue;
                    if (addJumpInfos == true) {
                        jumpInfo.addPixelWithSpikes(pixel);
                    }
                }
            }
        }

        return result;
    }

    //

    /**
     * Calculates the average of the result data array for a given patch. Also calulates the derivation of this patch average.
     * The timemarker channels are excluded from the averaging
     *
     * @param patch
     * @param patchAverage
     * @param patchDerivationAverage
     */
    public void createPatchAverage(int patch, double[] patchAverage, double[] patchDerivationAverage, double[] result) {
        for (int sl = 0; sl < roi; sl++) {
            for (int px = 0; px < 8; px++) {
                int slice = (patch * 9 + px) * roi + sl;
                patchAverage[sl] += result[slice];
            }
            patchAverage[sl] /= 8;
            if (sl > 0) {
                patchDerivationAverage[sl] = patchAverage[sl] - patchAverage[sl - 1];
            }
        }
    }

    /**
     * Checks if there is a jump from the slices pos to pos+1
     *
     * @param patch
     * @param pos
     * @param derivation
     * @param isStartCell
     * @return
     */
    public double checkForJump(int patch, int pos, double[] derivation, boolean isStartCell) {
        double jumpHeight = 0;

        double derivAtJump = derivation[pos + 1];

        if (isStartCell == false) {
            derivAtJump *= -1;
        }

        if (derivAtJump > jumpLimit) {
            jumpHeight = derivation[pos + 1];
        }
        return jumpHeight;
    }

    /**
     * Checks if the jumpHeight is in the range of the predicted jumpHeight, depending on deltaT
     *
     * @param patch
     * @param deltaT
     * @param jumpHeight
     * @param jumpInfos
     * @return
     */
    public boolean checkForTimeDependency(int patch, double deltaT, double jumpHeight, JumpInfos jumpInfos) {
        boolean timeDependIsCorrect = true;

        double predictedJumpHeight = constant * Math.pow(deltaT, tau);

        if (Math.abs(jumpHeight) > timeDependLimit * predictedJumpHeight) {
            timeDependIsCorrect = false;
            if (addJumpInfos == true) {
                jumpInfos.addPatchWithWrongTiming(patch);
            }
            jumpHeight = 0;
        }

        return timeDependIsCorrect;
    }

    /**
     * Checks if the detected jumps comes from a photon signal flank
     *
     * @param patch
     * @param pos
     * @param derivation
     * @param isStartCell
     * @return
     */
    public boolean checkForSignalFlank(int patch, int pos, double[] derivation, boolean isStartCell, double jumpHeight) {
        boolean noSignalFlank = true;

        // Calculate the average derivation over 3 slices before and 3 slices after the jump
        double averDerivAroundJump = 0;

        int counter = 0;
        for (int sl = pos; sl > pos - 2 && sl > leftBorder; sl--) {
            averDerivAroundJump += derivation[sl];
            counter += 1;
        }
        for (int sl = pos + 2; sl < pos + 4 && sl < roi; sl++) {
            averDerivAroundJump += derivation[sl];
            counter += 1;
        }
        averDerivAroundJump /= counter;

        // If we have a stop cell, we only want to check if we have a falling flank. Therefore we multiply with -1, to have positive derivations.
        if (isStartCell == false) {
            averDerivAroundJump *= -1;
        }

        // if either the average derivation before or after the jump is at the same level of the jump height we have a signal flank not a jump
        if (averDerivAroundJump > signalFlankLimit * Math.abs(jumpHeight)) {
            noSignalFlank = false;
            if (addJumpInfos == true) {
                jumpInfos.addPatchWithSignalFlanks(patch);
            }
            jumpHeight = 0;
        }

        return noSignalFlank;
    }

    //

    /**
     * First check if there is a significant ringing in the dataArray, by performing a FFT on the dataArray,
     * and checking the amplitude of the frequency bins between 0.18 GHz and 0.22 GHz. If there is ringing,
     * reduce the jumpHeight by the average of the derivation one period of the ringing before and one period
     * after the jump. Return false if the resulting jumpHeight is smaller than the jumpLimit, and true if it is
     * higher or no ringing was found.
     *
     * @param patch
     * @param pos
     * @param dataArray
     * @param derivation
     * @return
     */
    public double checkForRingingFFT(int patch, int pos, double[] dataArray, double[] derivation, double jumpHeight, JumpInfos jumpInfos) {
        FastFourierTransformer fftObject = new FastFourierTransformer(DftNormalization.STANDARD);

        // we perform a FFT on 32 slices of the data array. This 32 slices have to be between ]leftBorder,roi[
        // if they are not in this region, the slices are shifted.
        int right = pos + lengthAfterPosForFFT;
        if (right > roi) {
            right = roi;
        }
        int left = right - lengthForFFT;
        if (left < leftBorder) {
            left = leftBorder;
            right = left + lengthForFFT;
        }
        double[] arrayForFFT = new double[lengthForFFT];
        for (int sl = 0; sl < lengthForFFT; sl++) {
            arrayForFFT[sl] = derivation[left + sl];
        }

        // Calculating the FFT of the data array
        Complex[] fftResult = fftObject.transform(arrayForFFT, TransformType.INVERSE);

        // Checking the average amplitude in the frequency bins corresponding to a frequency between [0.18 GHz,0.22 GHz]
        // Also the average amplitude in the lower frequency bins ([0 GHz, 0.18 GHz]) is calculated.
        int freqBorderBinLeft = (int) (leftRingingFreq * lengthForFFT / 2);
        int freqBorderBinRight = (int) (rightRingingFreq * lengthForFFT / 2);
        double ringingFreqAmpl = 0.0;
        double comparisonFreqAmpl = 0.0;
        int bin = 0;
        for (; bin < freqBorderBinLeft; bin++) {
            double real = fftResult[bin].getReal();
            double imag = fftResult[bin].getImaginary();
            comparisonFreqAmpl += Math.sqrt(real * real + imag * imag);
        }
        comparisonFreqAmpl /= freqBorderBinLeft;
        for (; bin <= freqBorderBinRight; bin++) {
            double real = fftResult[bin].getReal();
            double imag = fftResult[bin].getImaginary();
            ringingFreqAmpl += Math.sqrt(real * real + imag * imag);
        }
        ringingFreqAmpl /= (freqBorderBinRight - freqBorderBinLeft + 1);

        if (addJumpInfos == true) {
            // this is only for getting the results of the FFT in the viewer
            for (int sl = 0; sl <= lengthForFFT / 2; sl++) {
                double real = fftResult[sl].getReal();
                double imag = fftResult[sl].getImaginary();
                for (int px = 0; px < 9; px++) {
                    jumpInfos.fftResults[(patch * 9 + px) * roi + sl + left] = Math.sqrt(real * real + imag * imag);
                }
            }
        }

        // now check whether the amplitude of the ringing frequencies are larger than a given limit and also the comparison frequency
        // is smaller than another limit, to exclude signals with a large deviation, which could fake high amplitude also in the ringing
        // frequencies
        if (ringingFreqAmpl > freqAmplLimit && comparisonFreqAmpl < freqCompAmplLimit) {
            // Now we calculate the average derivation one ringing period before the jump (if it is not smaller than the leftBorder) and
            // one period after the jump (if it is not larger than the roi)
            double averRingingDerviation = 0.0;
            double counter = 0;
            if ((pos + 1 - ringingPeriode) > leftBorder) {
                averRingingDerviation += derivation[pos + 1 - 11];
                counter += 1;
            }
            if ((pos + 1 + ringingPeriode) < roi) {
                averRingingDerviation += derivation[pos + 1 + 11];
                counter += 1;
            }
            averRingingDerviation /= counter;

            jumpHeight -= averRingingDerviation;
            if (addJumpInfos == true) {
                jumpInfos.addPatchWithRinging(patch);
            }

            if (Math.abs(jumpHeight) < jumpLimit) {
                jumpHeight = 0;
            }
        }
        return jumpHeight;
    }

    /**
     * Correct the data array for a jump. In case of a jump at a previous start cells,
     * all slices beginning with pos+1 are reduced about the jump height, in case of a
     * jump at a previous stopp cell, all slices up to pos are reduced about the jump height.
     *
     * @param patch
     * @param pos
     * @param isStartCell
     * @param result
     * @param jumpHeight
     * @return
     */
    public double[] correctJump(int patch, int pos, boolean isStartCell, double[] result, double jumpHeight) {
        int leftBorder = pos + 1;
        int rightBorder = roi;

        double jumpheight = Math.abs(jumpHeight);

        if (addJumpInfos == true) {
            jumpInfos.addPatchWithCorrectedJumps(patch);
            jumpInfos.averJumpHeights[patch] = jumpHeight;
        }

        if (isStartCell == false) {
            leftBorder = 0;
            rightBorder = pos + 1;
        }

        for (int px = 0; px < 9; px++) {
            int pixel = 9 * patch + px;
            for (int slice = leftBorder; slice < rightBorder; slice++) {
                result[pixel * roi + slice] -= jumpheight;
            }
        }
        return result;
    }
}
