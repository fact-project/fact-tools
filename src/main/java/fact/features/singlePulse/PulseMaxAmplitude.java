/**
 *
 */
package fact.photonstream.singlePulse;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * This processor calculates the position of the maximum value for each pulse in each pixel.
 * Input and output are both arrays of size NUMBEROFPIXEL with lists of positions for each pixel.
 *
 *modified by Katie Gray (kathryn.gray@tu-dortmund.de) from MaxAmplitudePosition
 *
 */
public class PulseMaxAmplitude implements Processor {
    static Logger log = LoggerFactory.getLogger(PulseMaxAmplitude.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey;
        //positions of max amplitudes of pulses
    @Parameter(required = true)
    private String pulsePositionKey;
        //positions of threshold crossings

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        double[] data = (double[]) input.get(key);
		int[][] pulsePositions = (int[][]) input.get(pulsePositionKey);
        int roi = data.length / npix;
        int[][] positions =  new int[npix][];

		//for each pixel
		for (int pix = 0; pix < npix; pix++) {
			positions[pix] = new int[pulsePositions[pix].length];
			positions[pix] = findMaximumPositions(pix, roi, data, pulsePositions);
		}
        input.put(outputKey, positions);
//      System.out.println(Arrays.toString(positions));

        return input;
    }

    /**
     * finds the position of the highest value in the pulse. if max is not unique, last position will be taken.
     * @param pix Pixel to check
     * @param roi Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */

    public int[] findMaximumPositions(int pix, int roi, double[] data, int[][] pulsePositions){

        ArrayList<Integer> maxima = new ArrayList<Integer>();

        if(pulsePositions[pix].length > 0){
        	int numberPulses = pulsePositions[pix].length;
        	for(int i = 0; i < numberPulses; i++){
        		  double tempMaxValue = 0;
                  int Position = 0;
                  int start = pulsePositions[pix][i];
                  for(int slice = start; slice < start + 30; slice++){
                       int pos = pix * roi + slice;
                       if(slice > roi) {break;}
                       if(pos == data.length) {break;}
                       double value = data[pos];
                        //update maxvalue and position if current value exceeds old value
                       if(slice != start && slice != start + 30){
                           if(value >= tempMaxValue){
                               tempMaxValue = value;
                               Position = slice;
                           }
                       }
                  }
                  maxima.add(Position);
            }
        }

        return Utils.arrayListToInt(maxima);
    }


    /*
     * Getters and Setters
     */


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getPulsePositionKey() {
        return pulsePositionKey;
    }

    public void setPulsePositionKey(String pulsePositionKey) {
        this.pulsePositionKey = pulsePositionKey;
    }

}
