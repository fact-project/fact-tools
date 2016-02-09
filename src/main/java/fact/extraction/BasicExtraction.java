package fact.extraction;

import fact.Constants;
import fact.Utils;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.CsvStream;
import stream.io.SourceURL;

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
public class BasicExtraction implements StatefulProcessor {
	static Logger log = LoggerFactory.getLogger(BasicExtraction.class);
	@Parameter(required = false, description="key to the data array", defaultValue="raw:dataCalibrated")
	protected String dataKey = "raw:dataCalibrated";
	@Parameter(required = false, description="outputKey for the position of the max amplitudes", defaultValue="pixels:maxAmplitudePositions")
	protected String maxAmplitudePositionsKey = "pixels:maxAmplitudePositions";
	@Parameter(required = false, description="outputKey for the calculated photoncharge", defaultValue="pixels:estNumPhotons")
	protected String estNumPhotonsKey = "pixels:estNumPhotons";
	
	// TODO: Think about a reasonable default value
	@Parameter(required = true, description = "The url to the inputfiles for the gain calibration constants")
	protected SourceURL url = null;
	@Parameter(required = false, description="start slice of the search window for the max amplitude", defaultValue="35")
	protected int startSearchWindow = 35;
	@Parameter(required = false, description="range of the search window for the max amplitude", defaultValue="60")
	protected int rangeSearchWindow = 60;
	@Parameter(required = false, description="range of the search window for the half heigt position", defaultValue="25")
	protected int rangeHalfHeightWindow = 25;
	@Parameter(required = false, description="range of the integration window", defaultValue="30")
	protected int integrationWindow = 30;
	@Parameter(required = false, description="minimal slice with valid values (we want to ignore slices below this value", defaultValue="10")
	protected int validMinimalSlice = 10;
	
	protected double[] integralGains = null;
	
	private int npix = Constants.NUMBEROFPIXEL;


	@Override
	public void init(ProcessContext arg0) throws Exception {
		try {
			integralGains = loadIntegralGainFile(url,log);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void resetState() throws Exception {
		
	}
	
	@Override
	public void finish() throws Exception {
	}
	
	@Override
	public Data process(Data item) {
		Utils.mapContainsKeys(item, dataKey,"NROI");
		
		int roi = (Integer) item.get("NROI");
		npix = (Integer) item.get("NPIX");
		
		double[] data = (double[]) item.get(dataKey);
		
		int[] positions =  new int[npix];
        IntervalMarker[] markerPositions = new IntervalMarker[npix];
		double[] estNumPhotons = new double[npix];
        IntervalMarker[] markerEstNumPhotons = new IntervalMarker[npix];
        
        Utils.checkWindow(startSearchWindow, rangeSearchWindow, rangeHalfHeightWindow+validMinimalSlice, roi);
        
        for (int pix = 0; pix < npix; pix++) {
			positions[pix] = calculateMaxPosition(pix, startSearchWindow, startSearchWindow+rangeSearchWindow, roi, data);
			markerPositions[pix] = new IntervalMarker(positions[pix],positions[pix] + 1);
			
			int halfHeightPos = calculatePositionHalfHeight(pix, positions[pix],positions[pix]-rangeHalfHeightWindow, roi, data);
			
			Utils.checkWindow(halfHeightPos, integrationWindow, validMinimalSlice, roi);
			estNumPhotons[pix] = calculateIntegral(pix, halfHeightPos, integrationWindow, roi, data) / integralGains[pix];
			markerEstNumPhotons[pix] = new IntervalMarker(halfHeightPos,halfHeightPos + integrationWindow);
		}
        item.put(maxAmplitudePositionsKey, positions);
        item.put(maxAmplitudePositionsKey + "Marker", markerPositions);
        item.put(estNumPhotonsKey, estNumPhotons);
        item.put("@estNumPhotons", estNumPhotons);
        item.put(estNumPhotonsKey + "Marker", markerEstNumPhotons);
        
		return item;
	}	
	
	public int calculateMaxPosition(int px, int start, int rightBorder, int roi, double[] data) {
		int maxPos = start;
		double tempMax = -Double.MAX_VALUE;
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
	public int calculatePositionHalfHeight(int px, int maxPos, int leftBorder, int roi, double[] data) {
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
	
	public double calculateIntegral(int px, int startingPosition, int integralSize, int roi, double[] data) {
		double integral = 0;
		for (int sl = startingPosition ; sl < startingPosition + integralSize ; sl++)
		{
			int pos = px*roi + sl;
			integral += data[pos];
		}
		return integral;
	}
	
	public void setUrl(SourceURL url) {
		this.url = url;
	}

	public double[] loadIntegralGainFile(SourceURL inputUrl, Logger log) {
		double[] integralGains = new double[npix];
		Data integralGainsItem = null;
		try {
			CsvStream stream = new CsvStream(inputUrl, " ");
			stream.setHeader(false);
			stream.init();
			integralGainsItem = stream.readNext();
			
			for (int i = 0 ; i < npix ; i++){
				String key = "column:" + (i);
				integralGains[i] = (Double) integralGainsItem.get(key);
			}
			return integralGains;
			
		} catch (Exception e) {
			log.error("Failed to load integral Gain data: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

}
