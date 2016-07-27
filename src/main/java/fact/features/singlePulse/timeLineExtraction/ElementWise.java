package fact.features.singlePulse.timeLineExtraction;

import java.util.Arrays;

/*
Elementwise operations on an array with a scalar
*/
public class ElementWise {

    /**
    * @param arr
    *           The input array [N]
    * @param scalar
    *           A scalar to multiply the array with  
    * @return out
    *           A copy of arr [N], but elementwise multiplied with scalar
    */
    public static double[] multiply(double[] arr, double scalar) {
        double[] out = new double[arr.length];
        for(int i=0; i<arr.length; i++)
            out[i] = arr[i]*scalar;
        return out;
    }  

    /**
    * @param arr
    *           The input array [N]
    * @param scalar
    *           A scalar to be added to the array  
    * @return out
    *           A copy of arr [N], but elementwise added with scalar
    */
    public static double[] add(double[] arr, double scalar) {
        double[] out = new double[arr.length];
        for(int i=0; i<arr.length; i++)
            out[i] = arr[i]+scalar;
        return out;
    }
}