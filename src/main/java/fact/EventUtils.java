package fact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fact.mapping.FactCameraPixel;
import fact.mapping.FactPixelMapping;
import fact.mapping.ui.components.cameradisplay.PixelMapDisplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;

/**
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class EventUtils {
	static Logger log = LoggerFactory.getLogger(EventUtils.class);
	
	
	
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
		ArrayList<ArrayList<Integer>> listOfLists = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> marked = new ArrayList<Integer>();
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
					FactCameraPixel[] neighbors = pixelMap.getNeighboursFromID(q.get(index));
					for (FactCameraPixel i : neighbors) {
						if (showerPixel.contains(i.id) && !marked.contains(i.id)) {
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

	/**
	 * helper method to create an array of ints from an Integer array. for the
	 * same reason stated above. Maybe this is different in java 7 ?
	 */
	public static int[] toIntArray(Integer[] l) {
		int[] ret = new int[l.length];
		int i = 0;
		for (Integer e : l)
			ret[i++] = e.intValue();
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

	//turn a double aray into a float array
	public static float[] doubleToFloatArray(double[] ar) {
		// return toDoubleArray(ar)
		if (ar != null) {
			float[] ret = new float[ar.length];
			for (int i = 0; i < ar.length; i++) {
				ret[i] = (float) ar[i];
			}
			return ret;
		} else {
			return null;
		}
	}

	//returns true if the specified value is anywhere in the array
	public static boolean arrayContains(int[] ar, int value) {
        for (int anAr : ar) {
            return anAr == value;
        }
		return false;
	}

	public static double[] toDoubleArray(Serializable arr){
		if(arr.getClass().isArray()){
			Class<?> clazz = arr.getClass().getComponentType();
			if(clazz.equals(float.class)){
				return floatToDoubleArray((float[])arr);
			} else if(clazz.equals(double.class)){
				return (double[])arr;
			} else if(clazz.equals(int.class)){
				return intToDoubleArray((int[])arr);
			}
		}
		return null;
	}

	public static double valueToDouble(Serializable val) {
		Class<?> clazz = val.getClass();
		if(clazz.equals(float.class)||clazz.equals(Float.class)){
			return (Float) val;
		} else if(clazz.equals(double.class)||clazz.equals(Double.class)){
			return (Double) val;
		} else if(clazz.equals(int.class)|| clazz.equals(Integer.class)) {
			return (Integer) val;
		}
		
		return 0;
	}


	public static float[] intToFloatArray(int[] ar) {
		// return toDoubleArray(ar)
		if (ar != null) {
			float[] ret = new float[ar.length];
			for (int i = 0; i < ar.length; i++) {
				ret[i] = (float) ar[i];
			}
			return ret;
		} else {
			return null;
		}
	}
//	public static void mapContainsKeysWithTypes(Class<?> caller, Data item, Class<?>[] types,    String... keys ){
//		
//	}
	public static void mapContainsKeys(Class<?> caller, Data item,  String... keys ){
		ArrayList<String> e = new ArrayList<String>();
		boolean isValid = true;
		if(keys == null){
			isValid = false;
		}
		for(String key : keys){
			if(key == null || !item.containsKey(key)){
				isValid = false;
				e.add(key);
			}
		}
		if(!isValid){
			StringBuilder b = new StringBuilder();
			for(String er: e){
				b.append(er);
				b.append("\n");
			}
			throw new RuntimeException("Missing keys for processor " + caller.getSimpleName() + ":  " +b.toString() );
		}
	}
	
	public static void isKeyValid(Class<?> caller, Data item, String key, Class<?> cl){
		if(key == null || key.equals("")){
			log.error("Key was empty");
		}
		if(!item.containsKey(key)){
			log.error("Data does not contain the key " + key);
			throw new RuntimeException("Did not find key "+  key + "  in the event. For processor:  " + caller.getName());
		}
		try{
			cl.cast( item.get(key));
		} catch (ClassCastException e){
			log.error("The value for the key " + key + " cannot be cast to " + cl.getSimpleName() + " for processor: " + caller.getName());
			throw e;
		}
	}
	
	public static double[] rotatePointInShowerSystem(double x,double y,double cogx,double cogy,double delta)
	{
		double xtr = x;
		double ytr = y;
		double[] result = {0,0};
		
		// translate the coord., so that the COG is in the center
		xtr -= cogx;
		ytr -= cogy;
		
		result[0] = xtr*Math.cos(delta) - ytr*Math.sin(delta);
		result[1] = xtr*Math.sin(delta) + ytr*Math.cos(delta);
		
		return result;
	}
	
	/**
	 * Sum up array
	 * @param a
	 * @return
	 */
	public static Double arraySum(double[] a)
	{
		if(a == null)
			return 0.0;
		if(a.length == 0)
			return 0.0;
		
		double ret = 0.0;
		
		for(int i = 0; i < a.length; i++)
		{
			ret += a[i];
		}
		
		return ret;
	}
	
	/**
	 * Elementwise multiplication of arrays
	 * @param a
	 * @param b
	 * @return double array containing a[i] * b[i]
	 * @throws ArrayStoreException
	 */
	public static double[] arrayMultiplication(double[] a, double[] b) throws ArrayStoreException
	{
		if (a == null || b == null)
		{
			return null;
		}
		if (a.length != b.length)
		{
			throw new ArrayStoreException("Array sizes do not match.");
		}
		if(a.length == 0)
		{
			throw new ArrayStoreException("Array of length zero.");
		}
		
		double[] ret = new double[a.length];
		
		for(int i = 0; i < ret.length; i++)
		{
			ret[i] = a[i] * b[i];
		}
		
		return ret;
	}
	
}
