package fact.extraction;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.CsvStream;
import stream.io.SourceURL;
import fact.Constants;
import fact.Utils;

/**
 * This processor performs a basic extraction on the data array. It contains three steps:
 * 1. Calculates the position of the max amplitude in [startSearchWindow,startSearchWindow+rangeSearchWindow[
 * 2. Calculates the position of the half height in front of the maxAmplitudePosition
 * 3. Calculates the integral by summing up the following integrationWindow slices beginning with the half heigth position
 * The resulting photoncharge is calculated by dividing the integral by the integralGain of the pixel
 * 
 * This processor also serves as a basic class for extraction processors
 * 
 * @author Fabian Temme
 *
 */
public class BasicExtraction implements Processor {
	static Logger log = LoggerFactory.getLogger(BasicExtraction.class);
	@Parameter(required = true, description="key to the data array")
	protected String dataKey = null;
	@Parameter(required = true, description="outputKey for the position of the max amplitudes")
	protected String outputKeyMaxAmplPos = null;
	@Parameter(required = true, description="outputKey for the calculated photoncharge")
	protected String outputKeyPhotonCharge = null;
	
	@Parameter(required = false, description = "The url to the inputfiles for the gain calibration constants",defaultValue="file:src/main/resources/defaultIntegralGains.csv")
	protected SourceURL url = null;
	@Parameter(required = false, description="start slice of the search window for the max amplitude", defaultValue="35")
	protected int startSearchWindow = 35;
	@Parameter(required = false, description="range of the search window for the max amplitude", defaultValue="90")
	protected int rangeSearchWindow = 90;
	@Parameter(required = false, description="range of the search window for the half heigt position", defaultValue="25")
	protected int rangeHalfHeightWindow = 25;
	@Parameter(required = false, description="range of the integration window", defaultValue="30")
	protected int integrationWindow = 30;
	@Parameter(required = false, description="minimal slice with valid values (we want to ignore slices below this value", defaultValue="10")
	protected int validMinimalSlice = 10;
	
	protected double[] integralGains = null;
	
	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(input, dataKey,"NROI");
		
		int roi = (Integer) input.get("NROI");
		
		double[] data = (double[]) input.get(dataKey);
		
		int[] positions =  new int[Constants.NUMBEROFPIXEL];
        IntervalMarker[] mPositions = new IntervalMarker[Constants.NUMBEROFPIXEL];
		double[] photonCharge = new double[Constants.NUMBEROFPIXEL];
        IntervalMarker[] mPhotonCharge = new IntervalMarker[Constants.NUMBEROFPIXEL];
        
        checkWindow(startSearchWindow, rangeSearchWindow, rangeHalfHeightWindow+validMinimalSlice, roi);
        
        for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			positions[pix] = calculateMaxPosition(pix, startSearchWindow, startSearchWindow+rangeSearchWindow, roi, data);
			mPositions[pix] = new IntervalMarker(positions[pix],positions[pix] + 1);
			
			int halfHeightPos = calculatePositionHalfHeight(pix, positions[pix],positions[pix]-rangeHalfHeightWindow, roi, data);
			
