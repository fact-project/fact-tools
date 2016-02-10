package fact.extraction;

import fact.Utils;
import fact.container.PixelSet;
import fact.utils.RemappingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Identify pixel with a signal above a given threshold by means of a given pixels array (for example estNumPhotons),
 * hand them over as list and pixel array
 * 
 * @author jbuss, ftemme
 *
 */
public class IdentifyPixelAboveThreshold implements Processor {
	static Logger log = LoggerFactory.getLogger(IdentifyPixelAboveThreshold.class);
	
    @Parameter(required = true, description = "The key to your pixels array.")
    private String key;
    @Parameter(required = true, description = "The threshold you want to check for.")
    private Integer threshold;
    @Parameter(required = true, description = "The outputkey for the matching array (contains the value of the pixels array if it is above the threshold or 0 if it is below.")
    private String outputKey;
	
    private PixelSet pixelSet;
    
	private int npix;
	
	@Override
	public Data process(Data item) {
		Utils.isKeyValid(item, key, double[].class);
		Utils.isKeyValid(item, "NPIX", Integer.class);
        npix = (Integer) item.get("NPIX");	
		
		double[] matchArray =  new double[npix];
				
		double[] featureArray 	 = (double[]) item.get(key);
				
		pixelSet = new PixelSet();
		for(int pix = 0 ; pix < npix; pix++){
			matchArray[pix] = 0;
			if ( featureArray[pix] > threshold){
				matchArray[pix] = 1;
				pixelSet.addById(pix);
			}
		}

        item.put(outputKey+"Set", pixelSet);
		item.put(outputKey, matchArray);
		
		return item;
	}
}
