package fact.extraction;

import org.slf4j.Logger;

import stream.Data;
import stream.io.CsvStream;
import stream.io.SourceURL;
import fact.Constants;

public class BasicExtraction {
	
	static public double[] loadIntegralGainFile(SourceURL inputUrl, Logger log) {
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
	
	static public int CalculateMaxPosition(int px, int leftBorder, int rightBorder, int roi, double[] data) {
		int maxPos = 0;
		double tempMax = Double.MIN_VALUE;
		
		for (int sl = leftBorder ; sl < rightBorder ; sl++)
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
	static public int CalculatePositionHalfHeight(int px, int maxPos, int leftBorder, int roi, double[] data){
		int slice = maxPos;
		double maxHalf = data[maxPos] / 2.0;
		
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
	
	static public double CalculateIntegral(int px, int startingPosition, int integralSize, int roi, double[] data) {

		double integral = 0;
		
		if (startingPosition + integralSize > roi){
			System.out.println("startingPosition + integralSize > roi : " + startingPosition + " + " + integralSize + " > " + roi);
			return 0;
		}
		
		for (int sl = startingPosition ; sl < startingPosition + integralSize ; sl++)
		{
			int pos = px*roi + sl;
			integral += data[pos];
		}
		
		return integral;
	}

}
