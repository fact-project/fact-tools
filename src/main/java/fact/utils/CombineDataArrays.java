/**
 *
 */
package fact.utils;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Combine two Pixel Arrays of the same size with each other
 * Allows for either addition, substraction, multiplication or division.
 *
 * @author Michael Bulinski &lt;michael.bulinski@udo.edu&gt;
 */
public class CombineDataArrays implements Processor {
    static Logger log = LoggerFactory.getLogger(RemappingKeys.class);

    @Parameter(required = true, description = "The key to your first array.")
    public String firstArrayKey;
    @Parameter(required = true, description = "The key to your second array.")
    public String secondArrayKey;
    @Parameter(required = false, description = "The key for the resulting array.")
    public String outputKey;
    @Parameter(required = true, description = "The operation to perform, (add, sub, mul, div)")
    public String op;


    /* (non-Javadoc)
     * @see stream.Processor#process(stream.Data)
     */
    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, firstArrayKey, double[].class);
        Utils.isKeyValid(input, secondArrayKey, double[].class);

        double[] array1 = (double[]) input.get(firstArrayKey);
        double[] array2 = (double[]) input.get(secondArrayKey);

        if (array1.length != array2.length) {
            throw new RuntimeException("Given arrays are different lengths");
        }
        double[] resultArray = new double[array1.length];


        if (op.equals("add")) {
            for (int pix = 0; pix < array1.length; pix++) {
                resultArray[pix] = (double) (array1[pix] + array2[pix]);
            }
        } else if (op.equals("sub")) {
            for (int pix = 0; pix < array1.length; pix++) {
                resultArray[pix] = (double) (array1[pix] - array2[pix]);
            }
        } else if (op.equals("mul")) {
            for (int pix = 0; pix < array1.length; pix++) {
                resultArray[pix] = (double) (array1[pix] * array2[pix]);
            }
        } else if (op.equals("div")) {
            for (int pix = 0; pix < array1.length; pix++) {
                resultArray[pix] = (double) (array1[pix] / array2[pix]);
            }
        } else {
            throw new RuntimeException("The given operation op: " + op + " is not supported");
        }

        input.put(outputKey, resultArray);

        return input;
    }
}