			checkWindow(halfHeightPos, integrationWindow, validMinimalSlice, roi);
			photonCharge[pix] = calculateIntegral(pix, halfHeightPos, integrationWindow, roi, data) / integralGains[pix];
			mPhotonCharge[pix] = new IntervalMarker(halfHeightPos,halfHeightPos + integrationWindow);
		}
        input.put(outputKeyMaxAmplPos, positions);
        input.put(outputKeyMaxAmplPos + "Marker", mPositions);
        input.put(outputKeyPhotonCharge, photonCharge);
        input.put("@photoncharge", photonCharge);
        input.put(outputKeyPhotonCharge + "Marker", mPhotonCharge);
        
		return input;
	}	
	
	protected int calculateMaxPosition(int px, int start, int rightBorder, int roi, double[] data) {
		int maxPos = start;
		double tempMax = Double.MIN_VALUE;
		for (int sl = start ; sl < rightBorder ; sl++)
		{
			int pos = px * roi + sl;
			if (data[pos] > tempMax)
			{
				maxPos = sl;
				tempMax = data[pos];
			}
		}
		return maxPos;
	}

	/**
	 * In an area ]amplitudePositon-leftBorder,amplitudePosition] searches for the last position, where data[pos] is < 0.5 * 
	 * maxAmplitude. Returns the following slice.
	 * 
	 * @param px
	 * @param maxPos
	 * @param leftBorder
	 * @param roi
	 * @param data
	 * @return
	 */
	protected int calculatePositionHalfHeight(int px, int maxPos, int leftBorder, int roi, double[] data) {
		int slice = maxPos;
		double maxHalf = data[px*roi+maxPos] / 2.0;
		for (; slice > leftBorder ; slice--)
		{
			int pos = px * roi + slice;
			if (data[pos-1] < maxHalf)
			{
				break;
			}
		}
		return slice;
	}
	
	protected double calculateIntegral(int px, int startingPosition, int integralSize, int roi, double[] data) {
		double integral = 0;
		for (int sl = startingPosition ; sl < startingPosition + integralSize ; sl++)
		{
			int pos = px*roi + sl;
			integral += data[pos];
		}
		return integral;
	}
	
	protected void checkWindow(int start, int size, int validLeft, int validRight)
	{	
		if (size < 0)
		{
			throw new RuntimeException("Size for window < 0! size: "+ size);
		}
		if (start < validLeft)
		{
			String message = "start < validLeft. start/validLeft: " + start + "/" + validLeft;
			throw new RuntimeException(message);
		}
		if (start+size > validRight)
		{
			String message = "start + size > validRight. start+size/validRight: " + (start+size) + "/" + validRight;
			throw new RuntimeException(message);
		}	
	}
	
	protected int[] getValidWindow(int start, int size, int validLeft, int validRight)
	{
		if (size < 0)
		{
			throw new RuntimeException("Size for window < 0! size: "+ size);
		}
		int[] window = {start,start+size};
		if (start < validLeft)
		{
			String message = "start < validLeft. start/validLeft: " + start + "/" + validLeft;
			log.warn(message);
			window[0] = validLeft;
		}
		if (start+size > validRight)
		{
			String message = "start + size > validRight. start+size/validRight: " + (start+size) + "/" + validRight;
			log.warn(message);
			window[1] = validRight;
		}
		if (window[1] < window[0])
		{
			window[1] = window[0];
		}
		return window;
	}
	
	protected double[] loadIntegralGainFile(SourceURL inputUrl, Logger log) {
		double[] integralGains = new double[Constants.NUMBEROFPIXEL];
		Data integralGainData = null;
		try {
			CsvStream stream = new CsvStream(inputUrl, " ");
			stream.setHeader(false);
			stream.init();
			integralGainData = stream.readNext();
			
			for (int i = 0 ; i < Constants.NUMBEROFPIXEL ; i++){
				String key = "column:" + (i);
				integralGains[i] = (Double) integralGainData.get(key);
			}
			return integralGains;
			
		} catch (Exception e) {
			log.error("Failed to load integral Gain data: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getOutputKeyMaxAmplPos() {
		return outputKeyMaxAmplPos;
	}

	public void setOutputKeyMaxAmplPos(String outputKeyMaxAmplPos) {
		this.outputKeyMaxAmplPos = outputKeyMaxAmplPos;
	}

	public String getOutputKeyPhotonCharge() {
		return outputKeyPhotonCharge;
	}

	public void setOutputKeyPhotonCharge(String outputKeyPhotonCharge) {
		this.outputKeyPhotonCharge = outputKeyPhotonCharge;
	}

	public int getStartSearchWindow() {
		return startSearchWindow;
	}

	public void setStartSearchWindow(int startSearchWindow) {
		this.startSearchWindow = startSearchWindow;
	}

	public int getRangeSearchWindow() {
		return rangeSearchWindow;
	}

	public void setRangeSearchWindow(int rangeSearchWindow) {
		this.rangeSearchWindow = rangeSearchWindow;
	}

	public int getRangeHalfHeightWindow() {
		return rangeHalfHeightWindow;
	}

	public void setRangeHalfHeightWindow(int rangeHalfHeightWindow) {
		this.rangeHalfHeightWindow = rangeHalfHeightWindow;
	}

	public int getIntegralSize() {
		return integrationWindow;
	}

	public void setIntegralSize(int integralSize) {
		this.integrationWindow = integralSize;
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
