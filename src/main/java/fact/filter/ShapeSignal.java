package fact.filter;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by jbuss on 07.10.14.
 */
public class ShapeSignal implements Processor {

    static Logger log = LoggerFactory.getLogger(ShapeSignal.class);


    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey;

    @Parameter(required = true)
    public int shift = 10;

    @Parameter(required = false)
    int skipLeft = 30;

    @Parameter(required = false)
    int skipRight = 40;

    @Parameter
    public double factor = 0.66;


    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, key, double[].class);
        double[] data = (double[]) item.get(key);
        double[] result = new double[data.length];

        int n_pixels = Constants.N_PIXELS;
        int roi = data.length/1440;

        for (int pix = 0; pix < n_pixels; pix++) {
            double[] pixel_data = Arrays.copyOfRange(data, pix*roi+skipLeft, roi*(pix+1)-skipRight);
            double[] shifted_data = new double[pixel_data.length];

            int validRoi = roi - skipLeft - skipRight;

            for (int i=0 ; i < pixel_data.length ; i++)
            {
                shifted_data[(i+shift) % validRoi] = (-1) * factor * pixel_data[ i ];
            }

            for (int i=0 ; i < pixel_data.length ; i++)
            {
                int sl = Utils.absPos(pix,i+skipLeft,roi);
                result[sl] = data[sl] + shifted_data[i];
            }
        }

        item.put(outputKey, result);

        return item;
    }
}
