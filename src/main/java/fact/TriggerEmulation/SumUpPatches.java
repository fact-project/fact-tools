package fact.TriggerEmulation;

import fact.Constants;
import fact.Utils;
import fact.filter.ShapeSignal;
import fact.photonstream.timeSeriesExtraction.ElementWise;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Sum up the signals of each patch and return an array of patchwise timeseries. This is e.g. useful for an emulation of
 * the trigger, which is doing the same.
 * Created by jbuss on 14.11.17.
 */
public class SumUpPatches implements Processor {

    static Logger log = LoggerFactory.getLogger(ShapeSignal.class);

    @Parameter(required = true)
    private String key;

    @Parameter(required = false)
    private String outKey;

    @Parameter(required = false, description = "Converts the patch array into a 1440*ROI array inorder to visualize the patche time series in the viewer")
    private Boolean visualize = false;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);
        double[] data = (double[]) item.get(key);

        int roi = (int) item.get("NROI");

        int n_patches = Constants.NUMBEROFPIXEL/9;


        double[][] pixel_data = Utils.snipPixelData(data, 0, 0, Constants.NUMBEROFPIXEL, roi);
        double[][] patch_sums = new double[n_patches][];

        for (int patch = 0; patch < n_patches; patch++) {
            patch_sums[patch] = sumPixelsOfPatch(pixel_data, patch);
        }
        item.put(outKey, patch_sums);

        if (visualize){
            item.put(outKey+"_vis", toDataArray(patch_sums));
        }
        return item;

    }

    /**
     * Convert to a full ROI double array with length npixels*ROI
     * @param patch_sums
     * @return
     */
    public double[] toDataArray(double[][] patch_sums) {
        double[] new_data = new double[0];


        for (int patch = 0; patch < patch_sums.length; patch++) {
            for (int pix = 0; pix < 9; pix++) {
                new_data = ArrayUtils.addAll(new_data, patch_sums[patch]);
            }
        }
        return new_data;
    }

    /**
     * sum up all pixels of a given patch
     * @param pixel_data
     * @param patch
     * @return sum of timeserieses of pixels of given patch
     */
    public double[] sumPixelsOfPatch(double[][] pixel_data, int patch) {
        int nPixPerPatch = 9;
        double[] patch_sum = new double[pixel_data[nPixPerPatch*patch].length];
        Arrays.fill(patch_sum, 0.);
        for (int pix = 0; pix < 9; pix++) {
            int current_pix = patch * 9 + pix;

            assert (patch_sum.length == pixel_data[current_pix].length);


            for (int i = 0; i < patch_sum.length; i++) {
                assert (i < patch_sum.length);
                assert (i < pixel_data[current_pix].length);
                patch_sum[i] += pixel_data[current_pix][i];
            }
        }
        return patch_sum;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutKey(String outKey) {
        this.outKey = outKey;
    }

    public void setVisualize(Boolean visualize) {
        this.visualize = visualize;
    }
}
