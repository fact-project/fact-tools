package fact.features.singlePulse.timeLineExtraction;

import java.util.Arrays;
import java.util.ArrayList;
import fact.features.singlePulse.timeLineExtraction.ElementWise;
import fact.features.singlePulse.timeLineExtraction.Convolve;

public class SinglePulseExtractor {

    public ArrayList<Integer> arrival_slices;

    public SinglePulseExtractor(
        double[] timeLine, 
        double[] templatePulse,
        double[] substractionPulse,
        int maxIterations
    ) {
        /**  
        * Reconstructs the arrival slices of single photons on a timeline. 
        * Only valid for small pulses up to 50 p.e. 
        * 
        * @return arrival_slices
        *           A list of the arrival slices of photons found in 
        *           the time line.
        * 
        * @param timeLine 
        *           The time line to look for pulses in. The time line 
        *           is modified in place. When the extractor was 
        *           successfull, the time line is flat and all pulses 
        *           were substracted. 
        *           Amplitude of the single puls must be normalized to 1.0.
        * 
        * @param templatePulse       
        *           A template time line of the pulse to look for. 
        *           For FACT pulses, it turned out best to mostly use 
        *           the rising edge of a pulse up to the maximum and 
        *           only a small part of the falling edge, 
        *           20 slices = 10ns.
        *           Amplitude of the single puls must be normalized to 1.0.
        * 
        * @param substractionPulse   
        *           The template time line of the pulse to be 
        *           substracted from the time line. For FACT, this 
        *           substraction pulse should be the full pulse with its 
        *           long falling edge. 300 slices = 150ns.
        *           Amplitude of the single puls must be normalized to 1.0.
        * 
        * @param maxIterations       
        *           The maximum iterations on atime line before abort.
        */
        arrival_slices = new ArrayList<Integer>();
        int iteration = 0;

        while(true) {
            final double[] conv = Convolve.firstWithSecond(
                timeLine, 
                templatePulse);
            final ArgMax am = new ArgMax(conv);
            final int offsetSlices = (int)((double)(templatePulse.length)*0.35);
            // The offsetSlices are needed to comensate both the asymetric 
            // convolution and the asymetric amplitude distribution in the 
            // pulse template (mostly the rising edge of the pulse).
            // These asymetries cause the maximum amplitude in conv not to 
            // be the optimum position for the pulse substraction.
            // The offsetSlices are chosen to correct for this and as a 
            // first guide we provide here the magic factor: 
            // offsetSlices = 0.35*templatePulse.length
            // This indicates the dependency of offsetSlices of the 
            // templatePulse.length 
            // (offsetSlices = 7 for templatePulse.length = 20).
            // It might be that offsetSlices can be optimized based on the
            // maxResponse.
            final int maxSlice = am.arg - offsetSlices;
            final double maxResponse = am.max;

            if(maxResponse > 0.5 && iteration < maxIterations) {
                final double weight = 1.0;
                final double[] sub = ElementWise.multiply(
                    substractionPulse, 
                    -weight);
                
                AddFirstArrayToSecondArray.at(sub, timeLine, maxSlice);

                applyAcCoupling(timeLine);
                arrival_slices.add(am.arg);
            }else{
                break;
            }
            iteration++;
        }
    }

    private void applyAcCoupling(double[] timeLine) {
        /**  
        * Estimates the effect of FACT's AC coupling in between the 
        * photo-electric converter (SIPM) and the signal sampler (DRS4).
        * Here simply the mean of the time line is substracted from it.
        * 
        * @param timeLine    
        *           The time line is modified inplace. 
        */
        if(timeLine.length == 0)
            return;

        double sum = 0.0;
        for(int i=0; i<timeLine.length; i++)
            sum += timeLine[i];

        final double mean = sum/(double)(timeLine.length);

        timeLine = ElementWise.add(timeLine, -mean);
    }
}