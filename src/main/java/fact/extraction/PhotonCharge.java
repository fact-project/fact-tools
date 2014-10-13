package fact.extraction;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import fact.Constants;
import fact.Utils;

/**
 * This processor Calculates PhotonCharge by doing the following: 1. Use the
 * MaxAmplitude Processor to find the maximum Value in the slices.</br> 2. In an
 * area of [amplitudePositon-25,amplitudePosition] search for the position,
 * behind the last time where data[pos] is < 0.5 of the original
 * maxAmplitude</br> 3. Calculate the integral over 30 slices </br> 4. Divide
 * the sum by the integralGain of the corresponding pixel and save the
 * result.</br>
 * 
 * TODO: Refactor to single units for calculating each step separately Treatment
 * of edge Cases is currently very arbitrary since Pixels with these values
 * should not be considered as showerPixels anyways.
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme
 *         &lt;fabian.temme@tu-dortmund.de&gt;
 * 
 */
public class PhotonCharge extends BasicExtraction implements Processor  {
	static Logger log = LoggerFactory.getLogger(PhotonCharge.class);

	@Parameter(required = true)
	private String dataKey = null;

	@Parameter(required = true, description = "The positions around which the integral is calculated.")
	private String positions = null;

	@Parameter(description = "The url to the inputfiles for the gain calibration constants")
	private SourceURL url = null;

	@Parameter(required = true, description = "The range before the maxAmplitude where the half height is searched", defaultValue = "25")
	private int rangeSearchWindow = 25;

	@Parameter(required = true)
	private String outputKey = null;

	private double[] photonCharge = null;

	Data integralGainData = null;
	private double[] integralGains = null;

	private int alpha = 64;

	@Override
	public Data process(Data input) {

		Utils.mapContainsKeys(input, dataKey, positions);

		int[] posArray;
		double[] data;
		posArray = (int[]) input.get(positions);
		data = (double[]) input.get(dataKey);

		if (integralGains == null) {
			setUrl(new SourceURL(
					PhotonCharge.class.getResource("/defaultIntegralGains.csv")));
		}

		IntervalMarker[] m = new IntervalMarker[Constants.NUMBEROFPIXEL];
		photonCharge = new double[Constants.NUMBEROFPIXEL];
		int roi = data.length / Constants.NUMBEROFPIXEL;
		// for each pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {

			int positionOfMaximum = posArray[pix];
			int leftBorder = positionOfMaximum - rangeSearchWindow;
			if (leftBorder < 0) {
				leftBorder = 0;
			}
			
			int positionOfHalfMaximumValue = CalculatePositionHalfHeight(pix, positionOfMaximum, leftBorder, roi, data);
			
			// Calculate the integral over 30 slices and divide it by the
			// calibration Gain (stored in integralGains[pix])
			double integral = CalculateIntegral(pix, positionOfHalfMaximumValue, 30, roi, data);
			photonCharge[pix] = integral / integralGains[pix];

			m[pix] = new IntervalMarker(positionOfHalfMaximumValue,
					positionOfHalfMaximumValue + 30);
		}

		input.put(outputKey + "Marker", m);
		input.put("@photoncharge", photonCharge);
		input.put(outputKey, photonCharge);
		return input;
	}

	/* Getters and Setters */

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

	public void setUrl(SourceURL url) {
		try {
			integralGains = loadIntegralGainFile(url,log);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		this.url = url;
	}

	public SourceURL getUrl() {
		return url;
	}

}
