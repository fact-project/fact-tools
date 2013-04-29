package fact.processors.parfact;

import java.io.Serializable;
import java.util.ArrayList;

import stream.Processor;
import stream.annotations.Parameter;
import stream.Data;
import fact.Constants;
import fact.data.EventUtils;
import fact.data.FactEvent;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;
/**
 *CoreNeighborClean. Identifies showerPixel in the image array.
 *	 Cleaning in three Steps:
 * 	1) Identify all Core Pixel (Photoncharge higher than corePixelThreshold)
 * 	2) Remove all Single Core Pixel
 * 	3) Add all Neighbor Pixel, whose Photoncharge is higher than neighborPixelThreshold
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 *
 */
public class CoreNeighborClean implements Processor{
	private String key;
	private String output;
	private  PixelSet corePixelSet;
	private  float corePixelThreshold = 5.0f;
	private  float neighborPixelThreshold = 2.0f;
	private boolean overwrite = true;
	private int minSize = 2;

	@Override
	public Data process(Data input) {
		if(output == null || output ==""){
			input.put(Constants.KEY_CORENEIGHBOURCLEAN, processEvent(input, key));
			input.put(Constants.KEY_CORENEIGHBOURCLEAN+"_"+Constants.PIXELSET, corePixelSet);
		} else {
			input.put(output, processEvent(input, key));
			input.put(output +"_" + Constants.PIXELSET, corePixelSet);
		}
		return input;
	}

	public int[] processEvent(Data input, String key) {
		Serializable value = null;
		if(input.containsKey(key)){
			value = input.get(key);
		} else {
			//key doesn't exist in map
			return null;
		}
		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType().equals(float.class)) {
			return processSeries((float[]) value);
		}
		//in case value in Map is of the wrong type to do this calculation
		else
		{
			return null;
		}

	}

	public int[] processSeries(float[] value) {

		float[] photonCharge = new float[Constants.NUMBEROFPIXEL];
		float[] data = value;
		int[] currentNeighbors;
		photonCharge = new CalculatePhotonCharge().processSeries(data);

		ArrayList<Integer> showerPixel= new ArrayList<Integer>();

		for(int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++)
		{ 
			if (photonCharge[pix] > corePixelThreshold){
				showerPixel.add(pix);
			}
		}

		ArrayList<ArrayList<Integer>> listOfLists = EventUtils.breadthFirstSearch(showerPixel);
		showerPixel.clear();
		for (ArrayList<Integer> l: listOfLists){
			if(l.size() >=2){
				showerPixel.addAll(l);
			}
		}

		ArrayList<Integer> newList = new ArrayList<Integer>();
		newList.addAll(showerPixel);
		for (int pix: showerPixel){
			currentNeighbors = FactEvent.PIXEL_MAPPING.getNeighborsFromChid(pix);
			for (int nPix:currentNeighbors){
				if(nPix != -1    && photonCharge[nPix] > neighborPixelThreshold && !newList.contains(nPix)){
					newList.add(nPix);
				}
			}
		}


		corePixelSet = new PixelSet();
		int[] showerPixelArray =  new int[showerPixel.size()];
		for(int i = 0; i < showerPixel.size(); i++){
			showerPixelArray[i] = showerPixel.get(i);
			corePixelSet.add(new Pixel(showerPixel.get(i)));
		}
		return showerPixelArray;
	}

	/*
	 * Getter and Setter
	 */


	public float getCorePixelThreshold() {
		return corePixelThreshold;
	}
	@Parameter(required = false, description = "The smallest PhotonCharge a Pixel must have to be identified as a CorePixel", defaultValue = "5.0")
	public void setCorePixelThreshold(float corePixelThreshold) {
		this.corePixelThreshold = corePixelThreshold;
	}

	public float getNeighborPixelThreshold() {
		return neighborPixelThreshold;
	}
	@Parameter(required = false, description = "The smallest PhotonCharge a Pixel must have thats adjacent to a previously identified corePixel", defaultValue = "2.0")
	public void setNeighborPixelThreshold(float neighborPixelThreshold) {
		this.neighborPixelThreshold = neighborPixelThreshold;
	}

	public boolean isOverwrite() {
		return overwrite;
	}
	@Parameter (required = false, description = "If true this operator will output the result as " + Constants.KEY_CORENEIGHBOURCLEAN +"+{current Key}. Else the result will be named " + Constants.KEY_CORENEIGHBOURCLEAN)
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public int getMinSize() {
		return minSize;
	}
	@Parameter(required = false, description = "Number of Pixels a patch of CorePixel must have before its Neighbours are even considered for NeighbourCorePixel. If Size is smaller than minSize the Pixels will be discarded", defaultValue = "2.0")
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}


	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}

}
