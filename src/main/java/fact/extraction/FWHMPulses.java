package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.filter.MovingLinearFit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Calculate the FWHM of a pulse in a list of pulses.
 * Created by jbuss on 14.10.14.
 */
public class FWHMPulses implements Processor{
    static Logger log = LoggerFactory.getLogger(MovingLinearFit.class);

    @Parameter(required = true, description = "key of data array")
    String key=null;

    @Parameter(required = true, description = "key of max position array")
    String maxPosKey=null;

    @Parameter(required = true, description = "key of min position array")
    String minPosKey=null;

    @Parameter(required = true, description = "key of output array")
    String outputKey =null;

    @Parameter(description = "key of output for visualisation")
    private String visualizationKey = null;

    @Override
    public Data process(Data input) {
        /**
         * Takes the data array,
         * Takes the position of the maximum
         * Takes the global minimum slices of each pixel
         * global minimum as offset
         * Shift amplitudes to be minimum slice at 0
         * calculate FWHM for pulse
         */

        Utils.mapContainsKeys(input, key, maxPosKey, minPosKey);

        double[] data             = (double[])input.get(key);
        double[] minAmplitudes    = (double[])input.get(minPosKey);
        ArrayList[] maxPosArrayList  = (ArrayList[]) input.get(maxPosKey);

        ArrayList[] widthArrayList = new ArrayList[Constants.NUMBEROFPIXEL];
        double[] visualisation 	 =  new double[data.length];

        int roi = data.length / Constants.NUMBEROFPIXEL;

        for (int pix = 0; pix < maxPosArrayList.length; pix++){
            double offset = minAmplitudes[pix];

            ArrayList<Integer> widthList = new ArrayList<Integer>();

            for (int pulse = 0; pulse < maxPosArrayList[pix].size(); pulse++){
                int maxPos     = pix*roi + (Integer) maxPosArrayList[pix].get(pulse);
                double maxAmplitude = data[maxPos] - offset;

                visualisation[maxPos] = data[maxPos];

                int right = 0;
                int left  = 0;
                double rightAmplitude = maxAmplitude;
                double leftAmplitude  = maxAmplitude;

                ///ToDo this method is still super fishy, rework it!

                while(rightAmplitude >= maxAmplitude/2 || leftAmplitude >= maxAmplitude/2){

                    if(rightAmplitude >= maxAmplitude/2){
                        right++;
                        if (maxPos+right >= (pix+1)*roi){
                            log.error(String.format("(right) slices out of bounds in pixel %s at %s", pix, maxPos+right));
                            break;
                        }

                        rightAmplitude = data[maxPos+right] - offset;
                        visualisation[maxPos+right]=data[maxPos]/2;
                    }

                    if(leftAmplitude >= maxAmplitude/2) {
                        left++;
                        if (maxPos-left < pix*roi){
                            log.error(String.format("(left) slices out of bounds in pixel %s at %s", pix, maxPos-left));
                            break;
                        }
                        leftAmplitude  = data[maxPos - left] - offset;
                        visualisation[maxPos-left]=data[maxPos]/2;
                    }
                }
                widthList.add(right+left);
            }
            widthArrayList[pix]=widthList;
        }
        input.put(outputKey, widthArrayList);
        input.put(visualizationKey, visualisation);


        return input;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMaxPosKey() {
        return maxPosKey;
    }

    public void setMaxPosKey(String maxPosKey) {
        this.maxPosKey = maxPosKey;
    }

    public String getMinPosKey() {
        return minPosKey;
    }

    public void setMinPosKey(String minPosKey) {
        this.minPosKey = minPosKey;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getVisualizationKey() {
        return visualizationKey;
    }

    public void setVisualizationKey(String visualizationKey) {
        this.visualizationKey = visualizationKey;
    }
}
