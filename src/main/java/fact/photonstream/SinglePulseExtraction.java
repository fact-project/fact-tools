package fact.photonstream;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import java.util.Arrays;
import fact.photonstream.singlePulse.timeSeriesExtraction.SinglePulseExtractor;
import fact.photonstream.singlePulse.timeSeriesExtraction.ElementWise;

/**
 * Extracts a list of arrival slice positions of photons for each pixel
 * time line.
 *
 * Only a fraction of the whole time series is analysed, this is
 * called extraction window.
 * Artifacts of the *extraction window* borders are removed from
 * the lists of found single pulses. This is implemented by introducing
 * the *output window*.
 *
 * slices:
 *                        whole time series
 * |.......................................................................|
 * |                                                                       |
 * |         |................ extraction window .................|        |
 * |         | <---------- length = 225 ------------------------> |        |
 * |         |                                                    |        |
 * |         |       |.. output window ..|                        |        |
 * 0        20       | <- length=100 ->  |                       245      300
 *                  30                  130
 *
 * Created by Sebastian Mueller
 * and some modifications from Jens Buss
*/
public class SinglePulseExtraction implements Processor {
    @Parameter(required=true, description="")
    private String dataKey = null;

    @Parameter(
        required = true,
        description =   "output key: The value is a 2dim int array [1440][variable length]" +
                        "of arrival slices of photons found in each pixel"
    )
    private String outputKey = null;

    @Parameter(
        required = false,
        description =   "max number of extraction tries on a single pixel's "+
                        "time line before abort",
        defaultValue = "4000"
    )
    protected int maxIterations = 4000;

    @Parameter(
        required = false,
        description = "start slice of extraction window",
        defaultValue = "20"
    )
    protected int startSliceExtractionWindow = 20;

    @Parameter(
        required = false,
        description = "start slice of output window",
        defaultValue = "30"
    )
    protected int startSliceOutputWindow = 30;

    @Parameter(
        required = false,
        description = "output window length in slices",
        defaultValue = "100"
    )
    protected int outputWindowLengthInSlices = 100;

    @Parameter(
        required = false,
        description = "extraction window length in slices",
        defaultValue = "225"
    )
    protected int extractionWindowLengthInSlices = 225;

    private int npix = Constants.NUMBEROFPIXEL;
    private int roi  = 300;

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        Utils.mapContainsKeys(input, dataKey,  "NPIX");

        npix = (Integer) input.get("NPIX");
        roi = (Integer) input.get("NROI");
        double[] timeSerieses = (double[]) input.get(dataKey);

        double[] numberOfPulses = new double[npix];
        int[][] pixelArrivalSlices = new int[npix][];
        double[] baseLine = new double[npix];
        double[][] timeSeriesAfterExtraction = new double[npix][];

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        config.maxIterations = maxIterations;

        SinglePulseExtractor spe = new SinglePulseExtractor(config);

        for (int pix = 0; pix < npix; pix++) {
            int start = pix*roi+startSliceExtractionWindow;
            int end = start + extractionWindowLengthInSlices;

            double[] pixelTimeSeriesInMv = Arrays.copyOfRange(
                timeSerieses,
                start,
                end
            );

            double[] pixelTimeSeries = ElementWise.multiply(
                pixelTimeSeriesInMv, 1.0/config.factSinglePeAmplitudeInMv);

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

        input.put(outputKey, pixelArrivalSlices);
        input.put(outputKey+"TimeSeriesAfterExtraction", Utils.flatten2dArray(timeSeriesAfterExtraction));
        input.put(outputKey+"NumberOfPulses", numberOfPulses);
        input.put(outputKey+"BaseLine", baseLine);
        input.put(outputKey+"MaxIterations", maxIterations);
        return input;
    }

    private void addStartSliceOffset(int[][] arr) {
        for(int pix=0; pix<arr.length; pix++) {
            for(int ph=0; ph<arr[pix].length; ph++) {
                int slice_with_offset = arr[pix][ph];
                arr[pix][ph] = slice_with_offset + startSliceExtractionWindow;
            }
        }
    }



    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setStartSliceExtractionWindow(int startSliceExtractionWindow) {
        this.startSliceExtractionWindow = startSliceExtractionWindow;
    }

    public void setStartSliceOutputWindow( int startSliceOutputWindow){
        this.startSliceOutputWindow = startSliceOutputWindow;
    }

    public void setOutputWindowLengthInSlices( int outputWindowLengthInSlices){
        this.outputWindowLengthInSlices = outputWindowLengthInSlices;
    }

    public void setExtractionWindowLengthInSlices(int extractionWindowLengthInSlices) {
        this.extractionWindowLengthInSlices = extractionWindowLengthInSlices;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

}