/**
 * 
 */
package fact.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Processor;
import stream.Data;
import fact.Constants;
import fact.processors.parfact.CalculatePhotonCharge;
import fact.processors.parfact.RisingEdge;

/**
 * 
 * A simple feature generator for testing purposes. Calculates the standard deviation of all ArrivalTimes in a shower, 
 * the number of unconnected showerpixel sets and the sum of all photoncharges in one showerpixel set.
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class GenerateFeatures implements Processor {
	static Logger log = LoggerFactory.getLogger(GenerateFeatures.class);

	
	public GenerateFeatures() {
		
	}

	public GenerateFeatures(String[] keys) {
		this.keys=keys;
	}

	/**
     * parameter and getter setters
     */
    String[] keys = new String[] { Constants.DEFAULT_KEY };


	/**
	 * minimum Size a shower island is allowed to have 
	 */
	private int minSize;


	/**
	 * threshold before a pixel is recognized as a shoewrPixel.  
	 */
	private double showerThreshold = 0.5;


	private boolean overwrite = true;

	private int numberOfIslands;
	private float summedPhotonCharge;
	
	private String outputString = null;


	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		
		for (String key : keys) {
			double dev = processEvent(input, key);
			if(outputString == null){
				if(overwrite){
					input.put(Constants.KEY_SHOWER_ARRIVALTIME_DEV, dev);
					input.put(Constants.KEY_NUMBER_ISLANDS, numberOfIslands);
					input.put(Constants.KEY_SHOWER_PHOTONCHARGE, summedPhotonCharge);
				} else {
					input.put(Constants.KEY_SHOWER_ARRIVALTIME_DEV+"_"+ key, dev);
					input.put(Constants.KEY_NUMBER_ISLANDS+"_"+ key, numberOfIslands);
					input.put(Constants.KEY_SHOWER_PHOTONCHARGE+"_"+ key, summedPhotonCharge);
				}
			} else {
				if(overwrite){
					input.put(Constants.KEY_SHOWER_ARRIVALTIME_DEV+"_"+ outputString, dev);
					input.put(Constants.KEY_NUMBER_ISLANDS+"_"+ outputString, numberOfIslands);
					input.put(Constants.KEY_SHOWER_PHOTONCHARGE+"_"+ outputString, summedPhotonCharge);
				} else {
					input.put(Constants.KEY_SHOWER_ARRIVALTIME_DEV+"_"+ outputString+"_"+ key, dev);
					input.put(Constants.KEY_NUMBER_ISLANDS+"_"+ outputString+"_"+ key, numberOfIslands);
					input.put(Constants.KEY_SHOWER_PHOTONCHARGE+"_"+ outputString+"_"+ key, summedPhotonCharge);
				}
			}
		}
		return input;
	}

	public double processEvent(Data input, String key) {
		
		Serializable value = null;
		
		if(input.containsKey(key)){
			 value = input.get(key);
		} else {
			log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
			return 0;
		}
		
		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType().equals(float.class)) {
			return processSeries((float[]) value);
			
		}
		//in case value in Map is of the wrong type to do this calculation
		else
		{
			return 0;
		}
		
	}

	public double processSeries(float[] series) {
		
		//get listoflist
		ArrayList<ArrayList<Integer>> ll =  new StdClean().processSeries(series, showerThreshold);
		ArrayList<Integer> showerPixel =  new ArrayList<Integer>();
		numberOfIslands = 0;
		float[] photonCharges = new CalculatePhotonCharge().processSeries(series);
		summedPhotonCharge = 0;
		
		for(ArrayList<Integer> l : ll) {
			if(l.size() >= minSize){
				showerPixel.addAll(l);
				numberOfIslands++;
			}
		}
		
	
		//sum all photonchrages
		for (int pix : showerPixel){
			summedPhotonCharge += photonCharges[pix];
		}
		//calculate stddev of arrivaltimes
		int[] arrivalTimes = new RisingEdge().processSeries(series);
		double averageArrivalTime = 0;
		for(int time : arrivalTimes){
			averageArrivalTime +=time;
		}
		averageArrivalTime = ((double)averageArrivalTime)/((double)arrivalTimes.length);
		double dev = 0;
		for(int time: arrivalTimes){
			dev += Math.pow((time - averageArrivalTime), 2);
		}
		dev = Math.sqrt(1/((double)arrivalTimes.length - 1) * dev);
		
		return dev;
	}
	
	/* Getter and Setter*/
	public String[] getKeys() {
		return keys;
	}

	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	public String getOutputString() {
		return outputString;
	}
	public void setOutputString(String outputString) {
		this.outputString = outputString;
	}
	
	public int getMinSize() { 
		return minSize; 
	}
	
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}
	public boolean isOverwrite() {
		return overwrite;
	}
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public double getShowerThreshold() {
		return showerThreshold;
	}
	public void setShowerThreshold(double showerThreshold) {
		this.showerThreshold = showerThreshold;
	}


}
