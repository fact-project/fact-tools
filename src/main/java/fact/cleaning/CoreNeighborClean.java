package fact.cleaning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.Processor;
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

public class CoreNeighborClean implements Processor{
	static Logger log = LoggerFactory.getLogger(CoreNeighborClean.class);

    @Parameter(required = true)
	private String key;
    @Parameter(required = true)
	private String keyPositions;
    @Parameter(required = true)
	private String outputKey;
	@Parameter(required = true, description = "The smallest PhotonCharge a Pixel must have to be identified as a CorePixel", defaultValue = "5.0")
	private  double corePixelThreshold;
	@Parameter(required = true, description = "The smallest PhotonCharge a Pixel must have thats adjacent to a previously identified corePixel", defaultValue = "2.0")
	private  double neighborPixelThreshold;
    @Parameter(required = true)
	private  double timeThreshold;
    @Parameter(required = true, description = "Number of Pixels a patch of CorePixel must have before its Neighbours are even considered for NeighbourCorePixel. If Size is smaller than minSize the Pixels will be discarded", defaultValue = "2.0")
	private int minNumberOfPixel;
    @Parameter(required = false)
    private String[] starPositionKeys = null;
    @Parameter(required = false)
	private double starRadiusInCamera = Constants.PIXEL_SIZE;
    
    private boolean showDifferentCleaningSets = false;


    private  PixelSet cleanedPixelSet;
	
	double[] photonCharge = new double[Constants.NUMBEROFPIXEL];
	
	double[] positions = new double[Constants.NUMBEROFPIXEL];
	

	private PixelSet starSet;


	@Override
	public Data process(Data input) {
		EventUtils.mapContainsKeys(getClass(), input, key,keyPositions);
		
			
		photonCharge= EventUtils.toDoubleArray(input.get(key));
		positions = EventUtils.toDoubleArray(input.get(keyPositions));
		
		ArrayList<Integer> showerPixel= new ArrayList<Integer>();
		// Add all pixel with a weight > corePixelThreshold
		// to the showerpixel list.
		for(int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++)
		{ 
			if (photonCharge[pix] > corePixelThreshold){
				showerPixel.add(pix);
			}
		}
		Integer[] level1 = new Integer[showerPixel.size()];
		showerPixel.toArray(level1);
		
		// Remove all clusters of corepixels
		// with less than minNumberOfPixel pixels in the cluster
		showerPixel = removeSmallCluster(showerPixel);
		Integer[] level2 = new Integer[showerPixel.size()];
		showerPixel.toArray(level2);
		
		Integer[] level2a = null;
		starSet = new PixelSet();
		if (starPositionKeys != null)
		{
			for (String starPositionKey : starPositionKeys)
			{
				EventUtils.mapContainsKeys(getClass(), input,starPositionKey);
				double[] starPosition = (double[]) input.get(starPositionKey);
				showerPixel = removeStarIslands(showerPixel,starPosition);
				level2a = new Integer[showerPixel.size()];
				showerPixel.toArray(level2a);
			}
		}
		
		// Add all neighboring pixels of the core pixels,
		// with a weight > neighborPixelThreshold to the showerpixellist
		showerPixel.addAll(addNeighboringPixels(showerPixel));
		Integer[] level3 = new Integer[showerPixel.size()];
		showerPixel.toArray(level3);
		
		Integer[] level4 = null;
		// do a "timeMedianClean" in case the timeThreshold is set 
		if(timeThreshold > 0 && keyPositions != null && showerPixel.size() != 0){
			showerPixel = applyTimeMedianCleaning(showerPixel);
			level4 = new Integer[showerPixel.size()];
			showerPixel.toArray(level4);
			showerPixel = removeSmallCluster(showerPixel);
			
			if (starPositionKeys != null)
			{
				for (String starPositionKey : starPositionKeys)
				{
					double[] starPosition = (double[]) input.get(starPositionKey);
					showerPixel = removeStarIslands(showerPixel,starPosition);
				}
			}
		}	
		
		// Convert list to array
		int[] showerPixelArray =  new int[showerPixel.size()];
		for(int i = 0; i < showerPixel.size(); i++){
			showerPixelArray[i] = showerPixel.get(i);
		}
		
		if (showDifferentCleaningSets == true){
			if (level1.length > 0)
			{
	    		PixelSet l1 = new PixelSet();
	    		for(int i = 0; i < level1.length; i++){
	    			l1.add(new Pixel(level1[i]));
	    		}
	    		input.put(outputKey+"Level1Set", l1);
			}
			if (level2.length > 0)
			{
	    		PixelSet l2 = new PixelSet();
	    		for(int i = 0; i < level2.length; i++){
	    			l2.add(new Pixel(level2[i]));
	    		}
	    		input.put(outputKey+"Level2Set", l2);
			}
			if (level2a != null)
			{
				if (level2a.length > 0)
				{
		    		PixelSet l2a = new PixelSet();
		    		for(int i = 0; i < level2a.length; i++){
		    			l2a.add(new Pixel(level2a[i]));
		    		}
		    		input.put(outputKey+"Level2aSet", l2a);
					input.put("Starset", starSet);
				}
			}
			if (level3.length > 0)
			{
	    		PixelSet l3 = new PixelSet();
	    		for(int i = 0; i < level3.length; i++){
	    			l3.add(new Pixel(level3[i]));
	    		}
	    		input.put(outputKey+"Level3Set", l3);
			}
			if (level4 != null)
			{
				if (level4.length > 0)
				{
		    		PixelSet l4 = new PixelSet();
		    		for(int i = 0; i < level4.length; i++){
		    			l4.add(new Pixel(level4[i]));
		    		}
		    		input.put(outputKey+"Level4Set", l4);
				}
			}
        }
		
		
		if(showerPixelArray.length > 0){

			cleanedPixelSet = new PixelSet();
	        for (int aShowerPixelArray : showerPixelArray) {
	        	cleanedPixelSet.add(new Pixel(aShowerPixelArray));
	        }
	        
			input.put(outputKey, showerPixelArray);
			input.put(outputKey+"Set", cleanedPixelSet);
		}
		return input;
	}
	
