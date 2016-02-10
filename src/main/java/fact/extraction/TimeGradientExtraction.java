package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactPixelMapping;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;

/**
 * This processor takes the calculated time gradient along the longitudinal axis of a shower, calculated by the standard preprocessing.
 * Using this time gradient it predicts a time for the cherenkov pulse in each time series, individual for each pixel, depending on the 
 * longitudinal coordinate of the pixel.
 * Around this predicted time, the standard algorithm for calculating the estNumPhotons (CalculateMaxPosition(),CalculatePositionHalfHeight(),
 * CalculateIntegral()) are applied to calculate a new estNumPhotons.
 * @author Fabian Temme
 */
public class TimeGradientExtraction extends BasicExtraction {
	static Logger log = LoggerFactory.getLogger(TimeGradientExtraction.class);

	@Parameter(required=false, description="key to the delta angle of the shower", defaultValue="shower:ellipse:delta")
	private String deltaKey = "shower:ellipse:delta";
	@Parameter(required=false, description="key to the xvalue of the cog of the shower", defaultValue="shower:cog:x")
	private String cogxKey = "shower:cog:x";
	@Parameter(required=false, description="key to the yvalue of the cog of the shower", defaultValue="shower:cog:y")
	private String cogyKey = "shower:cog:y";
	@Parameter(required=false, description="key to the timegradient slope on the longitudinal-axis", defaultValue="shower:timeGradient:slope:long")
	private String timeGradientSlopeLongKey = "shower:timeGradient:slope:long";
	@Parameter(required=false, description="key to the timegradient intercept on the longitudinal-axis", defaultValue="shower:timeGradient:intercept:long")
	private String timeGradientInterceptLongKey = "shower:timeGradient:intercept:long";
	
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();
	
	@Override
	public Data process(Data input) {
		
		Utils.mapContainsKeys(input, deltaKey, cogxKey, cogyKey, dataKey,
				timeGradientSlopeLongKey, timeGradientInterceptLongKey);
		
		double[] data = (double[]) input.get(dataKey);
		double delta = (Double) input.get(deltaKey);
		double cogx = (Double) input.get(cogxKey);
		double cogy = (Double) input.get(cogyKey);
		double slopeLong = (Double) input.get(timeGradientSlopeLongKey);
		double interceptLong = (Double) input.get(timeGradientInterceptLongKey);
		int roi = (Integer) input.get("NROI");
		
		int[] positions = new int[Constants.NUMBEROFPIXEL];
		IntervalMarker[] markerPositions = new IntervalMarker[Constants.NUMBEROFPIXEL];
		double[] estNumPhotons = new double[Constants.NUMBEROFPIXEL];
		IntervalMarker[] markerEstNumPhotons = new IntervalMarker[Constants.NUMBEROFPIXEL];
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			double x = pixelMap.getPixelFromId(px).getXPositionInMM();
			double y = pixelMap.getPixelFromId(px).getYPositionInMM();
			double[] ellipseCoord = Utils.transformToEllipseCoordinates(x, y, cogx, cogy, delta);
			
			double predictedTime = slopeLong*ellipseCoord[0] + interceptLong;
			int predictedSlice = (int) Math.round(predictedTime);
						
			int leftBorder = predictedSlice - rangeSearchWindow / 2;
			int[] window = Utils.getValidWindow(leftBorder, rangeSearchWindow, rangeHalfHeightWindow+validMinimalSlice, 210);
			
			positions[px] = calculateMaxPosition(px, window[0], window[1], roi, data);
			markerPositions[px] = new IntervalMarker(positions[px],positions[px] + 1);
			
			int halfHeightPos = calculatePositionHalfHeight(px, positions[px],positions[px]-rangeHalfHeightWindow, roi, data);
			
			Utils.checkWindow(halfHeightPos, integrationWindow, validMinimalSlice, roi);
			estNumPhotons[px] = calculateIntegral(px, halfHeightPos, integrationWindow, roi, data) / integralGains[px];
			markerEstNumPhotons[px] = new IntervalMarker(halfHeightPos,halfHeightPos + integrationWindow);
		}
		
		input.put(maxAmplitudePositionsKey, positions);
        input.put(maxAmplitudePositionsKey + "Marker", markerPositions);
        input.put(estNumPhotonsKey, estNumPhotons);
        input.put("@estNumPhotons", estNumPhotons);
        input.put(estNumPhotonsKey + "Marker", markerEstNumPhotons);
		
		return input;
	}
}
