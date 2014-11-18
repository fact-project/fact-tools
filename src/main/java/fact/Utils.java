package fact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;

/**
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class Utils {
	static Logger log = LoggerFactory.getLogger(Utils.class);

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
	 * This takes a data array (of length pixels * roi) and returns an
	 * array(length = roi) in which each entry is the average over all the
	 * values of the other pixels in that slice.
	 * 
	 * @param data
	 *            array of length pixels*region of interest
	 * @param roi
	 *            region of interrest. usualy 300 or 1024 for fact data
	 * @return an array of length roi containing the slice averages
	 */
	public static double[] averageSlicesForEachPixel(double[] data, int roi) {
		roi = data.length / Constants.NUMBEROFPIXEL;
		double[] average = new double[roi];
		double[][] values = Utils.sortPixels(data, 1440);
		int slice = 0;
		for (double[] slices : values) {
			for (double s : slices) {
				average[slice] += s;
				slice++;
			}
			slice = 0;
		}
		for (int i = 0; i < average.length; i++) {
			average[i] /= 1440.0;
		}
		return average;
	}

	/**
	 * Finds all unconnected sets of pixel in the showerPixel List and returns a
	 * list of lists. Each list containing one separate set. Does a BFs search.
	 * See the wikipedia article on BFS. This version is not as memory efficient
	 * as it could be.
	 * 
	 * @param showerPixel
	 *            the list to search in
	 * @return A list of lists.
	 */
	public static ArrayList<ArrayList<Integer>> breadthFirstSearch(
			List<Integer> showerPixel) {
		ArrayList<ArrayList<Integer>> listOfLists = new ArrayList<>();
		HashSet<Integer> marked = new HashSet<>();
		FactPixelMapping pixelMap = FactPixelMapping.getInstance();

		for (int pix : showerPixel) {
			if (!marked.contains(pix)) {
				// start BFS
				marked.add(pix);
				ArrayList<Integer> q = new ArrayList<Integer>();
				q.add(pix);
				// cannot use the enhanced for loop here.
				for (int index = 0; index < q.size() && !q.isEmpty(); index++) {
					// add neighbours to q
					FactCameraPixel[] neighbors = pixelMap
							.getNeighboursFromID(q.get(index));
					for (FactCameraPixel i : neighbors) {
						if (showerPixel.contains(i.id)
								&& !marked.contains(i.id)) {
							q.add(i.id);
							marked.add(i.id);
						}
					}
				}
				listOfLists.add(q);
			}
		}
		return listOfLists;
	}

	/**
	 * make an Array of the type int[] into ArrayList<Integer>. We need this
	 * because there is no autoboxing between Integer and int types hence
	 * Arrays.aslist cannot be used for this conversion
	 */
	public static ArrayList<Integer> arrayToList(int[] a) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
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

	// returns true if the specified value is anywhere in the array
	public static boolean arrayContains(int[] ar, int value) {
		for (int anAr : ar) {
			return anAr == value;
		}
		return false;
	}

	public static double[] toDoubleArray(Serializable arr) {
		if (arr.getClass().isArray()) {
			Class<?> clazz = arr.getClass().getComponentType();
			if (clazz.equals(float.class)) {
				return floatToDoubleArray((float[]) arr);
			} else if (clazz.equals(double.class)) {
				return (double[]) arr;
			} else if (clazz.equals(int.class)) {
				return intToDoubleArray((int[]) arr);
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
		ArrayList<String> e = new ArrayList<>();
		boolean isValid = true;
		if (keys == null) {
			isValid = false;
		}
		for (String key : keys) {
			if (key == null || !item.containsKey(key)) {
				isValid = false;
				e.add(key);
			}
		}
		if (!isValid) {
			StringBuilder b = new StringBuilder();
			for (String er : e) {
				b.append(er);
				b.append("\n");
			}
			StackTraceElement traceElement = Thread.currentThread()
					.getStackTrace()[2];
			String caller = traceElement.getClassName();
			throw new RuntimeException("Missing keys for processor " + caller
					+ ":  " + b.toString());
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
	public static double[] transformToEllipseCoordinates(double x, double y,
			double cogX, double cogY, double delta) {
		double translatedX = x - cogX;
		double translatedY = y - cogY;

		double dist = Math.sqrt(translatedX * translatedX + translatedY
				* translatedY);

		double beta = Math.atan2(translatedY, translatedX);
		double alpha = (beta - delta);

		double t = Math.sin(alpha) * dist;
		double l = Math.cos(alpha) * dist;
		double[] c = { l, t };

		return c;
	}
    
    public static double calculateDistancePointToShowerAxis(double cogx, double cogy, double delta, double x, double y){
    	double distance = 0;
    	
    	double r0 = (x-cogx)*Math.cos(delta)+(y-cogy)*Math.sin(delta);
    	
    	distance = Math.sqrt( Math.pow(cogx-x+r0*Math.cos(delta),2)+Math.pow(cogy-y+r0*Math.sin(delta),2) );
    	
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

		for (int i = 0; i < a.length; i++) {
			ret += a[i];
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
	
	public static int[] getValidWindow(int start, int size, int validLeft, int validRight)
	{
		if (size < 0)
		{
			throw new RuntimeException("Size for window < 0! size: "+ size);
		}
		int[] window = {start,start+size};
		if (start < validLeft)
		{
			window[0] = validLeft;
		}
		if (start+size > validRight)
		{
			window[1] = validRight;
		}
		if (window[1] < window[0])
		{
			window[1] = window[0];
		}
		return window;
	}
	
	
	public static void checkWindow(int start, int size, int validLeft, int validRight)
	{	
		if (size < 0)
		{
			throw new RuntimeException("Size for window < 0! size: "+ size);
		}
		if (start < validLeft)
		{
			String message = "start < validLeft. start/validLeft: " + start + "/" + validLeft;
			throw new RuntimeException(message);
		}
		if (start+size > validRight)
		{
			String message = "start + size > validRight. start+size/validRight: " + (start+size) + "/" + validRight;
			throw new RuntimeException(message);
		}	
	}
	


}
