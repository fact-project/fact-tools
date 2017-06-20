package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *TwoLevelTimeMedian. Identifies showerPixel in the image array.
 *	 Cleaning in three Steps:
 * 	1) Identify all Core Pixel (Photoncharge higher than corePixelThreshold)
 * 	2) Remove all Single Core Pixel
 * 	3) Add all Neighbor Pixel, whose Photoncharge is higher than neighborPixelThreshold
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 *
 */

public class TwoLevelTimeMedian extends BasicCleaning implements Processor{
	static Logger log = LoggerFactory.getLogger(TwoLevelTimeMedian.class);

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
    		", which a pixel is alound to have after cleaning")
	private  double timeLimit;

    @Parameter(required = true, description = "Number of Pixels a patch of CorePixel must have before its Neighbours" +
            " are even considered for NeighbourCorePixel. " +
            " If Size is smaller than minSize the Pixels will be discarded.")
	private int minNumberOfPixel;
	private int npix;

    @Parameter(required = false)
    private String[] starPositionKeys = null;

    @Parameter(required = false, defaultValue="Constants.PIXEL_SIZE_MM")
	private double starRadiusInCamera = Constants.PIXEL_SIZE_MM;

    private boolean showDifferentCleaningSets = false;

    private PixelSet cleanedPixelSet;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Override
	public Data process(Data input) {
//		Utils.isKeyValid(input, arrivalTimeKey, double[].class);
//		Utils.isKeyValid(input, photonChargeKey, double[].class);
		Utils.isKeyValid(input, "NPIX", Integer.class);
		npix = (Integer) input.get("NPIX");

		ZonedDateTime timeStamp = null;
		if (input.containsKey("UnixTimeUTC") == true){
    		Utils.isKeyValid(input, "UnixTimeUTC", int[].class);
    		int[] eventTime = (int[]) input.get("UnixTimeUTC");
			timeStamp = Utils.unixTimeUTCToZonedDateTime(eventTime);
    	}
    	else {
    		// MC Files don't have a UnixTimeUTC in the data item. Here the timestamp is hardcoded to 1.1.2000
    		// => The 12 bad pixels we have from the beginning on are used.
    		timeStamp = ZonedDateTime.of(2000, 1, 1, 0, 0,0,0,ZoneOffset.of("+00:00"));
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

        // Hacky method to increase the timeLimit for larger showers (which could have a larger spread in the arrival times):
        double currentTimeThreshold = timeLimit;
        if (showerPixel.size() > 50){
        	currentTimeThreshold = timeLimit*Math.log10(showerPixel.size());
        }

        showerPixel = applyTimeMedianCleaning(showerPixel,arrivalTimes,currentTimeThreshold);
        if (showDifferentCleaningSets == true)
        {
            addLevelToDataItem(showerPixel, outputKey + "_level4", input);
        }

        showerPixel = removeSmallCluster(showerPixel,minNumberOfPixel);
        if (showDifferentCleaningSets == true)
        {
            addLevelToDataItem(showerPixel, outputKey + "_level5", input);
        }

        if (starPositionKeys != null)
        {
            PixelSet starSet = new PixelSet();
            for (String starPositionKey : starPositionKeys)
            {
                Utils.isKeyValid(input, starPositionKey, CameraCoordinate.class);
                CameraCoordinate starPosition = (CameraCoordinate) input.get(starPositionKey);

                showerPixel = removeStarIslands(showerPixel, starPosition, starSet, starRadiusInCamera, log);
                if (showDifferentCleaningSets == true)
                {
                    addLevelToDataItem(showerPixel, outputKey + "_level6", input);
                    input.put("Starset", starSet);
                }
            }
        }

        if(showerPixel.size() > 0){
            cleanedPixelSet = new PixelSet();
			for (Integer aShowerPixel : showerPixel) {
				cleanedPixelSet.addById(aShowerPixel);
			}
            input.put(outputKey, cleanedPixelSet);
        }

		return input;
	}

	/**
	 * Remove pixels with a difference in the arrivalTime to the median of the arrivalTimes of all pixels, larger than the timeLimit
	 * @param showerPixel
	 * @param arrivalTime
	 * @param timeThreshold
	 * @return
	 */
	public ArrayList<Integer> applyTimeMedianCleaning(ArrayList<Integer> showerPixel,double[] arrivalTime, double timeThreshold) {

		double[] showerArrivals = new double[showerPixel.size()];
		int i = 0;
		for (int pixel : showerPixel){
			showerArrivals[i] = arrivalTime[pixel];
			i++;
		}
		double median = calculateMedian(showerArrivals);

		ArrayList<Integer> newList= new ArrayList<>();
		for(int pixel: showerPixel){
			if(Math.abs(arrivalTime[pixel] - median) < timeThreshold){
				newList.add(pixel);
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
