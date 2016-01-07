package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import fact.container.PixelSetOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * TwoLevelTimeNeighbor. Identifies showerPixel in the image array.
 *	 Cleaning in several Steps:
 * 	1) Identify all Core Pixel (Photoncharge higher than corePixelThreshold)
 * 	2) Remove Small Cluster (Cluster with less than minNumberOfPixel Pixel)
 * 	3) Add all Neighbor Pixel, whose Photoncharge is higher than neighborPixelThreshold
 *  4) Calculate for each Pixel the difference in arrival times to the neighboring Pixels. Remove all pixel
 *     with less than 3 neighboring pixel with a difference smaller than timeLimit
 *  5) Remove Small Cluster (Cluster with less than minNumberOfPixel Pixel)
 *  6) Remove Star Cluster (Cluster which contains only pixel around a known star position
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 *
 */

public class TwoLevelTimeNeighbor extends BasicCleaning implements Processor{
	static Logger log = LoggerFactory.getLogger(TwoLevelTimeNeighbor.class);

    @Parameter(required = true)
	private String photonChargeKey;

    @Parameter(required = true)
	private String arrivalTimeKey;

    @Parameter(required = true)
	private String outputKey;

    @Parameter(required = true, description = "The smallest PhotonCharge a Pixel must have to be " +
            "identified as a CorePixel")
	private  double corePixelThreshold;

    @Parameter(required = true, description = "The smallest PhotonCharge a Pixel must have that is adjacent to a " +
            "previously identified corePixel")
	private  double neighborPixelThreshold;

    @Parameter(required = true, description = "Maximal difference in arrival time to the median of the arrival times of the shower" + 
    		", which a pixel is aloud to have after cleaning")
	private  double timeLimit;

    @Parameter(required = true, description = "Number of Pixels a patch of CorePixel must have before its Neighbours" +
            " are even considered for NeighbourCorePixel. " +
            " If Size is smaller than minSize the Pixels will be discarded.")
	private int minNumberOfPixel;


    @Parameter(required = false)
    private String[] starPositionKeys = null;
    @Parameter(required = false, defaultValue="Constants.PIXEL_SIZE")
	private double starRadiusInCamera = Constants.PIXEL_SIZE;
    
    private boolean showDifferentCleaningSets = false;

    private PixelSetOverlay cleanedPixelSet;
	
    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Override
	public Data process(Data input) {
		Utils.isKeyValid(input, arrivalTimeKey, double[].class);
		Utils.isKeyValid(input, photonChargeKey, double[].class);
		
		DateTime timeStamp = null;
		if (input.containsKey("UnixTimeUTC") == true){
    		Utils.isKeyValid(input, "UnixTimeUTC", int[].class);
    		int[] eventTime = (int[]) input.get("UnixTimeUTC");
        	timeStamp = new DateTime((long)((eventTime[0]+eventTime[1]/1000000.)*1000), DateTimeZone.UTC);
    	}
    	else {
    		// MC Files don't have a UnixTimeUTC in the data item. Here the timestamp is hardcoded to 1.1.2000
    		// => The 12 bad pixels we have from the beginning on are used.
    		timeStamp = new DateTime(2000, 1, 1, 0, 0);
    	}

		double[] photonCharge = Utils.toDoubleArray(input.get(photonChargeKey));
		double[] arrivalTimes = Utils.toDoubleArray(input.get(arrivalTimeKey));
		
		ArrayList<Integer> showerPixel= new ArrayList<>();
		
		showerPixel = addCorePixel(showerPixel, photonCharge, corePixelThreshold, timeStamp);
		if (showDifferentCleaningSets == true)
		{
			addLevelToDataItem(showerPixel, outputKey + "_level1", input);
		}
		
		showerPixel = removeSmallCluster(showerPixel,minNumberOfPixel);
		if (showDifferentCleaningSets == true)
		{
			addLevelToDataItem(showerPixel, outputKey + "_level2", input);
		}
		
		showerPixel = addNeighboringPixels(showerPixel, photonCharge, neighborPixelThreshold, timeStamp);
		if (showDifferentCleaningSets == true)
		{
			addLevelToDataItem(showerPixel, outputKey + "_level3", input);
		}
		
		if (notUsablePixelSet != null){
			input.put("notUsablePixelSet", notUsablePixelSet);
		}

        //in case we have no showerpixels. We wont get any new ones in the steps below. And also it would crash.
        if(showerPixel.size() == 0){
            return input;
        }

        showerPixel = applyTimeNeighborCleaning(showerPixel, arrivalTimes, timeLimit, 2);
        if (showDifferentCleaningSets == true)
        {
            addLevelToDataItem(showerPixel, outputKey + "_level4", input);
        }

        showerPixel = removeSmallCluster(showerPixel,minNumberOfPixel);
        if (showDifferentCleaningSets == true)
        {
            addLevelToDataItem(showerPixel, outputKey + "_level5", input);
        }
        
        showerPixel = applyTimeNeighborCleaning(showerPixel, arrivalTimes, timeLimit, 1);
        if (showDifferentCleaningSets == true)
        {
            addLevelToDataItem(showerPixel, outputKey + "_level6", input);
        }

        if (starPositionKeys != null)
        {
            PixelSetOverlay starSet = new PixelSetOverlay();
            for (String starPositionKey : starPositionKeys)
            {
                Utils.isKeyValid(input, starPositionKey, double[].class);
                double[] starPosition = (double[]) input.get(starPositionKey);

                showerPixel = removeStarIslands(showerPixel,starPosition,starSet,starRadiusInCamera, log);
                if (showDifferentCleaningSets == true)
                {
                    addLevelToDataItem(showerPixel, outputKey + "_level7", input);
                    input.put("Starset", starSet);
                }
            }
        }

        if(showerPixel.size() > 0){

            cleanedPixelSet = new PixelSetOverlay();
            for (int i = 0; i < showerPixel.size(); i++) {
                cleanedPixelSet.addById(showerPixel.get(i));
            }
            input.put(outputKey, cleanedPixelSet);
        }

		return input;
	}	
	
	/**
	 * Remove pixels with less than minNumberOfNeighborPixel neighboring shower pixel, 
	 * which arrival time differs more than the timeThreshold from the current pixel
	 * @param showerPixel
	 * @param arrivalTime
	 * @param timeThreshold
	 * @param minNumberOfNeighborPixel
	 * @return
	 */
	public ArrayList<Integer> applyTimeNeighborCleaning(ArrayList<Integer> showerPixel,double[] arrivalTime, double timeThreshold, int minNumberOfNeighborPixel) {
		
	
		ArrayList<Integer> newList= new ArrayList<Integer>();
		for(int pixel: showerPixel){
			FactCameraPixel[] currentNeighbors = pixelMap.getNeighboursFromID(pixel);
			int counter = 0;
			double time = arrivalTime[pixel];
			for (FactCameraPixel nPix:currentNeighbors){
				if( Math.abs(arrivalTime[nPix.id]-time) < timeThreshold && showerPixel.contains(nPix.id)){
					counter++;
				}
			}
			if (counter >= minNumberOfNeighborPixel)
			{
				newList.add(pixel);
			}
		}		
		return newList;
	}
	
		
	public String getPhotonChargeKey() {
		return photonChargeKey;
	}

	public void setPhotonChargeKey(String photonChargeKey) {
		this.photonChargeKey = photonChargeKey;
	}

	public String getArrivalTimeKey() {
		return arrivalTimeKey;
	}

	public void setArrivalTimeKey(String arrivalTimeKey) {
		this.arrivalTimeKey = arrivalTimeKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public double getCorePixelThreshold() {
		return corePixelThreshold;
	}

	public void setCorePixelThreshold(double corePixelThreshold) {
		this.corePixelThreshold = corePixelThreshold;
	}

	public double getNeighborPixelThreshold() {
		return neighborPixelThreshold;
	}

	public void setNeighborPixelThreshold(double neighborPixelThreshold) {
		this.neighborPixelThreshold = neighborPixelThreshold;
	}

	

	public double getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(double timeLimit) {
		this.timeLimit = timeLimit;
	}

	public int getMinNumberOfPixel() {
		return minNumberOfPixel;
	}

	public void setMinNumberOfPixel(int minNumberOfPixel) {
		this.minNumberOfPixel = minNumberOfPixel;
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

	public boolean isShowDifferentCleaningSets() {
		return showDifferentCleaningSets;
	}

	public void setShowDifferentCleaningSets(boolean showDifferentCleaningSets) {
		this.showDifferentCleaningSets = showDifferentCleaningSets;
	}
	
	/*
	 * Getter and Setter
	 */


}
