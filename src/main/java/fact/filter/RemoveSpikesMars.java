/**
 * 
 */
package fact.filter;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Supposedly removes all spikes in the data.
 * Original algorithm by F.Temme. 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class RemoveSpikesMars implements Processor {
	static Logger log = LoggerFactory.getLogger(RemoveSpikesMars.class);

    @Parameter(required = false,
            description = "A Spike can consist of two slices. That means the peak has two data points which are higher " +
                    "than the rest. This parameter describes the maximum difference these two " +
                    "points are allowed to have.", defaultValue="4.0")
    private double topSlope = 4.0;

    @Parameter(required = true)
    private String key;


    @Parameter(required = true)
    private String outputKey;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);

        double[] data = (double[]) input.get(key);
        double[] filteredData = new double[data.length];
        System.arraycopy(data, 0, filteredData, 0, data.length);

        int roi = data.length / Constants.NUMBEROFPIXEL;
        for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
            //iterate over all slices
            for (int slice = 1; slice < roi-3; slice++) {
                int sl = pix * roi + slice;
                // check if it is a one slice jump up
                if (data[sl] - data[sl-1] > 25)
                {
                    // check if immediately a one slice jump down follows
                    // ==> Single Spike
                    if(data[sl+1] - data[sl] < -25)
                    {
                        filteredData[sl]     = ( data[sl-1] + data[sl+1] ) / 2;
                    }
                }
                // check if it is a one slice jump up
                if (data[sl] - data[sl-1] > 22)
                {
                    // check if immediately a small step follows
                    if (Math.abs((data[sl+1] - data[sl])) < topSlope )
                    {
                        // check if then a one slice jump down follows
                        // ==> Double Spike
                        if (data[sl+2] - data[sl+1] < -22)
                        {
                            filteredData[sl] = ( data[sl-1] + data[sl+2] ) / 2;
                            filteredData[sl+1] = data[sl];
                        }
                    }
                }
            }
        }

        input.put(outputKey, filteredData);

        return input;
    }

	public double getTopSlope() {
		return topSlope;
	}
	public void setTopSlope(double topSlope) {
		this.topSlope = topSlope;
	}

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }


}
