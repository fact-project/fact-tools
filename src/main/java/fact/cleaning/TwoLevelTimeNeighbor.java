package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * TwoLevelTimeNeighbor. Identifies showerPixel in the image array.
 *	 Cleaning in several Steps:
 * 	1) Identify all Core Pixel (estNumPhotons (aka photoncharge) higher than corePixelThreshold)
 * 	2) Remove Small Cluster (Cluster with less than minNumberOfPixel Pixel)
 * 	3) Add all Neighbor Pixel, whose estNumPhotons is higher than neighborPixelThreshold
 *  4) Calculate for each Pixel the difference in arrival times to the neighboring Pixels. Remove all pixel
 *     with less than 3 neighboring pixel with a difference smaller than timeLimit
 *  5) Remove Small Cluster (Cluster with less than minNumberOfPixel Pixel)
 *  6) Remove Star Cluster (Cluster which contains only pixel around a known star position
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 *
 */

public class TwoLevelTimeNeighbor extends BasicCleaning implements Processor{
	static Logger log = LoggerFactory.getLogger(TwoLevelTimeNeighbor.class);

    @Parameter(required = false)
	private String estNumPhotonsKey = "pixels:estNumPhotons";

    @Parameter(required = false)
	private String arrivalTimeKey = "pixels:arrivalTimes";

    @Parameter(required = false)
	private String outputKey = "pixelSet";

    @Parameter(required = false, description = "The smallest PhotonCharge a Pixel must have to be " +
            "identified as a CorePixel", defaultValue = "5.5")
	private  double corePixelThreshold = 5.5;

    @Parameter(required = false, description = "The smallest PhotonCharge a Pixel must have that is adjacent to a " +
            "previously identified corePixel", defaultValue = "3.0")
	private  double neighborPixelThreshold = 3.0;

    @Parameter(required = false, description = "Maximal difference in arrival time between two pixels of a shower.", defaultValue = "10.0")
	private  double timeLimit = 10.0;

    @Parameter(required = false, description = "Number of pixels a cluster of pixels must have after each single cleaning step." +
            " If number of pixels is smaller than minSize the cluster is discarded.", defaultValue = "2")
	private int minNumberOfPixel = 2;


    @Parameter(required = false)
    private String[] starPositionKeys = null;
    @Parameter(required = false, defaultValue="Constants.PIXEL_SIZE")
	private double starRadiusInCamera = Constants.PIXEL_SIZE;
    
    private boolean showDifferentCleaningSets = false;

    private PixelSet cleanedPixelSet;
	
    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Override
	public Data process(Data item) {
		Utils.isKeyValid(item, arrivalTimeKey, double[].class);
		Utils.isKeyValid(item, estNumPhotonsKey, double[].class);
		
		DateTime timeStamp = null;
		if (item.containsKey("UnixTimeUTC") == true){
    		Utils.isKeyValid(item, "UnixTimeUTC", int[].class);
    		int[] eventTime = (int[]) item.get("UnixTimeUTC");
        	timeStamp = new DateTime((long)((eventTime[0]+eventTime[1]/1000000.)*1000), DateTimeZone.UTC);
    	}
    	else {
    		// MC Files don't have a UnixTimeUTC in the item item. Here the timestamp is hardcoded to 1.1.2000
    		// => The 12 bad pixels we have from the beginning on are used.
    		timeStamp = new DateTime(2000, 1, 1, 0, 0);
    	}

		double[] estNumPhotons = Utils.toDoubleArray(item.get(estNumPhotonsKey));
		double[] arrivalTimes = Utils.toDoubleArray(item.get(arrivalTimeKey));
		
		ArrayList<Integer> pixelSetList= new ArrayList<>();
		
		pixelSetList = addCorePixel(pixelSetList, estNumPhotons, corePixelThreshold, timeStamp);
		if (showDifferentCleaningSets == true)
		{
			addLevelToDataItem(pixelSetList, outputKey + "_level1", item);
		}
		
		pixelSetList = removeSmallCluster(pixelSetList,minNumberOfPixel);
		if (showDifferentCleaningSets == true)
		{
			addLevelToDataItem(pixelSetList, outputKey + "_level2", item);
		}
		
		pixelSetList = addNeighboringPixels(pixelSetList, estNumPhotons, neighborPixelThreshold, timeStamp);
		if (showDifferentCleaningSets == true)
		{
			addLevelToDataItem(pixelSetList, outputKey + "_level3", item);
		}
		
		if (notUsablePixelSet != null){
			item.put("notUsablePixelSet", notUsablePixelSet);
		}

        //in case we have no showerpixels. We wont get any new ones in the steps below. And also it would crash.
        if(pixelSetList.size() == 0){
            return item;
        }

        pixelSetList = applyTimeNeighborCleaning(pixelSetList, arrivalTimes, timeLimit, 2);
        if (showDifferentCleaningSets == true)
        {
            addLevelToDataItem(pixelSetList, outputKey + "_level4", item);
        }

        pixelSetList = removeSmallCluster(pixelSetList,minNumberOfPixel);
        if (showDifferentCleaningSets == true)
        {
            addLevelToDataItem(pixelSetList, outputKey + "_level5", item);
        }
        
        pixelSetList = applyTimeNeighborCleaning(pixelSetList, arrivalTimes, timeLimit, 1);
        if (showDifferentCleaningSets == true)
        {
            addLevelToDataItem(pixelSetList, outputKey + "_level6", item);
        }

        if (starPositionKeys != null)
        {
            PixelSet starSet = new PixelSet();
            for (String starPositionKey : starPositionKeys)
            {
                Utils.isKeyValid(item, starPositionKey, double[].class);
                double[] starPosition = (double[]) item.get(starPositionKey);

                pixelSetList = removeStarIslands(pixelSetList,starPosition,starSet,starRadiusInCamera, log);
                if (showDifferentCleaningSets == true)
                {
                    addLevelToDataItem(pixelSetList, outputKey + "_level7", item);
                    item.put("Starset", starSet);
                }
            }
        }

        if(pixelSetList.size() > 0){

            cleanedPixelSet = new PixelSet();
            for (int i = 0; i < pixelSetList.size(); i++) {
                cleanedPixelSet.addById(pixelSetList.get(i));
            }
            item.put(outputKey, cleanedPixelSet);
        }

		return item;
	}	
	
	/**
	 * Remove pixels with less than minNumberOfNeighborPixel neighboring shower pixel, 
	 * which arrival time differs more than the timeThreshold from the current pixel
	 * @param pixelSetList
	 * @param arrivalTime
	 * @param timeThreshold
	 * @param minNumberOfNeighborPixel
	 * @return
	 */
	public ArrayList<Integer> applyTimeNeighborCleaning(ArrayList<Integer> pixelSetList,double[] arrivalTime, double timeThreshold, int minNumberOfNeighborPixel) {
		
	
		ArrayList<Integer> newList= new ArrayList<Integer>();
		for(int pixel: pixelSetList){
			FactCameraPixel[] currentNeighbors = pixelMap.getNeighboursFromID(pixel);
			int counter = 0;
			double time = arrivalTime[pixel];
			for (FactCameraPixel nPix:currentNeighbors){
				if( Math.abs(arrivalTime[nPix.id]-time) < timeThreshold && pixelSetList.contains(nPix.id)){
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
}
