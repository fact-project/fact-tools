package fact.features.singlePulse.timeLineExtraction;

import java.util.Arrays;
/*
Finds the maximum on an array and stores its position (arg) and its value (max).
*/
public class ArgMax {
    public int arg;
    public double max;

    public ArgMax(double[] arr) {
        arg = 0;
        max = arr[0];;        
        for(int i=0; i<arr.length; i++) {
            if(arr[i] > max) {
                max = arr[i];
                arg = i;
            }
        }
    }        
}