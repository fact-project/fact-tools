/**
 * 
 */
package fact.data;

import java.io.Serializable;

import stream.Processor;
import stream.annotations.Parameter;
import stream.Data;
import fact.Constants;

/**
 * 
 * This operator does a very simple fit of an exp-function to the data in each pixel. The function is simple section-wise defined curve based on the load and unload cycles of a traditional capacity.
 * The peak postion and amplitude will be set according to the values the MaxAmplitude Processor. This is not supposed to generate a good fit. Its intention is to identify showerpixel via the StdClean Processor.
 *  
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class ExFit implements Processor {
	
	
	public ExFit() {
		
	}

	public ExFit(String[] keys) {
		this.keys=keys;
	}

	/**
     * parameter and getter setters
     */
    private String[] keys = new String[] { Constants.DEFAULT_KEY };

	
	/**
	 * @return the keys
	 */
	public String[] getKeys() {
		return keys;
	}

	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	//oragne
	private String color = "#EBA817";
	public String getColor() {
		return color;
	}
	@Parameter(required = false, description = "RGB/Hex description String for the color that will be drawn in the FactViewer ChartPanel", defaultValue = "#EBA817")
	public void setColor(String color) {
		this.color = color;
	}
	
	
	private boolean overwrite =  true;
	public boolean isOverwrite() {
		return overwrite;
	}
	
	@Parameter (required = false, description = "If true this operator will output the result as " + Constants.KEY_EXFIT +"+{current Key}. Else the result will be named " + Constants.KEY_AVERAGES)
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	
	

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data event) {

		//
		//
		//String[] keys = new String[] { "Data", "DataCalibrated" };

		for (String key : keys) {

			/**
			 * TODO: find a better way to store colors for each series
			 */
			float[] ar = processEvent(event, key);
			if (ar != null)
				if(overwrite){
					event.put(Constants.KEY_EXFIT, ar);
					event.put("@"+Constants.KEY_COLOR+"_"+Constants.KEY_EXFIT, color);
				} else {
					event.put(Constants.KEY_EXFIT+"_"+key, ar);
					event.put("@"+Constants.KEY_COLOR+"_"+Constants.KEY_EXFIT, color);
				}
			
		}
		
		return event;
	}
	
	
	public float[] processEvent(Data event, String key){
		Serializable value = null;
		if(event.containsKey(key)){
			 value = event.get(key);
		} else {
			//key doesn't exist in map
			return null;
		}
		
		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType().equals(float.class)) {
			return processSeries(value);
		} else {
			//in case value is of the wrong type
			return null;
		}
	}


	
	public  float[] processSeries(Serializable value) {
		//calc maxamplitudes and positions
		float[] data = (float[])value;
		MaxAmplitude mA = new MaxAmplitude();
		int[] positions = mA.processSeries(data);
		float[] amplitudes = mA.amplitudes;
		mA = null;
					
		
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