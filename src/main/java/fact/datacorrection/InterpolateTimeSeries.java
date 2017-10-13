package fact.datacorrection;

import fact.Constants;
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
public class InterpolateTimeSeries implements Processor {
    static Logger log = LoggerFactory.getLogger(InterpolateTimeSeries.class);

    @Service(required = true, description = "The calibration service which provides the information about the bad pixels")
    CalibrationService calibService;
    @Parameter(required = true, description = "The data key to work on")
    private String dataKey = null;
    @Parameter(required = true, description = "The name of the interpolated data output")
    private String dataOutputKey = null;
    @Parameter(required = false, description = "The minimum number of neighboring pixels required for interpolation", defaultValue="3")
    private int minPixelToInterpolate = 3;

	@Parameter(required = false, description = "The key for the resulting badPixelSet.")
	private String badPixelKey = "badPixel";
	@Parameter(required = false, description = "The key to the unixTimeUTC of the Event.")
	private String unixTimeKey = "UnixTimeUTC";

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    private int npix = Constants.NUMBEROFPIXEL;

    @Override
    public Data process(Data item) {
    	Utils.isKeyValid(item, "NPIX", Integer.class);
		Utils.isKeyValid(item, dataKey, double[].class);
    	npix = (Integer) item.get("NPIX");
		double[] data = (double[]) item.get(dataKey);

		ZonedDateTime timeStamp = null;

    	if (item.containsKey(unixTimeKey) == true){
    		Utils.isKeyValid(item, unixTimeKey, int[].class);
    		int[] eventTime = (int[]) item.get(unixTimeKey);
			timeStamp = Utils.unixTimeUTCToZonedDateTime(eventTime);
    	}
    	else {
    		// MC Files don't have a UnixTimeUTC in the data item. Here the timestamp is hardcoded to 1.1.2000
    		// => The 12 bad pixels we have from the beginning on are used.
    		timeStamp = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    	}

    	int[] badChIds = calibService.getBadPixel(timeStamp);
		PixelSet badPixelsSet = new PixelSet();
		for (int px: badChIds){
			badPixelsSet.addById(px);
		}

		if(!dataKey.equals(dataOutputKey)){
			double[] newdata = new double[data.length];
			System.arraycopy(data,0, newdata, 0, data.length);
			data = interpolateTimeLine(newdata, badChIds);
		} else {
			data = interpolateTimeLine(data, badChIds);
		}

		item.put(dataOutputKey, data);
		item.put("badPixel", badPixelsSet);
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

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public void setDataOutputKey(String dataOutputKey) {
		this.dataOutputKey = dataOutputKey;
	}

	public void setMinPixelToInterpolate(int minPixelToInterpolate) {
		this.minPixelToInterpolate = minPixelToInterpolate;
	}

	public void setBadPixelKey(String badPixelKey) {
		this.badPixelKey = badPixelKey;
	}

}
