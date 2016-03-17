package fact.filter;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Description;
import stream.annotations.Parameter;

/**
 * 
 * @author Dominik Neise &lt;neised@phys.ethz.ch&gt;
 */
@Description(group = "Fact Tools.Filter", text = "A running mininum")
public class MovingMinimum implements StatefulProcessor{

	static Logger log = LoggerFactory.getLogger(MovingAverage.class);


    @Parameter(required = true)
    private String key;

    @Parameter(required = true)
    private String outputKey;

    @Parameter(required = true)
	private int length = 5;

	private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);
		Utils.isKeyValid(input, "NPIX", Integer.class);
		
        npix = (Integer) input.get("NPIX");
        double[] data = (double[]) input.get(key);
        double[] result = new double[data.length];

        int roi = data.length / npix;

        int length_left_half;
        int length_right_half;
        if (length % 2 == 1){
            length_left_half = length/2;
            length_right_half = length/2 + 1;
        }
        else{
            length_left_half = length/2;
            length_right_half = length/2;            
        }

        for(int pix = 0; pix < npix; pix++) {
            for(int pivot = 0; pivot < roi; pivot++){
                int pivotPosition = pix*roi + pivot;
                int seriesEnd = pix*roi + roi;
                int seriesStart = pix*roi;

                //loop over the window
                //intentional precission loss by division
                int start = pivotPosition - length_left_half;
                int end = pivotPosition + length_right_half;

                start = start > seriesStart ? start : seriesStart;
                end = end < seriesEnd ? end : seriesEnd;

                double min = 1.0e308;
                for(int i = start; i < end; i++){
                    if (data[i] < min){
                        min = data[i];
                    }
                }
                result[pivotPosition] = min;
            }
        }
        input.put(outputKey, result);
        return input;
    }

    @Override
    public void init(ProcessContext context) throws Exception {
        if((length & 1) == 0){
            length++;
            log.warn("CentralMovingAverage only supports uneven window lengths. New length is: " + length);
        }
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
