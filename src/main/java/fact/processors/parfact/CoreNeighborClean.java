package fact.processors.parfact;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.data.EventUtils;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;
import fact.processors.FactEvent;
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
	static Logger log = LoggerFactory.getLogger(CoreNeighborClean.class);
	private String key = "photoncharge";
	private String outputKey;
	private  PixelSet corePixelSet;
	private  float corePixelThreshold = 5.0f;
	private  float neighborPixelThreshold = 2.0f;
	private int minSize = 2;
	float[] photonCharge = new float[Constants.NUMBEROFPIXEL];
	

	@Override
	public Data process(Data input) {
		try{
			photonCharge= (float[]) input.get(key);
			if(photonCharge == null){
				return null;
			}
		} catch(ClassCastException e){
			log.error("Could cast the key: " + key + "to a float[]");
		}
		int[] currentNeighbors;

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
		
		
		if(outputKey == null || outputKey ==""){
			input.put(key, showerPixelArray);
			input.put(key+"_"+Constants.PIXELSET, corePixelSet);
		} else {
			input.put(outputKey, showerPixelArray);
			input.put(outputKey+"_"+Constants.PIXELSET, corePixelSet);
		}
		return input;
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


	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String output) {
		this.outputKey = output;
	}

}
