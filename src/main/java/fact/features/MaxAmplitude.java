/**
 * 
 */
package fact.features;

import fact.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor simply calculates the maximum value for all time slices in each Pixel. 
 * The output is a float array with an entry for each Pixel.
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class MaxAmplitude implements Processor{
	static Logger log = LoggerFactory.getLogger(MaxAmplitude.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey;

	private float minValue= -3000;
	private float maxValue = 3000;	
	
    @Override
    public Data process(Data input) {
        double[] data = (double[]) input.get(key);
        int roi = data.length / Constants.NUMBEROFPIXEL;

        //for all pixel find the maximum value
        double[] max = new double[Constants.NUMBEROFPIXEL];
        for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
            //initiate maxValue and postion
            double tempMaxValue = 0;
            //iterate over all slices
            for (int slice = 0; slice < roi; slice++) {
                int pos = pix * roi + slice;
                double value = data[pos];
                if (value > tempMaxValue && value <= maxValue && value >= minValue) {
                    tempMaxValue = value;
                }
            }
            max[pix] = tempMaxValue;
        }
        input.put(outputKey, max);
        return input;
    }

    // Getter and Setter//
    public float getMinValue() {
        return minValue;
    }
    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }
    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }


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
}
