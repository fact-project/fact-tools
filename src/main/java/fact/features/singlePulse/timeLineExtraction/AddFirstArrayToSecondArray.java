package fact.features.singlePulse.timeLineExtraction;

import java.util.Arrays;

public class AddFirstArrayToSecondArray {
    
    public static void at(double[] f1, double[] f2, int at) {
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
}