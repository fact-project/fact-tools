package fact.cleaning;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.EventUtils;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;
import fact.viewer.ui.DefaultPixelMapping;
/**
 *CoreNeighborClean. Identifies showerPixel in the image array.
 *	 Cleaning in three Steps:
 * 	1) Identify all Core Pixel (Photoncharge higher than corePixelThreshold)
 * 	2) Remove all Single Core Pixel
 * 	3) Add all Neighbor Pixel, whose Photoncharge is higher than neighborPixelThreshold
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 *
 */

public class CoreNeighborClean implements StatefulProcessor{
	static Logger log = LoggerFactory.getLogger(CoreNeighborClean.class);
	private String key = null;
	private String keyPositions = null;
	private String outputKey;
	private  PixelSet corePixelSet;
	private  double corePixelThreshold = 0.0;
	private  double neighborPixelThreshold = 0.0;
	private  double timeThreshold = 0.0; 
	private int minNumberOfPixel = 0;
	
	double[] photonCharge = new double[Constants.NUMBEROFPIXEL];
	
	@Override
	public void resetState() throws Exception {
	}
	@Override
	public void finish() throws Exception {
	}

	@Override
	public void init(ProcessContext context) throws Exception {
		if(corePixelThreshold == 0){
			log.warn("corePixelThrtshold not set using 5.0 as default ");
			corePixelThreshold = 5.0f;
		}
		if(neighborPixelThreshold == 0){
			log.warn("neighbourPixelThtschold not set using 2.0 as default ");
			neighborPixelThreshold = 2.0f;
		}
		if(minNumberOfPixel == 0){
			log.warn("minNumberOfPixel not set using 2 as default ");
			minNumberOfPixel = 2;
		}
		if(outputKey == null){
			log.error("Missing outputKey Aborting.");
			throw new RuntimeException("Missing parameters. Aborting.");
		}
	}
	
