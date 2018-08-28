package fact.photonstream;

import fact.Constants;
import fact.Utils;
import fact.photonstream.timeSeriesExtraction.SinglePulseExtractor;
import fact.utils.ElementWise;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Arrays;

/**
 * Extracts a list of arrival slice positions of photons for each pixel
 * time line.
 * <p>
 * Only a fraction of the whole time series is analysed, this is
 * called extraction window.
 * Artifacts of the *extraction window* borders are removed from
 * the lists of found single pulses. This is implemented by introducing
 * the *output window*.
 * <p>
 * slices:
 * whole time series
 * |.......................................................................|
 * |                                                                       |
 * |         |................ extraction window .................|        |
 * |         | <---------- length = 225 ------------------------> |        |
 * |         |                                                    |        |
 * |         |       |.. output window ..|                        |        |
 * 0        20       | <- length=100 ->  |                       245      300
 * 30                  130
 * <p>
 * Created by Sebastian Mueller
 * and some modifications from Jens Buss
 */
public class SinglePulseExtraction implements Processor {
    @Parameter(required = true, description = "")
    public String dataKey = null;

    @Parameter(
            required = true,
            description = "output key: The value is a 2dim int array [1440][variable length]" +
                    "of arrival slices of photons found in each pixel"
    )
    public String outputKey = null;

    @Parameter(
            required = false,
            description = "max number of extraction tries on a single pixel's " +
                    "time line before abort",
            defaultValue = "4000"
    )
    public int maxIterations = 4000;

    @Parameter(
            required = false,
            description = "start slice of extraction window",
            defaultValue = "20"
    )
    public int startSliceExtractionWindow = 20;

    @Parameter(
            required = false,
            description = "start slice of output window",
            defaultValue = "30"
    )
    public int startSliceOutputWindow = 30;

    @Parameter(
            required = false,
            description = "output window length in slices",
            defaultValue = "100"
    )
    public int outputWindowLengthInSlices = 100;

    @Parameter(
            required = false,
            description = "extraction window length in slices",
            defaultValue = "225"
    )
    public int extractionWindowLengthInSlices = 225;

    private int npix = Constants.N_PIXELS;
    private int roi = 300;

    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, "NPIX", Integer.class);
        Utils.mapContainsKeys(item, dataKey, "NPIX");

        npix = (Integer) item.get("NPIX");
        roi = (Integer) item.get("NROI");
        double[] timeSerieses = (double[]) item.get(dataKey);

        double[] numberOfPulses = new double[npix];
        int[][] pixelArrivalSlices = new int[npix][];
        double[] baseLine = new double[npix];
        double[][] timeSeriesAfterExtraction = new double[npix][];

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        config.maxIterations = maxIterations;

        SinglePulseExtractor spe = new SinglePulseExtractor(config);

        for (int pix = 0; pix < npix; pix++) {
            int start = pix * roi + startSliceExtractionWindow;
            int end = start + extractionWindowLengthInSlices;

            double[] pixelTimeSeriesInMv = Arrays.copyOfRange(
                    timeSerieses,
                    start,
                    end
            );

            double[] pixelTimeSeries = ElementWise.multiply(
                    pixelTimeSeriesInMv, 1.0 / config.factSinglePeAmplitudeInMv);

            SinglePulseExtractor.Result result = spe.extractFromTimeSeries(
                    pixelTimeSeries);

            numberOfPulses[pix] = result.numberOfPulses();
            pixelArrivalSlices[pix] = result.pulseArrivalSlicesInRange(
                    startSliceOutputWindow - startSliceExtractionWindow,
                    outputWindowLengthInSlices);
            timeSeriesAfterExtraction[pix] = ElementWise.multiply(
                    result.timeSeriesAfterExtraction,
                    config.factSinglePeAmplitudeInMv);
            baseLine[pix] = result.timeSeriesBaseLine();
        }

        addStartSliceOffset(pixelArrivalSlices);

        item.put(outputKey, pixelArrivalSlices);
        item.put(outputKey + "TimeSeriesAfterExtraction", Utils.flatten2dArray(timeSeriesAfterExtraction));
        item.put(outputKey + "NumberOfPulses", numberOfPulses);
        item.put(outputKey + "BaseLine", baseLine);
        item.put(outputKey + "MaxIterations", maxIterations);
        return item;
    }

    private void addStartSliceOffset(int[][] arr) {
        for (int pix = 0; pix < arr.length; pix++) {
            for (int ph = 0; ph < arr[pix].length; ph++) {
                int slice_with_offset = arr[pix][ph];
                arr[pix][ph] = slice_with_offset + startSliceExtractionWindow;
            }
        }
    }
}
