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

    @Parameter(required = true, description = "key of data array")
    String key=null;

    @Parameter(required = true, description = "key of slope array")
    String slopeKey=null;

    @Parameter(required = true, description = "key of intercept array")
    String interceptKey=null;

    @Parameter(required = true, description = "width of the window to do the linear regression", defaultValue = "10")
    int width = 10;

    @Parameter(required = true, description = "scaling factor for the slope", defaultValue = "1")
    double scale = 1;

    @Override
    public Data process(Data input) {

        // TODO Auto-generated method stub
        Utils.mapContainsKeys(input, key);

        double[] data = (double[])input.get(key);
        double[] slope = new double[data.length];
        double[] intercept = new double[data.length];

        for (int i=1 ; i < data.length ; i++)
        {
            SimpleRegression regression = new SimpleRegression();
            for (int j=0 ; j < width ; j++){
                regression.addData( j, data[(i+j) % data.length]);
            }
            regression.regress();
            slope[ (i+(width/2)) % data.length ]     = scale*regression.getSlope();
            intercept[ (i+(width/2)) % data.length ] = regression.getIntercept();
        }

        input.put(slopeKey, slope);
        input.put(interceptKey, intercept);

        return input;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public String getSlopeKey() {
        return slopeKey;
    }

    public void setSlopeKey(String slopeKey) {
        this.slopeKey = slopeKey;
    }

    public String getInterceptKey() {
        return interceptKey;
    }

    public void setInterceptKey(String interceptKey) {
        this.interceptKey = interceptKey;
    }
}
