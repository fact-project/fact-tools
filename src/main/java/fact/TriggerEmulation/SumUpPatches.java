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
        int skipFirst = 20;
        int skipLast = 40;

        double[][] pixel_data = Utils.snipPixelData(data, skipFirst, skipLast, Constants.NUMBEROFPIXEL, roi);
        double[][] patch_sums = new double[n_patches][];
        for (int patch = 0; patch < n_patches; patch++) {
            double[] patch_sum = new double[roi];

            for (int pix = 0; pix < 9; pix++) {
                int current_pix = patch * 9 + pix;
                double[] current_patch_sum = patch_sum;

                IntStream.range(skipFirst, current_patch_sum.length-skipLast-skipFirst)
                        .forEach(
                                i -> current_patch_sum[i] = pixel_data[current_pix][i] + current_patch_sum[i]
                        );

                patch_sum = current_patch_sum;
            }
            patch_sums[patch] = patch_sum;
        }
        item.put(outKey, patch_sums);

        if (visualize == true){
            double[] new_data = new double[0];
            for (int patch = 0; patch < n_patches; patch++) {
                for (int pix = 0; pix < 9; pix++) {

                    new_data = ArrayUtils.addAll(new_data, Arrays.copyOf(patch_sums[patch],roi));
                }
            }
            item.put(outKey+"_vis", new_data);
        }
        return item;

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
