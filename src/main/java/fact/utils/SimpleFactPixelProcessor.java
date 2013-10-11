package fact.utils;

import java.util.Arrays;

import fact.Constants;
/**
 * This class provides a simple Interface for someone who wants to
 * build a processor that operates on a single pixel and returns a single value for each one.
 * @author kaibrugge
 *
 * @param <TOutput>
 */
public abstract class SimpleFactPixelProcessor extends SimpleFactEventProcessor<double[], double[]> {

	@Override
	public double[] processSeries(double[] data) {

		int roi = data.length/Constants.NUMBEROFPIXEL;
		double[] resultArray = new double[Constants.NUMBEROFPIXEL];
		
		for(int pix = 0; pix < Constants.NUMBEROFPIXEL; ++pix){
			resultArray[pix] =  processPixel(Arrays.copyOfRange(data, pix*roi, (pix+1)*roi));
		}
		return resultArray;
	}
	//---has to be overwritten----//
	public abstract double processPixel(double[] pixelData);

}
