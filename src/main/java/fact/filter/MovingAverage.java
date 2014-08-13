package fact.filter;

import fact.Constants;
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
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
@Description(group = "Fact Tools.Filter", text = "A simple running average")
public class MovingAverage implements StatefulProcessor{

	static Logger log = LoggerFactory.getLogger(MovingAverage.class);


    @Parameter(required = true)
    private String key;

    @Parameter(required = true)
    private String outputKey;

    @Parameter(required = true)
	private int length = 5;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);
        double[] data = (double[]) input.get(key);
        double[] result = new double[data.length];

        int roi = data.length / Constants.NUMBEROFPIXEL;

        for(int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
            for(int pivot = 0; pivot < roi; pivot++){
                int pivotPosition = pix*roi + pivot;
                int seriesEnd = pix*roi + roi;
                int seriesStart = pix*roi;
                int count = 0;
                //loop over the window
                //intentional precission loss by division
                int start = pivotPosition - length/2;
                int end = pivotPosition + length/2 + 1;

                start = start > seriesStart ? start : seriesStart;
                end = end < seriesEnd ? end : seriesEnd;

                double sum = 0;
                for(int i = start; i < end; i++){
//                    int pos = pix*roi + i;
                    if (i !=  pivotPosition) {
                        sum += data[i];
                        count++;
                    }
                }
                result[pivotPosition] = sum/count;
            }
        }
        input.put(outputKey, result);
        return input;
    }

    @Override
    public void init(ProcessContext context) throws Exception {
        if((length & 1) == 0){
            length++;
            log.info("CentralMovingAverage only supports uneven window lengths. New length is: " + length);
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