	private ArrayList<Integer> removeStarIslands(ArrayList<Integer> showerPixel, double[] starPosition) {
		
		int chidOfPixelOfStar = DefaultPixelMapping.coordinatesToChid(starPosition[0], starPosition[1]);
		List<Integer> starChidList = new ArrayList<Integer>();
		
		starChidList.add(chidOfPixelOfStar);

		starSet.add(new Pixel(chidOfPixelOfStar));
		
		for (int px: DefaultPixelMapping.getNeighborsFromChid(chidOfPixelOfStar))
		{
			if (px != -1)
			{
				if (calculateDistance(px, starPosition[0], starPosition[1]) < starRadiusInCamera)
				{
					starSet.add(new Pixel(px));
					starChidList.add(px);
				}
			}
		}
		
		ArrayList<ArrayList<Integer>> listOfLists = EventUtils.breadthFirstSearch(showerPixel);
		ArrayList<Integer> newList = new ArrayList<Integer>();
		for (ArrayList<Integer> l: listOfLists){
			if ((l.size() <= starChidList.size() && starChidList.containsAll(l)) == false)
			{
				newList.addAll(l);
			}
		}
		return newList;
	}
	
	private double calculateDistance(int chid,double x,double y)
	{
		double xdist = DefaultPixelMapping.getPosXinMM(chid) - x;
		double ydist = DefaultPixelMapping.getPosYinMM(chid) - y;
		
		return Math.sqrt((xdist*xdist)+(ydist*ydist));
	}
	
	private ArrayList<Integer> applyTimeMedianCleaning(ArrayList<Integer> list) {
		
		double[] showerArrivals = new double[list.size()];
		int i = 0;
		for (int pixel : list){
			showerArrivals[i] = positions[pixel];
			i++;
		}
		double median = calculateMedian(showerArrivals);
		
		ArrayList<Integer> newList= new ArrayList<Integer>();
		for(int pixel: list){
			if(Math.abs(positions[pixel] - median) < timeThreshold){
				newList.add(pixel);
			}
		}		
		return newList;
	}

	private ArrayList<Integer> removeSmallCluster(ArrayList<Integer> list)
	{
		ArrayList<ArrayList<Integer>> listOfLists = EventUtils.breadthFirstSearch(list);
		ArrayList<Integer> newList = new ArrayList<Integer>();
		for (ArrayList<Integer> l: listOfLists){
			if(l.size() >= minNumberOfPixel){
				newList.addAll(l);
			}
		}
		return newList;
	}
	
	private ArrayList<Integer> addNeighboringPixels(ArrayList<Integer> list)
	{
		ArrayList<Integer> newList = new ArrayList<Integer>();
		for (int pix: list){
			int[] currentNeighbors = DefaultPixelMapping.getNeighborsFromChid(pix);
			for (int nPix:currentNeighbors){
				if(nPix != -1    && photonCharge[nPix] > neighborPixelThreshold && !newList.contains(nPix)){
					newList.add(nPix);
				}
			}
		}
		return newList;
	}

	private double calculateMedian(double[] showerArrivals)
	{
		double median = 0.0;
		Arrays.sort(showerArrivals);
		int length = showerArrivals.length;
		if (showerArrivals.length%2 == 1 ){
			median =  showerArrivals[(length-1)/2];
		} else {
			median = 0.5*(  showerArrivals[(length)/2] + showerArrivals[(length)/2 - 1] );
		}
		return median;
	}
	
	/*
	 * Getter and Setter
	 */


	public boolean isShowDifferentCleaningSets() {
		return showDifferentCleaningSets;
	}

	public void setShowDifferentCleaningSets(boolean showDifferentCleaningSets) {
		this.showDifferentCleaningSets = showDifferentCleaningSets;
	}

	
	public double getCorePixelThreshold() {
		return corePixelThreshold;
	}
	public void setCorePixelThreshold(float corePixelThreshold) {
		this.corePixelThreshold = corePixelThreshold;
	}

	public double getNeighborPixelThreshold() {
		return neighborPixelThreshold;
	}
	
	public void setNeighborPixelThreshold(float neighborPixelThreshold) {
		this.neighborPixelThreshold = neighborPixelThreshold;
	}

	public int getMinNumberOfPixel() {
		return minNumberOfPixel;
	}
	
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

	public String[] getStarPositionKeys() {
		return starPositionKeys;
	}

	public void setStarPositionKeys(String[] starPositionKeys) {
		this.starPositionKeys = starPositionKeys;
	}

	public double getStarRadiusInCamera() {
		return starRadiusInCamera;
	}

	public void setStarRadiusInCamera(double starRadiusInCamera) {
		this.starRadiusInCamera = starRadiusInCamera;
	}
}
