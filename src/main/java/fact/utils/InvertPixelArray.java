package fact.utils;

import fact.Constants;
import fact.Utils;
import fact.statistics.ArrayStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Arrays;

/**
 * Created by jbuss on 27.10.15.
 */
public class InvertPixelArray implements Processor {
    static Logger log = LoggerFactory.getLogger(ArrayStatistics.class);
    @Parameter(required = true, description = "Input Pixel Array")
    private String key = null;
    @Parameter(required = true, description = "Output Pixel Array")
    private String outputKey = null;

    private int [] pixelArray  = null;

    private int npix = Constants.NUMBEROFPIXEL;

    public Data process(Data input) {
        Utils.mapContainsKeys(input, key, "NROI");

        pixelArray  = (int[]) input.get(key);
        npix        = (Integer) input.get("NPIX");

        boolean [] boolArray = new boolean[npix];

        Arrays.fill(boolArray, Boolean.FALSE);
        for (int pix : pixelArray) {
            boolArray[pix] = true;
        }

        






        return input;
    }
}
