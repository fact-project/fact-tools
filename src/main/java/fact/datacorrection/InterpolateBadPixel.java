package fact.datacorrection;

import fact.Constants;
import fact.Utils;
import fact.calibrationservice.CalibrationService;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.PixelSetOverlay;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

/**
 *
 * This Processor interpolates all values for a broken Pixel by the average values of its neighboring Pixels.
  * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class InterpolateBadPixel implements StatefulProcessor {
    static Logger log = LoggerFactory.getLogger(InterpolateBadPixel.class);
    
    @Parameter(required = true, description = "The calibration service which provides the information about the bad pixels")
    CalibrationService calibService;
    
    @Parameter(required = false, description = "If true the whole time line will be interpolated",
    		defaultValue = "false")
    private boolean interpolateTimeLine = false;
    @Parameter(required = false, description = "If true the photoncharges and arrivalTimes will be interpolated",
    		defaultValue = "false")
    private boolean interpolatePhotonData = false;
    @Parameter(required = false, description = "If true a pixelSetOverlay with the bad pixels is added to the data item",
    		defaultValue = "false")
    private boolean showBadPixel = false;

    @Parameter(required = false, description = "The data key to work on")
    private String dataKey = null;
    @Parameter(required = false, description = "The name of the interpolated data output")
    private String dataOutputKey = null;
    @Parameter(required = false, description = "The photoncharge key to work on")
    private String photonChargeKey = null;
    @Parameter(required = false, description = "The name of the interpolated photoncharge output")
    private String photonChargeOutputKey = null;
    @Parameter(required = false, description = "The arrivalTime key to work on")
    private String arrivalTimeKey = null;
    @Parameter(required = false, description = "The name of the interpolated arrivalTime output")
    private String arrivalTimeOutputKey = null;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();
    
    

    private int npix = Constants.NUMBEROFPIXEL;
    
    private int minPixelToInterpolate = 3;
    

	@Override
	public void init(ProcessContext arg0) throws Exception {
		if (interpolateTimeLine == true) {
			if (dataKey == null | dataOutputKey == null)
			{
				throw new RuntimeException("Timeline shall be interpolated, but no dataKey and/or dataOutputkey specified");
			}
		}
		if (interpolatePhotonData == true) {
			if (photonChargeKey == null | photonChargeOutputKey == null | arrivalTimeKey == null | arrivalTimeOutputKey == null)
			{
				throw new RuntimeException("Photon data shall be interpolated, but there is at least one key of "
						+ "photonChargeKey, photonChargeOutputKey, arrivalTimeKey, arrivalTimeOutputkey missing");
			}
		}
	}
	
	@Override
	public void finish() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void resetState() throws Exception {
		// TODO Auto-generated method stub	
	}


    @Override
    public Data process(Data item) {
    	Utils.isKeyValid(item, "NPIX", Integer.class);
    	npix = (Integer) item.get("NPIX");
    	
    	DateTime timeStamp = null;
    	
    	if (item.containsKey("UnixTimeUTC") == true){
    		Utils.isKeyValid(item, "UnixTimeUTC", int[].class);
    		int[] eventTime = (int[]) item.get("UnixTimeUTC");
        	timeStamp = new DateTime((long)((eventTime[0]+eventTime[1]/1000000.)*1000), DateTimeZone.UTC);
    	}
    	else {
    		// MC Files don't have a UnixTimeUTC in the data item. Here the timestamp is hardcoded to 1.1.2000
    		// => The 12 bad pixels we have from the beginning on are used.
    		timeStamp = new DateTime(2000, 1, 1, 0, 0);
    	}
    	
    	
    	int[] badChIds = calibService.getBadPixel(timeStamp);
    	
    	if (interpolateTimeLine == true){
    		Utils.isKeyValid(item, dataKey, double[].class);
    		double[] data = (double[]) item.get(dataKey);
    		if(!dataKey.equals(dataOutputKey)){
    			double[] newdata = new double[data.length];
    			System.arraycopy(data,0, newdata, 0, data.length);
    			data = interpolateTimeLine(newdata, badChIds);
    		} else {
    			data = interpolateTimeLine(data, badChIds);
    		}
    		item.put(dataOutputKey, data);
    	}
    	if (interpolatePhotonData == true){
    		Utils.isKeyValid(item, photonChargeKey, double[].class);
    		Utils.isKeyValid(item, arrivalTimeKey, double[].class);
    		double[] photoncharge = (double[]) item.get(photonChargeKey);
    		double[] arrivalTime = (double[]) item.get(arrivalTimeKey);
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
    	}
    	if (showBadPixel == true){
    		PixelSetOverlay badPixelsSet = new PixelSetOverlay();
    		for (int px: badChIds){
    			badPixelsSet.addById(px);
    		}
    		item.put("Bad pixels", badPixelsSet);
    	}
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

	public double[] interpolateTimeLine(double[] data, int[] badChIds) {
        int roi = data.length / npix;

        for (int pix: badChIds) {
            FactCameraPixel[] currentNeighbors = pixelMap.getNeighboursFromID(pix);

            //iterate over all slices
            for (int slice = 0; slice < roi; slice++) {
                int pos = pix * roi + slice;
                //temp save the current value
                double avg = 0.0f;
                int numNeighbours = 0;

                for(FactCameraPixel nPix: currentNeighbors){
                		if (ArrayUtils.contains(badChIds, nPix.id)){
                			continue;
                		}
                        avg += data[nPix.id*roi + slice];
                        numNeighbours++;
                }
                checkNumNeighbours(numNeighbours, pix);
                //set value of current slice to average of surrounding pixels
                data[pos] = avg/(double)numNeighbours;
            }
        }
        return data;
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

	public void setInterpolateTimeLine(boolean interpolateTimeLine) {
		this.interpolateTimeLine = interpolateTimeLine;
	}

	public void setInterpolatePhotonData(boolean interpolatePhotonData) {
		this.interpolatePhotonData = interpolatePhotonData;
	}

	public void setShowBadPixel(boolean showBadPixel) {
		this.showBadPixel = showBadPixel;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public void setDataOutputKey(String dataOutputKey) {
		this.dataOutputKey = dataOutputKey;
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
