package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.features.singlePulse.timeSeriesExtraction.TemplatePulse;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

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
public class BasicExtraction implements StatefulProcessor{
	static Logger log = LoggerFactory.getLogger(BasicExtraction.class);
	@Parameter(required = true, description="key to the data array")
	protected String dataKey = null;
	@Parameter(required = true, description="outputKey for the position of the max amplitudes")
	protected String outputKeyMaxAmplPos = null;
	@Parameter(required = true, description="outputKey for the calculated photoncharge")
	protected String outputKeyPhotonCharge = null;
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
	
	private int npix = Constants.NUMBEROFPIXEL;
	public double factSinglePePulseIntegral;
	
	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(input, dataKey,"NROI");
		
		int roi = (Integer) input.get("NROI");
		npix = (Integer) input.get("NPIX");
		
		double[] data = (double[]) input.get(dataKey);
		
		int[] positions =  new int[npix];
        IntervalMarker[] mPositions = new IntervalMarker[npix];
		double[] photonCharge = new double[npix];
        IntervalMarker[] mPhotonCharge = new IntervalMarker[npix];
        
        Utils.checkWindow(startSearchWindow, rangeSearchWindow, rangeHalfHeightWindow+validMinimalSlice, roi);
        
        for (int pix = 0; pix < npix; pix++) {
			positions[pix] = calculateMaxPosition(pix, startSearchWindow, startSearchWindow+rangeSearchWindow, roi, data);
			mPositions[pix] = new IntervalMarker(positions[pix],positions[pix] + 1);

			int halfHeightPos = calculatePositionHalfHeight(pix, positions[pix],positions[pix]-rangeHalfHeightWindow, roi, data);
			
			Utils.checkWindow(halfHeightPos, integrationWindow, validMinimalSlice, roi);
			photonCharge[pix] = calculateIntegral(pix, halfHeightPos, integrationWindow, roi, data) / factSinglePePulseIntegral;
			mPhotonCharge[pix] = new IntervalMarker(halfHeightPos,halfHeightPos + integrationWindow);
		}
        input.put(outputKeyMaxAmplPos, positions);
        input.put(outputKeyMaxAmplPos + "Marker", mPositions);
        input.put(outputKeyPhotonCharge, photonCharge);
        input.put("@photoncharge", photonCharge);
        input.put(outputKeyPhotonCharge + "Marker", mPhotonCharge);
        
		return input;
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


	public int getIntegrationWindow() {
		return integrationWindow;
	}

	public void setIntegrationWindow(int integrationWindow) {
		this.integrationWindow = integrationWindow;
	}

	public int getValidMinimalSlice() {
		return validMinimalSlice;
	}

	public void setValidMinimalSlice(int validMinimalSlice) {
		this.validMinimalSlice = validMinimalSlice;
	}

    @Override
    public void init(ProcessContext processContext) throws Exception {
        factSinglePePulseIntegral = TemplatePulse.factSinglePePulseIntegral();  // unit: SinglePulseAmplitude * slices
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
