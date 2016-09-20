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
    protected int startSlice = 25;

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

        double[] single_pe_count = new double[npix];
        ArrayList<ArrayList<Integer>> pixelArrivalSlices = 
            new ArrayList<ArrayList<Integer>>();

        double[] reducedTimeline = new double[timeLines.length];

        for (int pix = 0; pix < npix; pix++) {
            int start = pix*roi+startSlice;
            int end = start + windowLength;

            double[] pixelTimeLineInMv = Arrays.copyOfRange(
                timeLines,
                start, 
                end
            );
            
            double[] pixelTimeLine = SinglePulseExtractor. 
                milliVoltToNormalizedSinglePulse(pixelTimeLineInMv);

            ArrayList<Integer> arrivalSlices = SinglePulseExtractor.
                getArrivalSlicesOnTimeline(
                    pixelTimeLine, 
                    maxIterations
                );

            single_pe_count[pix] = arrivalSlices.size();
            pixelArrivalSlices.add(arrivalSlices);

            for (int i = 0; i < windowLength; i++) {
                reducedTimeline[start+i] = SinglePulseExtractor.factSinglePeAmplitudeInMv*pixelTimeLine[i];
            }
        }



        addStartSliceOffset(pixelArrivalSlices);
        // printArrivalSlices(pixelArrivalSlices);
        input.put(outputKey, pixelArrivalSlices);
        input.put(outputKey+"TL", reducedTimeline);
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
