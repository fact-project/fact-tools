package fact.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Processor;
import stream.annotations.Parameter;
import stream.Data;
import fact.Constants;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;
/**
 * This processor identifies showerPixel in the image array by comparing the data in a pixel with some other time series. 
 * To compare two series the squared absolute difference between the two series is calculated. If the difference is less than the given showerthreshold the pixel will be added to the showerpixel list.
 * The operator also calculates the number unconnected subsets in the showerPixel set.   
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class StdClean implements Processor{
	static Logger log = LoggerFactory.getLogger(StdClean.class);
	private float[] comp = null;
	private double showerThreshold = 0.5f;
	static ArrayList<Integer> showerPixel  = new ArrayList<Integer>();
	private String inKey = Constants.KEY_EXFIT;
	private String output, key;




	
	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		ArrayList<ArrayList<Integer>> listOfLists = processEvent(input, key);
		int numIslands = listOfLists.size();
	    /**
	     * save PixelSetOverlay to draw in gui, and the pixelArray
	     */
		int[] showerPixelArray =  new int[showerPixel.size()];
	    PixelSet corePixel = new PixelSet();
	    for(int i = 0; i < showerPixel.size(); i++){
	    	corePixel.add(new Pixel(showerPixel.get(i)));
	    	showerPixelArray[i] = showerPixel.get(i);
	    }
		
		if(output == null || output ==""){
	    	input.put(Constants.KEY_STD_SHOWER+"_" + Constants.KEY_NUMBER_ISLANDS+"_"+key+"_"+showerThreshold, numIslands);
	    	input.put(Constants.KEY_STD_SHOWER+"_" + showerThreshold, showerPixelArray);
			input.put(Constants.KEY_STD_SHOWER+"_" + Constants.PIXELSET+"_" + showerThreshold, corePixel);
		} else {
	    	input.put(output +"_"+ Constants.KEY_NUMBER_ISLANDS, numIslands);
	    	input.put(output, showerPixelArray);
			input.put(output+"_"+ Constants.PIXELSET, corePixel);
		}
		return input;
	}

	public ArrayList<ArrayList<Integer>> processEvent(Data input, String key) {
		Serializable value = null;
		
		if(input.containsKey(key)){
			 value = input.get(key);
		} else {
			//key doesnt exist in map
			log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
		//check whether inkey exists in map and set the comp array accordingly
		if(input.containsKey(inKey)){
			Serializable inValue = input.get(inKey);
			if (inValue != null && inValue.getClass().isArray()
					&& inValue.getClass().getComponentType().equals(float.class)) {
				comp = new float[((float[]) inValue).length];
			}
		} else {
			//key doesnt exist in map
			log.debug(Constants.ERROR_WRONG_KEY + inKey + ",  " + this.getClass().getSimpleName() + ". Using ExFit operator as Default");
			comp = new ExFit().processEvent(input, key);
			
		}


		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType().equals(float.class)) {
			return processSeries((float[])value);
		}
		//in case value in Map is of the wrong type to do this calculation
		else
		{
			log.info(Constants.EXPECT_ARRAY_F + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
		
	}


	public ArrayList<ArrayList<Integer>> processSeries(float[] data) {

		//TODO: This statement should never be true. remove it?
		if (comp == null){
			comp = new ExFit().processSeries(data);
		}
		data = new SliceNormalization().processSeries(data);
		comp = new SliceNormalization().processSeries(comp);
		
		
		//List that stores all the ShowerPixel
		showerPixel.clear();
		int roi = data.length / Constants.NUMBEROFPIXEL;
		float difference = 0.0f;
		//foreach pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
							
		//iterate over all slices
			for (int slice = 1; slice < roi; slice++) {
				int pos = pix * roi + slice;
				difference += Math.pow((data[pos]- comp[pos]),2);
				
			}
			
			if(difference < showerThreshold){
			
				showerPixel.add(pix);
			}
			difference = 0.0f;
		}
		ArrayList<ArrayList<Integer>> listOfLists = EventUtils.breadthFirstSearch(showerPixel);
		
		return listOfLists;
	    
	}


	public ArrayList<ArrayList<Integer>> processSeries(float[] series, double t) {
		showerThreshold = t;
		return processSeries(series);
	}

	/*
	 * Getter and Setter
	 */
	
	public double getShowerThreshold() {
		return showerThreshold;
	}
	@Parameter (required = false, description = "If the difference is less than this threshold then a pixel will be considered a showerPixel.", defaultValue="0.5")
	public void setShowerThreshold(double showerThreshold) {
		this.showerThreshold = showerThreshold;
	}


	public String getInKey() {
		return inKey;
	}
	@Parameter (required = false, description = "The name of the item in the hashmap that the data should be compared to.", defaultValue="Constants.KEY_EXFIT")
	public void setInKey(String inKey) {
		this.inKey = inKey;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	


	
}
