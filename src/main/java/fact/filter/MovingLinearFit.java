package fact.filter;

import fact.Utils;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Fit a line to a given window of values in an array
 *
 * Created by jbuss on 08.10.14.
 */
public class MovingLinearFit implements Processor{
    static Logger log = LoggerFactory.getLogger(MovingLinearFit.class);

    String key=null;
    String outputKey=null;

    @Parameter(required = true)
    int width = 10;

    @Override
    public Data process(Data input) {

        // TODO Auto-generated method stub
        Utils.mapContainsKeys(input, key);

        double[] data = (double[])input.get(key);
        double[] result = new double[data.length];

        for (int i=1 ; i < data.length ; i++)
        {
            SimpleRegression regression = new SimpleRegression();
            for (int j=0 ; j < width ; j++){
                regression.addData( j, data[(i+j) % data.length]);
            }
            regression.regress();
            result[ (i+(width/2)) % data.length ] = 10*regression.getSlope();
        }

        input.put(outputKey, result);

        return input;
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
