package fact.TriggerEmulation;

import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.FactPixelMapping;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Arrays;

/**
 * Sum up the signals of each patch and return an array of patchwise timeseries. This is e.g. useful for an emulation of
 * the trigger, which is doing the same.
 * Created by jbuss on 14.11.17.
 */
public class SumUpPatches implements Processor {

    static Logger log = LoggerFactory.getLogger(SumUpPatches.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = false)
    public String outKey;

    @Parameter(description = "Key of the pixel sample that should be excluded")
    public String pixelSetExcludeKey = null;

    @Parameter(required = false, description = "Converts the patch array into a 1440*ROI array inorder to visualize the patche time series in the viewer")
    public Boolean visualize = false;

    private FactPixelMapping pixelMap = FactPixelMapping.getInstance();
    private PixelSet invalidPixels = new PixelSet();

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);
        double[] data = (double[]) item.get(key);

        int roi = (int) item.get("NROI");

        int n_patches = Constants.N_PIXELS/Constants.N_PIXELS_PER_PATCH;

        //Load a given pixelset, otherwise use the the whole camera

        if (pixelSetExcludeKey != null) {
            Utils.isKeyValid(item, pixelSetExcludeKey, PixelSet.class);
            invalidPixels = (PixelSet) item.get(pixelSetExcludeKey);
        }

        double[][] pixel_data = Utils.snipPixelData(data, 0, 0, Constants.N_PIXELS, roi);
        double[][] patch_sums = new double[n_patches][];

        for (int patch = 0; patch < n_patches; patch++) {
            patch_sums[patch] = sumPixelsOfPatch(pixel_data, patch, invalidPixels);
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
            for (int pix = 0; pix < Constants.N_PIXELS_PER_PATCH; pix++) {
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
    public double[] sumPixelsOfPatch(double[][] pixel_data, int patch, PixelSet invalid_pixels) {
        int first_pix_id = Constants.N_PIXELS_PER_PATCH*patch;

        //array of length NROI to contain the summed slices
        double[] patch_sum = new double[pixel_data[first_pix_id].length];
        Arrays.fill(patch_sum, 0.);

        int pixel_counter = 0;

        for (int pix = 0; pix < Constants.N_PIXELS_PER_PATCH; pix++) {
            int current_pix = first_pix_id + pix;

            if (invalid_pixels.containsID(current_pix)) {
                continue;
            }

            assert (patch_sum.length == pixel_data[current_pix].length);


            for (int i = 0; i < patch_sum.length; i++) {
                patch_sum[i] += pixel_data[current_pix][i];
            }
            pixel_counter++;
        }
        // if not all 9 pixels were used, scale the patch sum to the same value as is if 9 pixels were used.
        if (pixel_counter > 0 && pixel_counter < Constants.N_PIXELS_PER_PATCH){
            for (int i = 0; i < patch_sum.length; i++) {
                patch_sum[i] /= pixel_counter;
                patch_sum[i] *= Constants.N_PIXELS_PER_PATCH;
            }
        }
        return patch_sum;
    }

}
