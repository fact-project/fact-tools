/**
 * 
 */
package fact.utils;

import fact.Constants;
import fact.Utils;
import fact.extraction.MaxAmplitude;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor counts the number of Pixels in each event that have a value > maxValue.  
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class ThresholdPixelCounter implements Processor{
	static Logger log = LoggerFactory.getLogger(MaxAmplitude.class);

    @Parameter(required = true, description = "The maximum value. If a pixel has data > maxvalue it will be counted",
            defaultValue = "2048")
    private double maxValue = 2048;

    @Parameter(required = true)
    private String key;

    @Parameter(required = true)
    private String outputKey;


    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);

        double[] data = (double[]) input.get(key);

        int roi = data.length / Constants.NUMBEROFPIXEL;

        int pC = 0;
        for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
            // result[pix*roi] =
            // iterate over all slices
            for (int slice = 0; slice < roi; slice++) {
                int pos = pix * roi + slice;
                if(data[pos] > maxValue){
                    pC++;
                    break;
                }
            }
        }
        input.put(outputKey, pC);
        return input;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setKey(String key) {
        this.key = key;
    }

}