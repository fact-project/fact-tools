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
 * <p>
 * Created by jbuss on 08.10.14.
 */
public class MovingLinearFit implements Processor {
    static Logger log = LoggerFactory.getLogger(MovingLinearFit.class);

    @Parameter(required = true, description = "key of data array")
    public String key = null;

    @Parameter(required = true, description = "key of slope array")
    public String slopeKey = null;

    @Parameter(required = true, description = "key of intercept array")
    public String interceptKey = null;

    @Parameter(description = "width of the window to do the linear regression", defaultValue = "10")
    public int width = 10;

    @Parameter(description = "scaling factor for the slope", defaultValue = "1")
    public double scale = 1;

    @Override
    public Data process(Data item) {

        // TODO Auto-generated method stub
        Utils.mapContainsKeys(item, key);

        double[] data = (double[]) item.get(key);
        double[] slope = new double[data.length];
        double[] intercept = new double[data.length];

        for (int i = 1; i < data.length; i++) {
            SimpleRegression regression = new SimpleRegression();
            for (int j = 0; j < width; j++) {
                regression.addData(j, data[(i + j) % data.length]);
            }
            regression.regress();
            slope[(i + (width / 2)) % data.length] = scale * regression.getSlope();
            intercept[(i + (width / 2)) % data.length] = regression.getIntercept();
        }

        item.put(slopeKey, slope);
        item.put(interceptKey, intercept);

        return item;
    }
}
