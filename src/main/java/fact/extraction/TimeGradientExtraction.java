package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.net.URL;

/**
 * This processor takes the calculated time gradient along the longitudinal axis of a shower, calculated by the standard preprocessing.
 * Using this time gradient it predicts a time for the cherenkov pulse in the data array, individual for each pixel, depending on the 
 * longitudinal coordinate of the pixel.
 * Around this predicted time, the standard algorithm for calculating the photoncharge (CalculateMaxPosition(),CalculatePositionHalfHeight(),
 * CalculateIntegral()) are applied to calculate a new photoncharge.
 * @author Fabian Temme
 */
public class TimeGradientExtraction extends BasicExtraction implements Processor {
	static Logger log = LoggerFactory.getLogger(TimeGradientExtraction.class);

	@Parameter(required=true, description="key to the delta angle of the shower")
	private String deltaKey = null;
	@Parameter(required=true, description="key to the xvalue of the cog of the shower")
	private String cogxKey = null;
	@Parameter(required=true, description="key to the yvalue of the cog of the shower")
	private String cogyKey = null;
	@Parameter(required=true, description="key to the data array")
	private String dataKey = null;
	@Parameter(required=true, description="key to the timegradient slopes")
	private String timeGradientSlopeKey = null;
	@Parameter(required=true, description="key to the timegradient intercepts")
	private String timeGradientInterceptKey = null;
	@Parameter(required=true, description="outputKey for the calculated photoncharge")
	private String outputKeyPhotonCharge = null;
	@Parameter(required = true, description="outputKey for the calculated max amplitude positions")
	private String outputKeyMaxAmplitudePos = null;
	@Parameter(required = true, description = "The url to the inputfiles for the gain calibration constants",defaultValue="file:src/main/resources/defaultIntegralGains.csv")
    private URL url = null;
	
	@Parameter(required=false, description="size of the search window for the max amplitude position",defaultValue = "40")
	private int searchWindowSize = 40;
	@Parameter(required=false, description="size of the integration window",defaultValue = "30")
	private int integralSize = 30;
	@Parameter(required=false, description="size of the search window for the position of the half maximum value",defaultValue = "25")
	private int halfMaxSearchWindowSize = 25;

	Data integralGainData = null;
    private double[] integralGains = new double[Constants.NUMBEROFPIXEL];
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();
	
	
	@Override
	public Data process(Data input) {
		
		Utils.mapContainsKeys(input, deltaKey, cogxKey, cogyKey, dataKey, timeGradientSlopeKey, timeGradientInterceptKey);
		
		double[] data = (double[]) input.get(dataKey);
		
		double delta = (Double) input.get(deltaKey);
		
		double cogx = (Double) input.get(cogxKey);
		double cogy = (Double) input.get(cogyKey);
		
		double slope = (Double) input.get(timeGradientSlopeKey);
		double intercept = (Double) input.get(timeGradientInterceptKey);
		
		
		int roi = (Integer) input.get("NROI");
		
		int[] maxAmplitudePositions = new int[Constants.NUMBEROFPIXEL];
		double[] photoncharge = new double[Constants.NUMBEROFPIXEL];
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			double x = pixelMap.getPixelFromId(px).getXPositionInMM();
			double y = pixelMap.getPixelFromId(px).getYPositionInMM();
			double[] ellipseCoord = Utils.transformToEllipseCoordinates(x, y, cogx, cogy, delta);
			
			double predictedTime = slope*ellipseCoord[0] + intercept;
			int predictedSlice = (int) Math.round(predictedTime);
			
			int leftBorder = predictedSlice - searchWindowSize / 2;
			int rightBorder = predictedSlice + searchWindowSize / 2;
			if (searchWindowSize%2 == 1)
			{
				rightBorder += 1;
			}
			maxAmplitudePositions[px] = CalculateMaxPosition(px, leftBorder, rightBorder, roi, data);
			int halfHeightPos = CalculatePositionHalfHeight(px, maxAmplitudePositions[px], halfMaxSearchWindowSize, roi, data);
			photoncharge[px] = CalculateIntegral(px, halfHeightPos, integralSize, roi, data) / integralGains[px];
		}
		
		input.put(outputKeyPhotonCharge, photoncharge);
		input.put(outputKeyMaxAmplitudePos, maxAmplitudePositions);
		
		return input;
	}
		
	public String getDeltaKey() {
		return deltaKey;
	}

	public void setDeltaKey(String deltaKey) {
		this.deltaKey = deltaKey;
	}

	public String getCogxKey() {
		return cogxKey;
	}

	public void setCogxKey(String cogxKey) {
		this.cogxKey = cogxKey;
	}

	public String getCogyKey() {
		return cogyKey;
	}

	public void setCogyKey(String cogyKey) {
		this.cogyKey = cogyKey;
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getTimeGradientSlopeKey() {
		return timeGradientSlopeKey;
	}

	public void setTimeGradientSlopeKey(String timeGradientSlopeKey) {
		this.timeGradientSlopeKey = timeGradientSlopeKey;
	}

	public String getTimeGradientInterceptKey() {
		return timeGradientInterceptKey;
	}

	public void setTimeGradientInterceptKey(String timeGradientInterceptKey) {
		this.timeGradientInterceptKey = timeGradientInterceptKey;
	}

	public String getOutputKeyPhotonCharge() {
		return outputKeyPhotonCharge;
	}

	public void setOutputKeyPhotonCharge(String outputKeyPhotonCharge) {
		this.outputKeyPhotonCharge = outputKeyPhotonCharge;
	}

	public String getOutputKeyMaxAmplitudePos() {
		return outputKeyMaxAmplitudePos;
	}

	public void setOutputKeyMaxAmplitudePos(String outputKeyMaxAmplitudePos) {
		this.outputKeyMaxAmplitudePos = outputKeyMaxAmplitudePos;
	}

	public int getSearchWindowSize() {
		return searchWindowSize;
	}

	public void setSearchWindowSize(int searchWindowSize) {
		this.searchWindowSize = searchWindowSize;
	}

	public int getIntegralSize() {
		return integralSize;
	}

	public void setIntegralSize(int integralSize) {
		this.integralSize = integralSize;
	}

	public int getHalfMaxSearchWindowSize() {
		return halfMaxSearchWindowSize;
	}

	public void setHalfMaxSearchWindowSize(int halfMaxSearchWindowSize) {
		this.halfMaxSearchWindowSize = halfMaxSearchWindowSize;
	}

	public void setUrl(URL url) {
		try {
			integralGains = loadIntegralGainFile(new SourceURL(url),log);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		this.url = url;
	}


	public URL getUrl() {
		return url;
	}
		

}