	@Override
	public Data process(Data input) {
		try{
			//EventUtils.mapContainsKeys(getClass(), input, key, keyPositions);
			EventUtils.mapContainsKeys(getClass(), input, key);
			photonCharge= (double[]) input.get(key);
			if(photonCharge == null){
				log.error("No weights found in event. Aborting.");
				throw new RuntimeException("No weights found in event. Aborting.");
			}
		} catch(ClassCastException e){
			log.error("Could cast the key: " + key + "to a double[]");
		}
		

		int[] currentNeighbors;
		ArrayList<Integer> showerPixel= new ArrayList<Integer>();
		// Add all pixel with a weight > corePixelThrshold to the showerpixel list.
		for(int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++)
		{ 
			if (photonCharge[pix] > corePixelThreshold){
				showerPixel.add(pix);
			}
		}
		Integer[] level1 = new Integer[showerPixel.size()];
		showerPixel.toArray(level1);
		//
		ArrayList<ArrayList<Integer>> listOfLists = EventUtils.breadthFirstSearch(showerPixel);
		showerPixel.clear();
		for (ArrayList<Integer> l: listOfLists){
			if(l.size() >= minNumberOfPixel){
				showerPixel.addAll(l);
			}
		}
		Integer[] level2 = new Integer[showerPixel.size()];
		showerPixel.toArray(level2);
		
		ArrayList<Integer> newList = new ArrayList<Integer>();
		newList.addAll(showerPixel);
		for (int pix: showerPixel){
			currentNeighbors = DefaultPixelMapping.getNeighborsFromChid(pix);
			for (int nPix:currentNeighbors){
				if(nPix != -1    && photonCharge[nPix] > neighborPixelThreshold && !newList.contains(nPix)){
					newList.add(nPix);
				}
			}
		}
		showerPixel = newList;
		Integer[] level3 = new Integer[newList.size()];
		showerPixel.toArray(level3);

		int[] showerPixelArray =  new int[showerPixel.size()];
		for(int i = 0; i < showerPixel.size(); i++){
			showerPixelArray[i] = showerPixel.get(i);
		}

		double median;
		//do a "timeMedianClean" in case the timethrshold is set 
		if(timeThreshold > 0 && keyPositions != null && showerPixelArray.length != 0){
			
			int[] positions = (int[]) input.get(keyPositions);
			if (positions == null){
				log.error("The key " + keyPositions + "  was not found in the data");
				throw new RuntimeException("The key " + keyPositions + "  was not found in the data");
			}
			//calculate the median value of the arrival times in the shower
			int[] showerArrivals = new int[showerPixelArray.length];
			int i = 0;
			for (int pixel : showerPixelArray){
				showerArrivals[i] = positions[pixel];
				i++;
			}
			Arrays.sort(showerArrivals);
			int length = showerArrivals.length;
			if (showerArrivals.length%2 == 1 ){
				median =  showerArrivals[(length-1)/2];
			} else {
				median = 0.5*(  showerArrivals[(length)/2] + showerArrivals[(length)/2 - 1] );
			}
			
			
			//count number of pixel with arrival time within the threshold
			int c = 0;
			for(int pixel: showerPixelArray){
				if(Math.abs(positions[pixel] - median) < timeThreshold){
					c++;
				}
			}
			int[] newShowerPixelArray = new int[c];
			int k = 0;
			for(int pixel: showerPixelArray){
//				System.out.println(Math.abs(positions[pixel] - median));
				if(Math.abs(positions[pixel] - median) < timeThreshold){
					newShowerPixelArray[k] = pixel;
					k++;
				}
			}
//			System.out.println("vorher: " + showerPixelArray.length + "  nachher: " + newShowerPixelArray.length);
			showerPixelArray = newShowerPixelArray;
		}
		
		corePixelSet = new PixelSet();
        for (int aShowerPixelArray : showerPixelArray) {
            corePixelSet.add(new Pixel(aShowerPixelArray));
        }
		
//		PixelSet l1 = new PixelSet();
//		for(int i = 0; i < level1.length; i++){
//			l1.add(new Pixel(level1[i]));
//		}
//		PixelSet l3 = new PixelSet();
//		for(int i = 0; i < level3.length; i++){
//			l3.add(new Pixel(level3[i]));
//		}
//		PixelSet l2 = new PixelSet();
//		for(int i = 0; i < level2.length; i++){
//			l2.add(new Pixel(level2[i]));
//		}
		if(showerPixelArray.length > 0){
//			input.put(outputKey+"_level1", level1);
//			input.put(outputKey+"_level1" +"_"+Constants.PIXELSET, l1);
//			input.put(outputKey+"_level2", level2);
//			input.put(outputKey+"_level2" +"_"+Constants.PIXELSET, l2);
//			input.put(outputKey+"_level3", level3);
//			input.put(outputKey+"_level3" +"_"+Constants.PIXELSET, l3);
			input.put(outputKey, showerPixelArray);
			input.put(outputKey+"_"+Constants.PIXELSET, corePixelSet);
		}
//			input.put(outputKey+"_numCorePixel", numCorePixel);
		return input;
	}

	
	/*
	 * Getter and Setter
	 */
	public double getCorePixelThreshold() {
		return corePixelThreshold;
	}
	@Parameter(required = false, description = "The smallest PhotonCharge a Pixel must have to be identified as a CorePixel", defaultValue = "5.0")
	public void setCorePixelThreshold(float corePixelThreshold) {
		this.corePixelThreshold = corePixelThreshold;
	}

	public double getNeighborPixelThreshold() {
		return neighborPixelThreshold;
	}
	@Parameter(required = false, description = "The smallest PhotonCharge a Pixel must have thats adjacent to a previously identified corePixel", defaultValue = "2.0")
	public void setNeighborPixelThreshold(float neighborPixelThreshold) {
		this.neighborPixelThreshold = neighborPixelThreshold;
	}

	public int getMinNumberOfPixel() {
		return minNumberOfPixel;
	}
	@Parameter(required = false, description = "Number of Pixels a patch of CorePixel must have before its Neighbours are even considered for NeighbourCorePixel. If Size is smaller than minSize the Pixels will be discarded", defaultValue = "2.0")
	public void setMinNumberOfPixel(int minSize) {
		this.minNumberOfPixel = minSize;
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


	public String getKeyPositions() {
		return keyPositions;
	}
	public void setKeyPositions(String keyPositions) {
		this.keyPositions = keyPositions;
	}


	public double getTimeThreshold() {
		return timeThreshold;
	}


	public void setTimeThreshold(double timeThreshold) {
		this.timeThreshold = timeThreshold;
	}
}
