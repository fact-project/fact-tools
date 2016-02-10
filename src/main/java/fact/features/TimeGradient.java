package fact.features;

import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSet;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates the timing gradient along the longitudinal and the transversal axis of the shower.
 * Therefore the ellipse coordinates ({l,t}) are calculated and the function arrTime(l)=s_l*l + i_l 
 * and arrTime(t)=s_t*t + i_t are fitted.
 * The parameters (s_l,i_l,s_t,i_t) of the fits, their standard deviation and the sum squared errors of the fits are pushed in the
 * data item.
 * @author Fabian Temme
 *
 */
public class TimeGradient implements Processor {
	
	static Logger log = LoggerFactory.getLogger(TimeGradient.class);
	
	@Parameter(required=false, description="key to the shower pixels", defaultValue="shower")
	private String pixelSetKey = "shower";
	@Parameter(required=false, description="key to the arrival times of all pixels", defaultValue="pixels:arrivalTimes")
	private String arrivalTimesKey = "pixels:arrivalTimes";
	@Parameter(required=false, description="key to the xvalue of the cog of the shower", defaultValue="shower:cog:x")
	private String cogxKey = "shower:cog:x";
	@Parameter(required=false, description="key to the yvalue of the cog of the shower", defaultValue="shower:cog:y")
	private String cogyKey = "shower:cog:y";
	@Parameter(required=false, description="key to the delta angle of the shower", defaultValue="shower:delta")
	private String deltaKey = "shower:ellipse:delta";
	@Parameter(required=false, description="outputKey for the calculated timegradient slopes", defaultValue="shower:timeGradient:slope")
	private String outputKeySlope = "shower:timeGradient:slope";
	@Parameter(required=false, description="outputKey for the calculated timegradient intercepts", defaultValue="shower:timeGradient:intercept")
	private String outputKeyIntercept = "shower:timeGradient:intercept";
	@Parameter(required=false, description="outputKey for the sum squared errors of the linear fits", defaultValue="shower:timeGradient:sumSquaredErrors")
	private String outputKeySumSquaredErrors = "shower:timeGradient:sumSquaredErrors";
	
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	public Data process(Data item) {
		
		Utils.mapContainsKeys(item, pixelSetKey,arrivalTimesKey,cogxKey,cogyKey,deltaKey);

		PixelSet shower = (PixelSet) item.get(pixelSetKey);
		double[] arrivalTimes = (double[]) item.get(arrivalTimesKey);
		double cogx = (Double) item.get(cogxKey);
		double cogy = (Double) item.get(cogyKey);
		double delta = (Double) item.get(deltaKey);
		
		SimpleRegression regressorLong = new SimpleRegression();
		SimpleRegression regressorTrans = new SimpleRegression();
		
		for (CameraPixel px: shower.set)
		{
			double x = pixelMap.getPixelFromId(px.id).getXPositionInMM();
			double y = pixelMap.getPixelFromId(px.id).getYPositionInMM();
			double[] ellipseCoord = Utils.transformToEllipseCoordinates(x, y, cogx, cogy, delta);
			double time = arrivalTimes[px.id];
			regressorLong.addData(ellipseCoord[0], time);
			regressorTrans.addData(ellipseCoord[1], time);
		}
		
		double[] slope = {0,0};
		double[] slopeErr = {0,0};
		double[] intercept = {0,0};
		double[] interceptErr = {0,0};
		double[] sumSquaredErrors = {0,0};
		
		try
		{
			regressorLong.regress();
			slope[0] = regressorLong.getSlope();
			slopeErr[0] = regressorLong.getSlopeStdErr();
			intercept[0] = regressorLong.getIntercept();
			interceptErr[0] = regressorLong.getInterceptStdErr();
			sumSquaredErrors[0] = regressorLong.getSumSquaredErrors();
		}
		catch (NoDataException exc)
		{
			log.warn("Not enough data points to regress the longitudinal timegradient. Putting Double.NaN in data item");
			slope[0] = Double.NaN;
			slopeErr[0] = Double.NaN;
			intercept[0] = Double.NaN;
			interceptErr[0] = Double.NaN;
			sumSquaredErrors[0] = Double.NaN;
		}
		
		try
		{
			regressorTrans.regress();
			slope[1] = regressorTrans.getSlope();
			slopeErr[1] = regressorTrans.getSlopeStdErr();
			intercept[1] = regressorTrans.getIntercept();
			interceptErr[1] = regressorTrans.getInterceptStdErr();
			sumSquaredErrors[1] = regressorTrans.getSumSquaredErrors();
		}
		catch (NoDataException exc)
		{
			log.warn("Not enough data points to regress the transversal timegradient. Putting Double.NaN in data item");
			slope[1] = Double.NaN;
			slopeErr[1] = Double.NaN;
			intercept[1] = Double.NaN;
			interceptErr[1] = Double.NaN;
			sumSquaredErrors[1] = Double.NaN;
		}
		
		item.put(outputKeySlope+":long", slope[0]);
		item.put(outputKeySlope+":trans", slope[1]);
		item.put(outputKeySlope+":long:err", slopeErr[0]);
		item.put(outputKeySlope+":trans:err", slopeErr[1]);
		item.put(outputKeyIntercept+":long", intercept[0]);
		item.put(outputKeyIntercept+":trans", intercept[1]);
		item.put(outputKeyIntercept+":long:err", interceptErr[0]);
		item.put(outputKeyIntercept+":trans:err", interceptErr[1]);
		item.put(outputKeySumSquaredErrors+":long", sumSquaredErrors[0]);
		item.put(outputKeySumSquaredErrors+":trans", sumSquaredErrors[1]);
		
		return item;
	}
}
