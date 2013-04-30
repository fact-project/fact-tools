/**
 * 
 */
package fact.processors;

import fact.Constants;

/**
 * 
 * This operator does a very simple fit of an exp-function to the data in each pixel. The function is simple section-wise defined curve based on the load and unload cycles of a traditional capacity.
 * The peak postion and amplitude will be set according to the values the MaxAmplitude Processor. This is not supposed to generate a good fit. Its intention is to identify showerpixel via the StdClean Processor.
 *  
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class ExFit extends SimpleFactEventProcessor<float[], float[]> {
	
	
	@Override
	public  float[] processSeries(float[] value) {
		float[] data = (float[])value;
		int[] positions = new MaxAmplitudePosition().processSeries(data);
		float[] amplitudes = new MaxAmplitude().processSeries(data);
					
		
// Iterieren ueber alle Pixel

		float[] exF = new float[data.length];
		
		int roi = data.length / Constants.NUMBEROFPIXEL;
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			
//				//iterate over all slices
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
					
					exF[pos] = fit(positions[pix], amplitudes[pix], slice, roi);
					
			}
		}
		return exF;
	}
	
	private  float fit(int amplitudePos, float ampl, int slice, int roi) {
		float offset = 12;
		float peakOffset = 3;
		float tail =  40;
		
		if(amplitudePos - offset < 0) {
			return 0.0f;
		}
		if(slice < (amplitudePos - offset)){
			return 0.0f;
		} else if(slice >= (amplitudePos -offset) && slice < (amplitudePos-peakOffset) ){
			return (float) (ampl* (  Math.pow( Math.E ,(   (slice-(amplitudePos-offset))*(1/(offset))  )) -1 ) );
		} else if(slice >= amplitudePos-peakOffset && slice < (amplitudePos+peakOffset) ){
			return (float) (ampl - Math.pow((amplitudePos-slice),2.0));
		} else if (slice >= amplitudePos +peakOffset){
			return (float) (ampl* (  Math.pow( Math.E ,(   -(slice-amplitudePos)*(1/(tail))    ))));
		}
		return 0.0f; 
		
	}
	
}