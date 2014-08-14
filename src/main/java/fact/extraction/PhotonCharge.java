
package fact.extraction;

import fact.Constants;
import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.awt.*;
import java.net.URL;

/**
 * This processor Calculates PhotonCharge by doing the following: 
 * 1. 	Use the MaxAmplitude Processor to find the maximum Value in the slices.</br>
 * 2.	In an area of [amplitudePositon-25,amplitudePosition] search for the position, behind the last time where data[pos] is < 0.5 of the original maxAmplitude</br>
 * 3.	Calculate the integral over 30 slices </br>
 * 4. 	Divide the sum by the integralGain of the corresponding pixel and save the result.</br>
 * 
 * Treatment of edge Cases is currently very arbitrary since Pixels with these values should not be considered as showerPixels anyways.
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 *
 */
public class PhotonCharge implements Processor {
	static Logger log = LoggerFactory.getLogger(PhotonCharge.class);
    
    @Parameter(required = true)
	private String dataKey = null;
    @Parameter(required = true, description = "The positions around which the integral is calculated.",defaultValue="DataCalibrated")
    private String positions = null;
    @Parameter(required = true, description = "The url to the inputfiles for the gain calibration constants",defaultValue="file:src/main/resources/defaultIntegralGains.csv")
    private URL url = null;
    @Parameter(required = true, description = "The range before the maxAmplitude where the half height is searched", defaultValue ="25")
    private int rangeSearchWindow = 25;
    @Parameter(required = true)
    private String outputKey = null;
    
	private double[] photonCharge = null;
	
    Data integralGainData = null;
    private double[] integralGains = new double[Constants.NUMBEROFPIXEL];
    
    private int alpha = 64;
	

	@Override
	public Data process(Data input) {
		
		Utils.mapContainsKeys(input, dataKey, positions);
		
		int[] posArray;
		double[] data;
		posArray = (int[]) input.get(positions);
		data = (double[]) input.get(dataKey);

		IntervalMarker[] m = new IntervalMarker[Constants.NUMBEROFPIXEL];
		photonCharge = new double[Constants.NUMBEROFPIXEL];
		int roi = data.length / Constants.NUMBEROFPIXEL;
		// for each pixel
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
			
			int pos = pix*roi;
			int positionOfMaximum = posArray[pix];
			int positionOfHalfMaximumValue = 0;
			int leftBorder = positionOfMaximum - rangeSearchWindow;
			if (leftBorder < 0)
			{
				leftBorder = 0;
			}
			// in an area of ]amplitudePositon-25,amplitudePosition] search for the position,
			// behind the last time where data[pos] is < 0.5 of the original maxAmplitude
			for (int sl = positionOfMaximum ; sl > leftBorder ; sl--)
			{
				positionOfHalfMaximumValue = sl;

				if (data[pos + sl-1] < data[pos + positionOfMaximum] / 2  && data[pos + sl] >= data[pos + positionOfMaximum] / 2)
				{
					break;
				}
			}

			// Calculate the integral over 30 slices and divide it by the calibration Gain (stored in integralGains[pix])
			float integral = 0;
			if(positionOfHalfMaximumValue + 30 < roi ){
				for (int sl = positionOfHalfMaximumValue ; sl < positionOfHalfMaximumValue + 30 ; sl++){  
					integral += data[sl + (pix*roi)];
				}
			}
			else {
				integral = 0;
			}			    
			photonCharge[pix] = integral/integralGains[pix];
			
			m[pix] = new IntervalMarker(positionOfHalfMaximumValue, positionOfHalfMaximumValue + 30);
		}

		//add color value if set
		input.put(outputKey+"Marker", m);
		input.put(outputKey, photonCharge);
		return input;
	}


	/*Getters and Setters */

	public String getDataKey() {
		return dataKey;
	}


	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}


	public void setRangeSearchWindow(int rangeSearchWindow) {
		this.rangeSearchWindow = rangeSearchWindow;
	}


	public void setPositions(String positions) {
		this.positions = positions;
	}

	
	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}


	public int getAlpha() {
		return alpha;
	}
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
	
	public void setUrl(URL url) {
		try {
			loadIntegralGainFile(new SourceURL(url));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		this.url = url;
	}


	public URL getUrl() {
		return url;
	}


	private void loadIntegralGainFile(SourceURL inputUrl) {
		try {
			CsvStream stream = new CsvStream(inputUrl, " ");
			stream.setHeader(false);
			stream.init();
			integralGainData = stream.readNext();
			
			for (int i = 0 ; i < Constants.NUMBEROFPIXEL ; i++){
				String key = "column:" + (i);
				this.integralGains[i] = (Double) integralGainData.get(key);
			}
			
		} catch (Exception e) {
			log.error("Failed to load integral Gain data: {}", e.getMessage());
			e.printStackTrace();
		}
		
	}


}
