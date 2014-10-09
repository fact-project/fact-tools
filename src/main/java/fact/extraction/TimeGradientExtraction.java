package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.net.URL;

/**
 * This should replace the classical photoncharge processor sooner or later.
 * TODO: WIP
 * @author Fabian Temme
 */
public class TimeGradientExtraction implements Processor {
	static Logger log = LoggerFactory.getLogger(TimeGradientExtraction.class);

	private String deltaKey = null;
	private String cogxKey = null;
	private String cogyKey = null;
	
	private String dataKey = null;
	
	private String timeGradientKey = null;
	
	private String outputKey = null;
	
	private int searchWindowSize;
	
	@Parameter(required = true, description = "The url to the inputfiles for the gain calibration constants",defaultValue="file:src/main/resources/defaultIntegralGains.csv")
    private URL url = null;
	
	Data integralGainData = null;
    private double[] integralGains = new double[Constants.NUMBEROFPIXEL];
	
	private int integralSize = 30;
	
	private double[] result = null;
	
	private double delta;
	private double cogx;
	private double cogy;
	private double timeGradient;
	private int roi = 0;
	
	private double[] data = null;
	
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();
	
	
	@Override
	public Data process(Data input) {
		
		Utils.mapContainsKeys(input, deltaKey, cogxKey, cogyKey, dataKey);
		
		data = (double[]) input.get(dataKey);
		
		delta = (Double) input.get(deltaKey);
		
		cogx = (Double) input.get(cogxKey);
		cogy = (Double) input.get(cogyKey);
		
		timeGradient = (Double) input.get(timeGradientKey);
		
		roi = (Integer) input.get("NROI");
		
		result = new double[Constants.NUMBEROFPIXEL];
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			double posx = pixelMap.getPixelFromId(px).getXPositionInMM();
			double posy = pixelMap.getPixelFromId(px).getYPositionInMM();
			double distance = Utils.calculateDistancePointToShowerAxis(cogx, cogy, delta, posx, posy);
			
			int predictedTime = CalculatePredictedTime(px,posx,posy);
			int leftBorder = predictedTime - searchWindowSize / 2;
			int rightBorder = predictedTime + searchWindowSize / 2;
			if (searchWindowSize%2 == 1)
			{
				rightBorder += 1;
			}
			int maxPos = BasicExtraction.CalculateMaxPosition(px, leftBorder, rightBorder, roi, data);
			int halfHeightPos = BasicExtraction.CalculatePositionHalfHeight(px, maxPos, 25, roi, data);
			result[px] = BasicExtraction.CalculateIntegral(px, halfHeightPos, integralSize, roi, data) / integralGains[px];
		}
		
		
		input.put(outputKey, result);
		
		return input;
	}




	private int CalculatePredictedTime(int px, double posx, double posy) {
		// TODO Auto-generated method stub
		return 0;
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
