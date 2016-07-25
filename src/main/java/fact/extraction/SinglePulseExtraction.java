package fact.extraction;

import fact.Constants;
import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Single Photon Equvalent Extractor
 * 
 * RETURNS
 * -------
 *
 */
public class SinglePulseExtraction implements Processor {
    @Parameter(required=true, description="")
    private String dataKey = null;

    @Parameter(required=true, description="output key, array with number count of p.e for each pixel")
    private String outputKey = null;

    @Parameter(required = false, description="start slice index on the time line", defaultValue="20")
    protected int firstSlice = 20;

    @Parameter(required = false, description="max number of single p.e. to be extracted on a time line before giving up", defaultValue="50")
    protected int maxIterations = 50;

    @Parameter(required = false, description="number of slices on time line", defaultValue="225")
    protected int range = 225;

    private int npix = Constants.NUMBEROFPIXEL;
    private int roi  = 300;

    class ArgMax {
        public int arg;
        public double max;
    }

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        Utils.mapContainsKeys(input, dataKey,  "NPIX");

        npix        = (Integer) input.get("NPIX");
        roi         = (Integer) input.get("NROI");

        double[] data       = (double[]) input.get(dataKey);

        int pulseToLookForLength = 20;
        double[] pulseToLookFor = getTemplatePulse(pulseToLookForLength);

        int pulseToSubstractLength = roi;
        double[] pulseToSubstract = getTemplatePulse(pulseToSubstractLength);

        double factSinglePeAmplitudeInMv = 10.0;

        double[] outputAmplitude = new double[npix];

        for (int pix = 0; pix < npix; pix++) {
            int start = pix*roi;
            int end = start + range;

            double[] pixelTimeLineInMv = Arrays.copyOfRange(data, start, end);
            
            double[] pixelTimeLine = multiplyArrayElementwis(
                pixelTimeLineInMv, 
                1.0/factSinglePeAmplitudeInMv
            );

            ArrayList<Double> arrival_slices = getExtractedPulses(
                pixelTimeLine, 
                pulseToLookFor,
                pulseToSubstract,
                maxIterations
            );
            outputAmplitude[pix] = arrival_slices.size();
            //outputArrivalSlices[pix] = arrival_slices;
        }

        input.put(outputKey, outputAmplitude);
        //input.put(outputKey+"_arrival_slices", outputArrivalSlices);
        return input;
    }

    private double[] getTemplatePulse(int lengthInSlices) {
        //  amplitude
        //       |
        //  1.0 _|_        ___  
        //       |        /   \
        //       |       /     |
        //       |      |       \
        //       |      |        \_
        //       |      /          \_____
        //       |     |                 \________
        //  0.0 _|_____|__________________________\_______________\ time
        //             |                                          /
        //            0.0
        
        final double periodeSliceInNs = 0.5;

        final double[] time = new double[lengthInSlices];
        for (int i = 0; i < time.length; i++) {time[i] = i*periodeSliceInNs;}


        double[] template = new double[lengthInSlices];
        for (int i = 0; i < time.length; i++) {

            final double amplitude = 
                1.626*
                (1.0-Math.exp(-0.3803*time[i]))
                *Math.exp(-0.0649*time[i]);
            
            if(amplitude < 0.0) {
                template[i] = 0.0;
            }else{
                template[i] = amplitude;
            }
        }

        return template;
    }

    private ArrayList<Double> getExtractedPulses(
        double[] timeLine, 
        double[] templatePulse,
        double[] substractionPulse,
        int maxIterations
    ) {
        ArrayList<Double> arrival_slice = new ArrayList<Double>();
        int iteration = 0;

        while(true) {
            double[] sig_conv_sipm = getValidConvolveOfTimeLineAndPulseTemplate(
                timeLine, 
                templatePulse
            );
            final int offset_slices = 7;
            ArgMax am = getArgMax(sig_conv_sipm);
            int max_slice = am.arg - offset_slices;
            double max_response = am.max;

            if(max_response > 0.5 && iteration < maxIterations) {
                double weight = 1.0;
                double[] sub = multiplyArrayElementwis(substractionPulse, -weight);
                addFirstToSecondAt(sub, timeLine, max_slice);
                arrival_slice.add((double)(max_slice));
            }else{
                break;
            }
            iteration++;
        } 

        return arrival_slice;   
    }

    private double[] getValidConvolveOfTimeLineAndPulseTemplate(
        double[] timeLine, 
        double[] pulse
    ) {
        final int maxLength = Math.max(timeLine.length, pulse.length);
        final int minLength = Math.min(timeLine.length, pulse.length);
        double[] output = new double[maxLength - minLength + 1];

        for (int i = 0; i < output.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < pulse.length; j++) {
                sum += timeLine[i+j]*pulse[j];
            }
            output[i] = sum;
        }
        return output;
    }

    private ArgMax getArgMax(double[] arr) {
        ArgMax am = new ArgMax();
        am.arg = 0;
        am.max = arr[0];;        
        for(int i=0; i<arr.length; i++) {

            if(arr[i] > am.max) {
                am.max = arr[i];
                am.arg = i;
            }
        }
        return am;
    }

    private void addFirstToSecondAt(
        double[] f1, 
        double[] f2, 
        int at
    ) {
        if(at > f2.length) return;
        if(at < 0) at = 0;
        // endpoint of injection in f2: e2
        int e2 = at + f1.length;
        // naive endpoint of sampling in f1
        int e1 = f1.length;
        // e2 is limited to range of f2
        if (e2 > f2.length) e2 = f2.length;
        // correct sampling range in f1 if needed
        if (e2-at < f1.length) e1 = e2-at;
        for(int i=at; i<e2; i++)
            f2[i] += f1[i-at];
    }

    private double[] multiplyArrayElementwis(double[] arr, double scalar) {
        double[] out = new double[arr.length];
        for(int i=0; i<arr.length; i++)
            out[i] = arr[i]*scalar;
        return out;
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

    public int getFirstSlice() {
        return firstSlice;
    }

    public void setFirstSlice(int firstSlice) {
        this.firstSlice = firstSlice;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public int getMaxIterations() {
        return maxIterations;
    }
}
