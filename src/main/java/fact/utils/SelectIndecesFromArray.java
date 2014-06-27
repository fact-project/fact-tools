/**
 * 
 */
package fact.utils;

import fact.Utils;
import stream.Data;
import stream.Processor;

import java.io.Serializable;

/**
 * This processors takes an array and an array of indices. It puts all values with the given indeces into a new array. That means the new Array is of the same length as the indices array.
 * 
 * 
 * @author kai
 * 
 */
public class SelectIndecesFromArray implements Processor{
	private String indices;
	private String key;
	private String outputKey;
	
	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(getClass(), input, key, indices);

		Serializable value = input.get(key);

		int[] indexArray = null;
		indexArray = (int[]) input.get(indices);
		
		double[] sAr = new double[indexArray.length];
		
		if(value.getClass().isArray()){
			double[] arr = Utils.toDoubleArray(value);
//			System.out.println("arr.length: " + arr.length);
			int i = 0;
			for(int pix : indexArray){
				double b =  arr[pix];
				sAr[i] = b;
				i++;
			}
		}
		
		input.put(outputKey, sAr);
		return input;
	}

	public String getIndices() {
		return indices;
	}
	public void setIndices(String indices) {
		this.indices = indices;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	

}