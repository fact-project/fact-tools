package fact.TriggerEmulation;

import fact.Constants;
import fact.Utils;
import fact.calibrationservice.CalibrationService;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Service(required = true, description = "The calibration service which provides the information about the bad pixels")
    public CalibrationService calibService;

    @Parameter(required = false, description = "The key containing the event timestamp")
    public String timeStampKey = "timestamp";

    @Parameter(required = false, description = "Converts the patch array into a 1440*ROI array inorder to visualize the patche time series in the viewer")
    public Boolean visualize = false;

    @Parameter(required = false, description = "Ignore the broken pixels in the patch sum")
    public Boolean ignoreBrokenPixels = false;

    @Parameter(required = false, description = "Ignore the pixels containing light from a start in the patch sum")
    public Boolean ignoreStarpixels = false;

    @Parameter(required = false)
    public String[] starPositionKeys = null;
    @Parameter(required = false, defaultValue="Constants.PIXEL_SIZE")
    public double starRadiusInCamera = Constants.PIXEL_SIZE_MM;

    private FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    private PixelSet star_set = null;
    private PixelSet bad_pixel_set = null;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);
        double[] data = (double[]) item.get(key);

        int roi = (int) item.get("NROI");

        int n_patches = Constants.N_PIXELS/9;

        star_set = calculateStarPixelSet(item);
        bad_pixel_set = calculateBadPixelSet(item);

        double[][] pixel_data = Utils.snipPixelData(data, 0, 0, Constants.N_PIXELS, roi);
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
     * generates the pixel set with bad pixels to be excluded from the trigger
     * @param item
     * @return pixelset
     */
    public PixelSet calculateBadPixelSet(Data item) {
        ZonedDateTime timeStamp = null;

        if (item.containsKey(timeStampKey) == true){
            Utils.isKeyValid(item, timeStampKey, int[].class);
            int[] eventTime = (int[]) item.get(timeStampKey);
            timeStamp = Utils.unixTimeUTCToZonedDateTime(eventTime);
        }
        else {
            // MC Files don't have a UnixTimeUTC in the data item. Here the timestamp is hardcoded to 1.1.2000
            // => The 12 bad pixels we have from the beginning on are used.
            timeStamp = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        }

        PixelSet badPixelsSet = calibService.getNotUsablePixels(timeStamp);

        return badPixelsSet;
    }

    /**
     * generates the pixel set with star pixels to be excluded from the trigger
     * @param item
     * @return pixelset
     */
    public PixelSet calculateStarPixelSet(Data item) {
        PixelSet starSet = new PixelSet();

        if (starPositionKeys == null) {
            return starSet;
        }

        for (String starPositionKey : starPositionKeys)
        {
            Utils.isKeyValid(item, starPositionKey, double[].class);
            double[] starPosition = (double[]) item.get(starPositionKey);

            CameraPixel star_pixel =  pixelMap.getPixelBelowCoordinatesInMM(starPosition[0], starPosition[1]);
            int chidOfPixelOfStar = star_pixel.chid;

            List<Integer> starChidList = new ArrayList<>();

            starChidList.add(chidOfPixelOfStar);

            starSet.addById(chidOfPixelOfStar);

            for (CameraPixel px: pixelMap.getNeighborsFromID(chidOfPixelOfStar))
            {
                if (calculateDistance(px.id, starPosition[0], starPosition[1]) < starRadiusInCamera)
                {
                    starSet.add(px);
                    starChidList.add(px.id);
                }
            }
        }
        return starSet;
    }

    /**
     * Calculates the Distance between a pixel and a given position
     * @param chid
     * @param x
     * @param y
     * @return
     */
    private double calculateDistance(int chid, double x, double y)
    {
        double xdist = pixelMap.getPixelFromId(chid).getXPositionInMM() - x;
        double ydist = pixelMap.getPixelFromId(chid).getYPositionInMM() - y;

        return Math.sqrt((xdist*xdist)+(ydist*ydist));
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
        int pixel_counter = 0;
        for (int pix = 0; pix < 9; pix++) {
            int current_pix = patch * 9 + pix;

            if (ignoreBrokenPixels && bad_pixel_set.toArrayList().contains(current_pix)){
                continue;
            }
            if (ignoreStarpixels && star_set.toArrayList().contains(current_pix)){
                continue;
            }

            assert (patch_sum.length == pixel_data[current_pix].length);


            for (int i = 0; i < patch_sum.length; i++) {
                assert (i < patch_sum.length);
                assert (i < pixel_data[current_pix].length);
                patch_sum[i] += pixel_data[current_pix][i];
            }
            pixel_counter++;
        }
        // if not all 9 pixels were used scale the patch sum to the same value as is if 9 pixels were used
        if (pixel_counter < 8){
            for (int i = 0; i < patch_sum.length; i++) {
                patch_sum[i] /= pixel_counter;
                patch_sum[i] *= 9;
            }
        }
        return patch_sum;
    }

}
