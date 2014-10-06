package fact.extraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.Utils;
import fact.hexmap.ui.overlays.PixelSetOverlay;
import fact.utils.RemappingKeys;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class IdentifyPixelAboveThreshold implements Processor {
	static Logger log = LoggerFactory.getLogger(RemappingKeys.class);
	
    @Parameter(required = true, description = "The key to your data array.")
    private String key;
    @Parameter(required = true, description = "The threshold you want to check for.")
    private Integer threshold = 0;
    @Parameter(required = false)
    private String outputKey;
	
    private PixelSetOverlay pixelSet;
	

	@Override
	public Data process(Data input) {
		Utils.isKeyValid(input, key, double[].class);				
		
		double[] matchArray =  new double[Constants.NUMBEROFPIXEL];
				
		double[] featureArray 	 = (double[]) input.get(key);
		
		
		pixelSet = new PixelSetOverlay();
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
			matchArray[pix] = 0;
			if ( featureArray[pix] > threshold){
				matchArray[pix] = 1;
				pixelSet.addById(pix);
			}
		}
		

        input.put(outputKey+"Set", pixelSet);
		
		input.put(outputKey, matchArray);
		
		// TODO Auto-generated method stub
		return input;
	}


	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	public Integer getThreshold() {
		return threshold;
	}


	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}


	public String getOutputKey() {
		return outputKey;
	}


	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}


	public PixelSetOverlay getPixelSet() {
		return pixelSet;
	}


	public void setPixelSet(PixelSetOverlay pixelSet) {
		this.pixelSet = pixelSet;
	}

}
