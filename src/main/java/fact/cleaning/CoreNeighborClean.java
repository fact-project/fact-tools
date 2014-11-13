package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.PixelSetOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *CoreNeighborClean. Identifies showerPixel in the image array.
 *	 Cleaning in three Steps:
 * 	1) Identify all Core Pixel (Photoncharge higher than corePixelThreshold)
 * 	2) Remove all Single Core Pixel
 * 	3) Add all Neighbor Pixel, whose Photoncharge is higher than neighborPixelThreshold
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 *
 */

public class CoreNeighborClean extends BasicCleaning implements Processor{
	static Logger log = LoggerFactory.getLogger(CoreNeighborClean.class);

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
	private  double timeThreshold;

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
			
		double[] photonCharge = (double[]) input.get(photonChargeKey);
		double[] arrivalTimes = (double[]) input.get(arrivalTimeKey);
		
		ArrayList<Integer> showerPixel= new ArrayList<>();
		
		showerPixel = addCorePixel(showerPixel, photonCharge, corePixelThreshold);
		if (showDifferentCleaningSets == true)
		{
			addLevelToDataItem(showerPixel, outputKey + "_level1", input);
		}
		
		showerPixel = removeSmallCluster(showerPixel,minNumberOfPixel);
		if (showDifferentCleaningSets == true)
		{
			addLevelToDataItem(showerPixel, outputKey + "_level2", input);
		}
		
		showerPixel = addNeighboringPixels(showerPixel, photonCharge, neighborPixelThreshold);
		if (showDifferentCleaningSets == true)
		{
			addLevelToDataItem(showerPixel, outputKey + "_level3", input);
		}

        //in case we have no showerpixels. We wont get any new ones in the steps below. And also it would crash.
        if(showerPixel.size() == 0){
            return input;
        }
        
        // Hacky method to increase the timeThreshold for larger showers (which could have a larger spread in the arrival times):
        double currentTimeThreshold = timeThreshold;
        if (showerPixel.size() > 50){
        	currentTimeThreshold = timeThreshold*Math.log10(showerPixel.size());
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
            PixelSetOverlay starSet = new PixelSetOverlay();
            for (String starPositionKey : starPositionKeys)
            {
                Utils.isKeyValid(input, starPositionKey, double[].class);
                double[] starPosition = (double[]) input.get(starPositionKey);

                showerPixel = removeStarIslands(showerPixel,starPosition,starSet,starRadiusInCamera, log);
                if (showDifferentCleaningSets == true)
                {
                    addLevelToDataItem(showerPixel, outputKey + "_level6", input);
                    input.put("Starset", starSet);
                }
            }
        }

        int[] showerPixelArray = new int[showerPixel.size()];
        if(showerPixelArray.length > 0){

            cleanedPixelSet = new PixelSetOverlay();
            for (int i = 0; i < showerPixel.size(); i++) {
                cleanedPixelSet.addById(showerPixel.get(i));
                showerPixelArray[i] =  showerPixel.get(i);
            }
            input.put(outputKey, showerPixelArray);
            input.put(outputKey+"Set", cleanedPixelSet);
        }

		return input;
	}
	
	
	
	/**
	 * Remove pixels with a difference in the arrivalTime to the median of the arrivalTimes of all pixels, larger than the timeThreshold
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
		
		ArrayList<Integer> newList= new ArrayList<Integer>();
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

	public double getTimeThreshold() {
		return timeThreshold;
	}

	public void setTimeThreshold(double timeThreshold) {
		this.timeThreshold = timeThreshold;
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
