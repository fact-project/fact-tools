package fact.features.singlePulse.timeLineExtraction;

import java.util.Arrays;

/*
Elementwise operations on an array with a scalar
*/
public class ElementWise {

    public static double[] multiply(double[] arr, double scalar) {
        double[] out = new double[arr.length];
        for(int i=0; i<arr.length; i++)
            out[i] = arr[i]*scalar;
        return out;
    }  

    public static double[] add(double[] arr, double scalar) {
        double[] out = new double[arr.length];
        for(int i=0; i<arr.length; i++)
            out[i] = arr[i]+scalar;
        return out;
    }
}