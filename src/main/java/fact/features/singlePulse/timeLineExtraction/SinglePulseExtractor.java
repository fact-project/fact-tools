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
        /*  
        Reconstructs arrival slices of single photons on a timeline. 
        Only valid for small pulses up to 50 p.e. 
        
        Retruns
        -------
        arrival_slices      A list of the arrival slices of photons found in 
                            the time line.

        Parameter
        ---------
        timeLine            The time line to look for pulses in. The time line 
                            is modified in place. When the extractor was 
                            successfull, the time line is flat and all pulses 
                            were substracted.

        templatePulse       A template time line of the pulse to look for. 
                            For FACT pulses, it turned out best to mostly use 
                            the rising edge of a pulse up to the maximum and 
                            only a small part of the falling edge, 
                            20 slices = 10ns.

        substractionPulse   The template time line of the pulse to be 
                            substracted from the time line. For FACT, this 
                            substraction pulse should be the full pulse with its 
                            long falling edge. 300 slices = 150ns.

        maxIterations       The maximum iterations on atime line before abort.
        */
        arrival_slices = new ArrayList<Integer>();
        int iteration = 0;

        while(true) {
            final double[] conv = Convolve.firstWithSecond(
                timeLine, 
                templatePulse);
            final int offset_slices = 7;
            final ArgMax am = new ArgMax(conv);
            final int max_slice = am.arg - offset_slices;
            final double max_response = am.max;

            if(max_response > 0.5 && iteration < maxIterations) {
                final double weight = 1.0;
                final double[] sub = ElementWise.multiply(
                    substractionPulse, -weight);
                
                AddFirstArrayToSecondArray.at(sub, timeLine, max_slice);

                applyAcCoupling(timeLine);
                arrival_slices.add(am.arg);
            }else{
                break;
            }
            iteration++;
        }
    }

    private void applyAcCoupling(double[] timeLine) {
        /*  
        estimates the effect of FACT's AC coupling in between the 
        photo-electric converter (SIPM) and the signal sampler (DRS4).
        Here simply the mean of the time line is substracted from it.

        Parameter
        ---------
        timeLine    The time line is modified inplace. 
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