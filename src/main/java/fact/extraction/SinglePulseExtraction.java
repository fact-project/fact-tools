package fact.extraction;

import fact.Constants;
import fact.Utils;
import org.apache.commons.lang3.ArrayUtils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import java.util.Arrays;
import java.util.ArrayList;
import fact.features.singlePulse.timeLineExtraction.SinglePulseExtractor;
import fact.features.singlePulse.timeLineExtraction.ElementWise;

/*
* Extracts a list of arrival slice positions of photons for each pixel 
* time line.
*/
public class SinglePulseExtraction implements Processor {
    @Parameter(required=true, description="")
    private String dataKey = null;

    @Parameter(
        required = true, 
        description =   "output key, a list of lists of arrival slices of "+ 
                        "photons found in each pixel"
    )
    private String outputKey = null;

    @Parameter(
        required = true, 
        description =   "max number of extraction tries on a single pixel's "+
                        "time line before abort" 
    )
    protected int maxIterations;

    @Parameter(
        required = false, 
        description = "start slice of extraction window", 
        defaultValue = "20"
    )
    protected int startSlice = 20;

    @Parameter(
        required = false, 
        description = "extraction window length in slices", 
        defaultValue = "225"
    )
    protected int windowLength = 225;

    private int npix = Constants.NUMBEROFPIXEL;
    private int roi  = 300;

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        Utils.mapContainsKeys(input, dataKey,  "NPIX");

        npix = (Integer) input.get("NPIX");
        roi = (Integer) input.get("NROI");
        double[] timeLines = (double[]) input.get(dataKey);

        double[] numberOfPulses = new double[npix];
        int[][] pixelArrivalSlices = new int[npix][];

        double[][] timeSeriesAfterExtraction = new double[npix][];

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        config.maxIterations = maxIterations;
        SinglePulseExtractor spe = new SinglePulseExtractor(config);  

        for (int pix = 0; pix < npix; pix++) {
            int start = pix*roi+startSlice;
            int end = start + windowLength;

            double[] pixelTimeLineInMv = Arrays.copyOfRange(
                timeLines,
                start, 
                end
            );
            
            double[] pixelTimeLine = ElementWise.multiply(
                pixelTimeLineInMv, 1.0/config.factSinglePeAmplitudeInMv);

            SinglePulseExtractor.Result result = spe.extractFromTimeline(
                pixelTimeLine);

            numberOfPulses[pix] = result.numberOfPulses();
            pixelArrivalSlices[pix] = result.pulseArrivalSlices;
            timeSeriesAfterExtraction[pix] = ElementWise.multiply(
                result.timeSeriesAfterExtraction, 
                config.factSinglePeAmplitudeInMv);
        }

        addStartSliceOffset(pixelArrivalSlices);

        input.put(outputKey, pixelArrivalSlices);
        input.put(outputKey+"TimeSeriesAfterExtraction", flatten(timeSeriesAfterExtraction));
        input.put(outputKey+"NumberOfPulses", numberOfPulses);
        return input;
    }

    private void addStartSliceOffset(int[][] arr) {
        for(int pix=0; pix<arr.length; pix++) {
            for(int ph=0; ph<arr[pix].length; ph++) {
                int slice_with_offset = arr[pix][ph];
                arr[pix][ph] = slice_with_offset + startSlice;
            }
        }
    }

    private double[] flatten(double[][] matrix2d) {
        if(matrix2d.length > 0) {
            if(matrix2d[0].length > 0) {
                double[] flat = new double[matrix2d.length*matrix2d[0].length];
                int k = 0; 
                for(int i=0; i<matrix2d.length; i++) {
                    for(int j=0; j<matrix2d[0].length; j++) {
                        flat[k] = matrix2d[i][j];
                        k++;
                    }
                }
                return flat;
            }else{
                return new double[0];
            }
        }else{
            return new double[0];
        }
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setStartSlice(int startSlice) {
        this.startSlice = startSlice;
    }

    public void setWindowLength(int windowLength) {
        this.windowLength = windowLength;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

}
