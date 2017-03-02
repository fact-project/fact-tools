package fact.datacorrection;

import fact.Utils;
import fact.calibrationservice.CalibrationService;
import fact.container.PixelSet;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Service;
import stream.annotations.Parameter;

import java.time.*;

/**
 *
 * This Processor interpolates all values for a broken Pixel by the average values of its neighboring Pixels.
  * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class InterpolatePhotondata implements Processor {
    static Logger log = LoggerFactory.getLogger(InterpolatePhotondata.class);
    
    @Service(required = true, description = "The calibration service which provides the information about the bad pixels")
    CalibrationService calibService;
    @Parameter(required = true, description = "The photoncharge key to work on")
    private String photonChargeKey = null;
    @Parameter(required = true, description = "The name of the interpolated photoncharge output")
    private String photonChargeOutputKey = null;
    @Parameter(required = true, description = "The arrivalTime key to work on")
    private String arrivalTimeKey = null;
    @Parameter(required = true, description = "The name of the interpolated arrivalTime output")
    private String arrivalTimeOutputKey = null;
    @Parameter(required = false, description = "The minimum number of neighboring pixels required for interpolation", defaultValue="3")
    private int minPixelToInterpolate = 3;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();
    
    
    @Override
    public Data process(Data item) {
    	Utils.isKeyValid(item, photonChargeKey, double[].class);
		Utils.isKeyValid(item, arrivalTimeKey, double[].class);
		double[] photoncharge = (double[]) item.get(photonChargeKey);
		double[] arrivalTime = (double[]) item.get(arrivalTimeKey);

		OffsetDateTime timeStamp = null;
    	
    	if (item.containsKey("UnixTimeUTC") == true){
    		Utils.isKeyValid(item, "UnixTimeUTC", int[].class);
    		int[] eventTime = (int[]) item.get("UnixTimeUTC");

			long seconds=(long)((eventTime[0] + eventTime[1] / 1000000.) * 1000);
			timeStamp = Instant.ofEpochSecond(seconds).atOffset(ZoneOffset.UTC);
    	}
    	else {
    		// MC Files don't have a UnixTimeUTC in the data item. Here the timestamp is hardcoded to 1.1.2000
    		// => The 12 bad pixels we have from the beginning on are used.
    		timeStamp = OffsetDateTime.of(2000, 1, 1, 0, 0,0,0,ZoneOffset.of("+00:00"));
    	}
    	
    	int[] badChIds = calibService.getBadPixel(timeStamp);
    	PixelSet badPixelsSet = new PixelSet();
		for (int px: badChIds){
			badPixelsSet.addById(px);
		}
    	
		if(!photonChargeKey.equals(photonChargeOutputKey)){
			double[] newPhotonCharge = new double[photoncharge.length];
			System.arraycopy(photoncharge,0, newPhotonCharge, 0, photoncharge.length);
			photoncharge = interpolatePixelArray(newPhotonCharge, badChIds);
		} else {
			photoncharge = interpolatePixelArray(photoncharge, badChIds);
		}
		if(!arrivalTimeKey.equals(arrivalTimeOutputKey)){
			double[] newArrivalTime = new double[arrivalTime.length];
			System.arraycopy(arrivalTime,0, newArrivalTime, 0, arrivalTime.length);
			arrivalTime = interpolatePixelArray(newArrivalTime, badChIds);
		} else {
			arrivalTime = interpolatePixelArray(arrivalTime, badChIds);
		}
		item.put(photonChargeOutputKey, photoncharge);
		item.put(arrivalTimeOutputKey, arrivalTime);
		item.put("Bad pixels", badPixelsSet);
		
        return item;
    }

    private double[] interpolatePixelArray(double[] pixelArray, int[] badChIds) {
    	for (int pix: badChIds){
			FactCameraPixel[] currentNeighbors = pixelMap.getNeighboursFromID(pix);
			double avg = 0.0f;
			int numNeighbours = 0;
			for (FactCameraPixel nPix: currentNeighbors){
				if (ArrayUtils.contains(badChIds, nPix.id)){
					continue;
				}
				avg += pixelArray[nPix.id];
				numNeighbours++;
			}
			checkNumNeighbours(numNeighbours, pix);
			pixelArray[pix] = avg/numNeighbours;
		}
		return pixelArray;
	}
    
	private void checkNumNeighbours(int numNeighbours, int pixToInterpolate) {
		if (numNeighbours == 0){
			throw new RuntimeException("A pixel (chid: "+ pixToInterpolate + ") shall be interpolated, but there a no valid "
					+ "neighboring pixel to interpolate.");
		}
		if (numNeighbours < minPixelToInterpolate)
		{
			throw new RuntimeException("A pixel (chid: "+ pixToInterpolate + ") shall be interpolated, but there are only " 
					+ numNeighbours + " valid neighboring pixel to interpolate.\n" + 
					"Minimum number of pixel to interpolate is set to " + minPixelToInterpolate);
		}
	}


	public void setCalibService(CalibrationService calibService) {
		this.calibService = calibService;
	}

	public void setPhotonChargeKey(String photonChargeKey) {
		this.photonChargeKey = photonChargeKey;
	}

	public void setPhotonChargeOutputKey(String photonChargeOutputKey) {
		this.photonChargeOutputKey = photonChargeOutputKey;
	}

	public void setArrivalTimeKey(String arrivalTimeKey) {
		this.arrivalTimeKey = arrivalTimeKey;
	}

	public void setArrivalTimeOutputKey(String arrivalTimeOutputKey) {
		this.arrivalTimeOutputKey = arrivalTimeOutputKey;
	}

	public void setMinPixelToInterpolate(int minPixelToInterpolate) {
		this.minPixelToInterpolate = minPixelToInterpolate;
	}

}
