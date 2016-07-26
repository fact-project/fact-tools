package fact.features.singlePulse.timeLineExtraction;

import java.util.Arrays;

public class TemplatePulse {

    public static double[] factSinglePePulse(int lengthInSlices) {
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
}