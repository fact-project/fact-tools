package fact.extraction;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import java.util.Arrays;
import java.util.ArrayList;
import fact.features.singlePulse.timeLineExtraction.ElementWise;
import fact.features.singlePulse.timeLineExtraction.TemplatePulse;
import fact.features.singlePulse.timeLineExtraction.SinglePulseExtractor;

/*
Exttracts a list of arrival slices of photons for each pixel.
*/
public class SinglePulseExtraction implements Processor {
    @Parameter(required=true, description="")
    private String dataKey = null;

    @Parameter(required=true, description="output key, a list of lists of arrival slices of photons found in each pixel")
    private String outputKey = null;

    @Parameter(required = false, description="start slice of extraction window", defaultValue="20")
    protected int startSlice = 25;

    @Parameter(required = false, description="extraction window length in slices", defaultValue="225")
    protected int windowLength = 225;

    @Parameter(required = false, description="max number of extraction tries on a single pixel's time line before abort", defaultValue="50")
    protected int maxIterations = 50;

    private int npix = Constants.NUMBEROFPIXEL;
    private int roi  = 300;

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        Utils.mapContainsKeys(input, dataKey,  "NPIX");

        npix = (Integer) input.get("NPIX");
        roi = (Integer) input.get("NROI");

        double[] timeLines = (double[]) input.get(dataKey);

        int pulseToLookForLength = 20;
        double[] pulseToLookFor = TemplatePulse.factSinglePePulse(
            pulseToLookForLength);

        int pulseToSubstractLength = roi;
        double[] pulseToSubstract = TemplatePulse.factSinglePePulse(
            pulseToSubstractLength);

        final double factSinglePeAmplitudeInMv = 10.0;

        double[] single_pe_count = new double[npix];

        ArrayList<ArrayList<Integer>> pixel_arrival_slices = 
            new ArrayList<ArrayList<Integer>>();

        for (int pix = 0; pix < npix; pix++) {
            int start = pix*roi+startSlice;
            int end = start + windowLength;

            double[] pixelTimeLineInMv = Arrays.copyOfRange(timeLines, start, end);
            
            double[] pixelTimeLine = ElementWise.multiply(
                pixelTimeLineInMv,
                1.0/factSinglePeAmplitudeInMv
            );

            SinglePulseExtractor extractor = new SinglePulseExtractor(
                pixelTimeLine, 
                pulseToLookFor,
                pulseToSubstract,
                maxIterations
            );

            single_pe_count[pix] = extractor.arrival_slices.size();
            pixel_arrival_slices.add(extractor.arrival_slices);
        }

        addStartSliceOffset(pixel_arrival_slices);

        // printArrivalSlices(pixel_arrival_slices);

        input.put(outputKey, pixel_arrival_slices);
        input.put(outputKey+"Count", single_pe_count);
        return input;
    }

    private void addStartSliceOffset(ArrayList<ArrayList<Integer>> arr) {
        for(int pix=0; pix<arr.size(); pix++) {
            for(int ph=0; ph<arr.get(pix).size(); ph++) {
                int slice_with_offset = arr.get(pix).get(ph);
                arr.get(pix).set(ph, slice_with_offset + startSlice);
            }
        }
    }

    private void printArrivalSlices(ArrayList<ArrayList<Integer>> arr) {
        for(int pix=0; pix<arr.size(); pix++) {
            System.out.print("pix "+pix+": ");
            for(int ph=0; ph<arr.get(pix).size(); ph++)
                System.out.print(arr.get(pix).get(ph)+" ");
            System.out.print("\n");
        }
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public int getStartSlice() {
        return startSlice;
    }

    public void setStartSlice(int startSlice) {
        this.startSlice = startSlice;
    }

    public int getWindowLength() {
        return windowLength;
    }

    public void setWindowLength(int windowLength) {
        this.windowLength = windowLength;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public int getMaxIterations() {
        return maxIterations;
    }
}
