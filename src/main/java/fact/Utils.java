package fact;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class Utils {
    private final static Logger log = LoggerFactory.getLogger(Utils.class);

    public static ZonedDateTime unixTimeUTCToZonedDateTime(int[] unixTimeUTC) {
        return Instant.ofEpochSecond(unixTimeUTC[0], unixTimeUTC[1] * 1000).atZone(ZoneOffset.UTC);
    }


    /**
     * Return a 2D array
     */
    public static double[][] sortPixels(double[] data, int numPixel) {

        int roi = data.length / numPixel;
        double[][] pixels = new double[numPixel][roi];

        for (int pix = 0; pix < numPixel; pix++) {
            for (int slice = 0; slice < roi; slice++) {
                int pixId = pix * roi + slice;
                double value = data[pixId];
                pixels[pix][slice] = value;
            }
        }
        return pixels;
    }

    /**
     * Return an int array with the ids of the pixelSet belonging to the given Key,
     * return all camera pixel Ids if the set is not existing
     */
    public static int[] getValidPixelSetAsIntArr(Data item, int npix, String pixelSetKey) {
        int[] pixels;

        //Load a given pixelset, otherwise use the the whole camera

        if (pixelSetKey == null) {
            ContiguousSet<Integer> numbers = ContiguousSet.create(Range.closed(0, npix - 1), DiscreteDomain.integers());
            pixels = Ints.toArray(numbers);
        } else {
            Utils.isKeyValid(item, pixelSetKey, PixelSet.class);
            PixelSet pixelSet = (PixelSet) item.get(pixelSetKey);
            pixels = pixelSet.toIntArray();
        }

        return pixels;
    }

    /**
     * This takes a data array (of length pixels * roi) and returns an
     * array(length = roi) in which each entry is the average over all the
     * values of the other pixels in that slice.
     *
     * @param data array of length pixels*region of interest
     * @return an array of length roi containing the slice averages
     */
    public static double[] averageSlicesForEachPixel(double[] data) {
        int roi = data.length / Constants.N_PIXELS;
        double[] average = new double[roi];
        double[][] values = Utils.sortPixels(data, Constants.N_PIXELS);
        int slice = 0;
        for (double[] slices : values) {
            for (double s : slices) {
                average[slice] += s;
                slice++;
            }
            slice = 0;
        }
        for (int i = 0; i < average.length; i++) {
            average[i] /= (double) Constants.N_PIXELS;
        }
        return average;
    }

    /**
     * Finds all unconnected sets of pixel in the showerPixel List and returns a
     * list of lists. Each list containing one separate set. Does a BFs search.
     * See the wikipedia article on BFS. This version is not as memory efficient
     * as it could be.
     *
     * @param showerPixel the list to search in
     * @return A list of lists.
     */
    public static ArrayList<PixelSet> breadthFirstSearch(PixelSet showerPixel) {
        FactPixelMapping pixelMap = FactPixelMapping.getInstance();

        PixelSet marked = new PixelSet();
        ArrayList<PixelSet> clusters = new ArrayList<>();

        for (CameraPixel pixel : showerPixel) {
            if (!marked.contains(pixel)) {
                // start BFS
                marked.add(pixel);
                ArrayList<CameraPixel> q = new ArrayList<>();
                q.add(pixel);

                // cannot use the enhanced for loop here.
                for (int index = 0; index < q.size(); index++) {
                    // add neighbours to q
                    CameraPixel[] neighbors = pixelMap.getNeighborsForPixel(q.get(index));
                    for (CameraPixel neighbor : neighbors) {

                        if (showerPixel.contains(neighbor) && !marked.contains(neighbor)) {
                            q.add(neighbor);
                            marked.add(neighbor);
                        }
                    }
                }
                clusters.add(PixelSet.fromIDs(q.stream().mapToInt(p -> p.id).toArray()));
            }
        }
        return clusters;
    }

    /**
     * make an Array of the type int[] into ArrayList<Integer>. We need this
     * because there is no autoboxing between Integer and int types hence
     * Arrays.aslist cannot be used for this conversion
     */
    public static ArrayList<Integer> arrayToList(int[] a) {
        ArrayList<Integer> ret = new ArrayList<>();
        for (int i = 0; i < a.length; i++) {
            ret.add(a[i]);
        }
        return ret;
    }

    public static double[] floatToDoubleArray(float[] ar) {
        if (ar != null) {
            double[] ret = new double[ar.length];
            for (int i = 0; i < ar.length; i++) {
                ret[i] = (double) ar[i];
            }
            return ret;
        } else {
            return null;
        }
    }

    public static double[] intToDoubleArray(int[] ar) {
        // return toDoubleArray(ar)
        if (ar != null) {
            double[] ret = new double[ar.length];
            for (int i = 0; i < ar.length; i++) {
                ret[i] = (double) ar[i];
            }
            return ret;
        } else {
            return null;
        }
    }

    public static double[] shortToDoubleArray(short[] ar) {
        // return toDoubleArray(ar)
        if (ar != null) {
            double[] ret = new double[ar.length];
            for (int i = 0; i < ar.length; i++) {
                ret[i] = (double) ar[i];
            }
            return ret;
        } else {
            return null;
        }
    }

    public static double[] byteToDoubleArray(byte[] ar) {
        // return toDoubleArray(ar)
        if (ar != null) {
            double[] ret = new double[ar.length];
            for (int i = 0; i < ar.length; i++) {
                ret[i] = (double) ar[i];
            }
            return ret;
        } else {
            return null;
        }
    }

    /**
     * This is method might be useful for getting stuff from the data items and, if possible, cast it into a double array.
     * This is not very fast. And its also very ugly. But thats okay. I still like you
     *
     * @param arr
     * @return can be null!
     */
    public static double[] toDoubleArray(Serializable arr) {
        if (arr == null) {
            return null;
        }
        if (arr.getClass().isArray()) {
            Class<?> clazz = arr.getClass().getComponentType();
            if (clazz.equals(float.class)) {
                return floatToDoubleArray((float[]) arr);
            } else if (clazz.equals(double.class)) {
                return (double[]) arr;
            } else if (clazz.equals(int.class)) {
                return intToDoubleArray((int[]) arr);
            } else if (clazz.equals(short.class)) {
                return shortToDoubleArray((short[]) arr);
            } else if (clazz.equals(byte.class)) {
                return byteToDoubleArray((byte[]) arr);
            }
        }
        return null;
    }

    public static double valueToDouble(Serializable val) {
        Class<?> clazz = val.getClass();
        if (clazz.equals(float.class) || clazz.equals(Float.class)) {
            return (Float) val;
        } else if (clazz.equals(double.class) || clazz.equals(Double.class)) {
            return (Double) val;
        } else if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
            return (Integer) val;
        }

        return 0;
    }

    public static double[] arrayListToDouble(ArrayList<Double> list) {
        Double[] tempArray = new Double[list.size()];
        list.toArray(tempArray);
        return ArrayUtils.toPrimitive(tempArray);
    }

    public static int[] arrayListToInt(ArrayList<Integer> list) {
        Integer[] tempArray = new Integer[list.size()];
        list.toArray(tempArray);
        return ArrayUtils.toPrimitive(tempArray);
    }

    /**
     * This is a helper method which checks if all the keys provided are in the
     * data item. If one of the keys is not in the item a RuntimeException will
     * be thrown containing a message detailing which processor is causing the
     * error
     *
     * @param item
     * @param keys
     */
    public static void mapContainsKeys(Data item, String... keys) {
		mapContainsKeys(item, Arrays.asList(keys));
	}

	/**
	 * This is a helper method which checks if all the keys provided are in the
	 * data item. If one of the keys is not in the item a RuntimeException will
	 * be thrown containing a message detailing which processor is causing the
	 * error
	 *
	 * @param item
	 * @param keys
	 */
	public static void mapContainsKeys(Data item, Collection<String> keys) {
        ArrayList<String> e = new ArrayList<>();
        boolean isValid = true;
        if (keys == null) {
            isValid = false;
        } else {
            for (String key : keys) {
                if (key == null || !item.containsKey(key)) {
                    isValid = false;
                    e.add(key);
                }
            }
        }
        if (!isValid) {
            StringBuilder b = new StringBuilder();
            for (String er : e) {
                b.append(er);
                b.append("\n");
            }
            StackTraceElement traceElement = Thread.currentThread().getStackTrace()[2];
            String callerClass = traceElement.getClassName();
            String callerMethod = traceElement.getMethodName();
            // if wrapped by the mapContainsKeys(Data item, String...)
            if (callerClass.equals("fact.Utils") && callerMethod.equals("mapContainsKeys")) {
                traceElement = Thread.currentThread().getStackTrace()[3];
                callerClass = traceElement.getClassName();
            }
            throw new RuntimeException("Missing keys for processor " + callerClass + ":  " + b.toString());
        }
    }

    /**
     * This method tries to find the key in the data item and tries to cast them
     * into the type given by the type parameter. If it fails it will throw a
     * RuntimeException with a message containing information about the error.
     *
     * @param item
     * @param key
     * @param type
     */
    public static void isKeyValid(Data item, String key, Class<?> type) {

        if (key == null || key.equals("")) {
            log.error("Key was empty");
        }
        if (!item.containsKey(key)) {
            log.error("Data does not contain the key " + key);
            StackTraceElement traceElement = Thread.currentThread()
                    .getStackTrace()[2];
            String caller = traceElement.getClassName();
            throw new RuntimeException("Did not find key '" + key
                    + "' in the event. For processor:  " + caller);
        }
        try {
            type.cast(item.get(key));
        } catch (ClassCastException e) {
            StackTraceElement traceElement = Thread.currentThread()
                    .getStackTrace()[2];
            String caller = traceElement.getClassName();
            log.error("The value for the key " + key + " cannot be cast to "
                    + type.getSimpleName() + " for processor: " + caller);
            throw e;
        }
    }

    /**
     * Transforms camera coordinates (x, y) into longitudinal and transversal
     * ellipse coordinates (l, t). The ellipse coordinate system is defined by
     * the center of gravity (x,y) and the angle between the major axis and the
     * camera x-axis (delta in radians).
     *
     * @param x
     * @param y
     * @param cogX
     * @param cogY
     * @param delta
     * @return an array having two elements {l, t}
     */
    public static double[] transformToEllipseCoordinates(double x, double y, double cogX, double cogY, double delta) {
        double translatedX = x - cogX;
        double translatedY = y - cogY;

        double sinDelta = Math.sin(delta);
        double cosDelta = Math.cos(delta);

        double l = cosDelta * translatedX + sinDelta * translatedY;
        double t = -sinDelta * translatedX + cosDelta * translatedY;

        return new double[]{l, t};
    }

    public static double calculateDistancePointToShowerAxis(double cogx, double cogy, double delta, double x, double y) {
        double distance = 0;

        double r0 = (x - cogx) * Math.cos(delta) + (y - cogy) * Math.sin(delta);

        distance = Math.sqrt(Math.pow(cogx - x + r0 * Math.cos(delta), 2) + Math.pow(cogy - y + r0 * Math.sin(delta), 2));

        return distance;
    }

    /**
     * Sum up array
     *
     * @param a
     * @return
     */
    public static Double arraySum(double[] a) {
        if (a == null)
            return 0.0;
        if (a.length == 0)
            return 0.0;

        double ret = 0.0;

        for (double element : a) {
            ret += element;
        }

        return ret;
    }

    /**
     * Elementwise multiplication of arrays
     *
     * @param a
     * @param b
     * @return double array containing a[i] * b[i]
     * @throws ArrayStoreException
     */
    public static double[] arrayMultiplication(double[] a, double[] b)
            throws ArrayStoreException {
        if (a == null || b == null) {
            return null;
        }
        if (a.length != b.length) {
            throw new ArrayStoreException("Array sizes do not match.");
        }
        if (a.length == 0) {
            throw new ArrayStoreException("Array of length zero.");
        }

        double[] ret = new double[a.length];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = a[i] * b[i];
        }

        return ret;
    }

    public static int[] getValidWindow(int start, int size, int validLeft, int validRight) {
        if (size < 0) {
            throw new RuntimeException("Size for window < 0! size: " + size);
        }
        if (validLeft > validRight) {
            throw new RuntimeException("validLeft > validRight! validLeft: " + validLeft + " validRight: " + validRight);
        }
        int[] window = {start, start + size};

        if (start > validRight) {
            window[0] = validRight;
            window[1] = validRight;
            return window;
        }
        if (start + size < validLeft) {
            window[0] = validLeft;
            window[1] = validLeft;
            return window;
        }
        if (start < validLeft) {
            window[0] = validLeft;
        }
        if (start + size > validRight) {
            window[1] = validRight;
        }
        return window;
    }


    public static void checkWindow(int start, int size, int validLeft, int validRight) {
        if (size < 0) {
            throw new RuntimeException("Size for window < 0! size: " + size);
        }
        if (start < validLeft) {
            String message = "start < validLeft. start/validLeft: " + start + "/" + validLeft;
            throw new RuntimeException(message);
        }
        if (start + size > validRight) {
            String message = "start + size > validRight. start+size/validRight: " + (start + size) + "/" + validRight;
            throw new RuntimeException(message);
        }
    }


    /**
     * Absolute position in the data array by pixel and slice
     *
     * @param pix
     * @param slice
     * @return absolute array position
     */
    public static int absPos(int pix, int slice, int roi) {
        return pix * roi + slice;
    }


    /**
     * Convert the fact data array to a 2 dim array of pixels and slices and snip out an desired window of slices
     *
     * @param data
     * @param skipFirst
     * @param skipLast
     * @param npix
     * @return 2dim snipped data array
     */
    public static double[][] snipPixelData(double[] data, int skipFirst, int skipLast, int npix, int roi) {

        double[][] pixelData = new double[npix][];
        for (int pix = 0; pix < npix; pix++) {
            int firstSlice = absPos(pix, skipFirst, roi);
            int lastSlice = absPos(pix + 1, (-1) * skipLast, roi);
            pixelData[pix] = Arrays.copyOfRange(data, firstSlice, lastSlice);
        }

        return pixelData;
    }

    /**
     * Calculate the statistics of given 2 dim data array, where the first field
     * is the pixel Id and the second the slices.
     *
     * @param data
     * @return pixel array of Descriptive Statistics
     */
    public static DescriptiveStatistics[] calculateTimeseriesStatistics(double[][] data) {
        int npix = data.length;
        DescriptiveStatistics[] pixelStatistics = new DescriptiveStatistics[npix];

        for (int pix = 0; pix < npix; pix++) {
            pixelStatistics[pix] = new DescriptiveStatistics(data[pix]);
        }
        return pixelStatistics;
    }


    /**
     * Flatten a 2d array
     *
     * @param array2d 2dim double array
     * @return flattend double array
     */
    public static double[] flatten2dArray(double[][] array2d) {
        return Arrays.stream(array2d)
                .flatMapToDouble(Arrays::stream)
                .toArray();
    }


    /**
     * Check if the dataitem has the timestamp key, if not return the MC data default timestamp
     *
     * @param item The event to process
     * @return The timestamp of the event
     */
    public static ZonedDateTime getTimeStamp(Data item) {
        return getTimeStamp(item, "timestamp");
    }

    /**
     * Check if the dataitem has the timestamp key, if not return the MC data default timestamp
     *
     * @param item The event to process
     * @return The timestamp of the event
     */
    public static ZonedDateTime getTimeStamp(Data item, String timeStampKey) {
        ZonedDateTime timeStamp = null;
        if (item.containsKey(timeStampKey)) {
            Utils.isKeyValid(item, timeStampKey, ZonedDateTime.class);
            timeStamp = (ZonedDateTime) item.get(timeStampKey);
        } else {
            // MC Files don't have a UnixTimeUTC in the data item. Here the timestamp is hardcoded to 1.1.2000
            // => The 12 bad pixels we have from the beginning on are used.
            timeStamp = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        }

        return timeStamp;
    }
}
