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
import stream.annotations.Parameter;

/**
 *
 * This Processor interpolates all values for a broken Pixel by the average values of its neighboring Pixels.
  * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class InterpolateTimeline implements Processor {
    static Logger log = LoggerFactory.getLogger(InterpolateTimeline.class);
    
    @Parameter(required = true, description = "The calibration service which provides the information about the bad pixels")
    CalibrationService calibService;
    
    @Parameter(required = false, description = "If true a pixelSetOverlay with the bad pixels is added to the data item",
    		defaultValue = "false")
    private boolean showBadPixel = false;

    @Parameter(required = true, description = "The data key to work on")
    private String dataKey = null;
    @Parameter(required = true, description = "The name of the interpolated data output")
    private String dataOutputKey = null;
    FactPixelMapping pixelMap = FactPixelMapping.getInstance();
    

    private int npix = Constants.NUMBEROFPIXEL;
    
    private int minPixelToInterpolate = 3;
    

    @Override
    public Data process(Data item) {
    	Utils.isKeyValid(item, "NPIX", Integer.class);
		Utils.isKeyValid(item, dataKey, double[].class);
    	npix = (Integer) item.get("NPIX");
		double[] data = (double[]) item.get(dataKey);
    	
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
    	
		if(!dataKey.equals(dataOutputKey)){
			double[] newdata = new double[data.length];
			System.arraycopy(data,0, newdata, 0, data.length);
			data = interpolateTimeLine(newdata, badChIds);
		} else {
			data = interpolateTimeLine(data, badChIds);
		}
		
		item.put(dataOutputKey, data);
    	if (showBadPixel == true){
    		PixelSetOverlay badPixelsSet = new PixelSetOverlay();
    		for (int px: badChIds){
    			badPixelsSet.addById(px);
    		}
    		item.put("Bad pixels", badPixelsSet);
    	}
        return item;
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

	public void setShowBadPixel(boolean showBadPixel) {
		this.showBadPixel = showBadPixel;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public void setDataOutputKey(String dataOutputKey) {
		this.dataOutputKey = dataOutputKey;
	}

	public void setMinPixelToInterpolate(int minPixelToInterpolate) {
		this.minPixelToInterpolate = minPixelToInterpolate;
	}

}
