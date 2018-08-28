package fact.utils;

/**
 * A collection of element-wise operations on double[] arrays with a scalar.
 */
public class ElementWise {

    /**
     * Multiply values in an array with a scalar.
     *
     * @param arr    The input array [N]
     * @param scalar A scalar to multiply the array with
     * @return out
     * A copy of arr [N], but elementwise multiplied with scalar
     */
    public static double[] multiply(double[] arr, double scalar) {
        double[] out = new double[arr.length];
        for (int i = 0; i < arr.length; i++)
            out[i] = arr[i] * scalar;
        return out;
    }

    /**
     * Add scalar to values in an array.
     *
     * @param arr    The input array [N]
     * @param scalar A scalar to be added to the array
     * @return out
     * A copy of arr [N], but element-wise added with scalar
     */
    public static double[] add(double[] arr, double scalar) {
        double[] out = new double[arr.length];
        for (int i = 0; i < arr.length; i++)
            out[i] = arr[i] + scalar;
        return out;
    }

    /**
     * Add two arrays of same length.
     *
     * @param arr1 First input array [N]
     * @param arr2 Second input array [N]
     * @return out
     * The elementwise sum of arr1 and arr2.
     */
    public static double[] addFirstToSecond(double[] arr1, double[] arr2) {
        assert arr1.length == arr2.length;
        double[] out = new double[arr1.length];
        for (int i = 0; i < arr1.length; i++)
            out[i] = arr1[i] + arr2[i];
        return out;
    }

    /**
     * Subtract two arrays of same length.
     *
     * @param arr1 First input array [N]
     * @param arr2 Second input array [N]
     * @return out
     * The elementwise difference fo arr2 and arr1.
     */
    public static double[] subtractFirstFromSecond(double[] arr1, double[] arr2) {
        assert arr1.length == arr2.length;
        double[] out = new double[arr1.length];
        for (int i = 0; i < arr1.length; i++)
            out[i] = arr2[i] - arr1[i];
        return out;
    }
}
