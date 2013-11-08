package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.EventUtils;
import fact.statistics.PixelDistribution2D;

/**
 * <PRE format="md" >
 * Hallo ich bin mardown __Fett__
 * </PRE>
 * 
 * <TEX txt="\[ F\left( x \right) = \int_{ -
* \infty }^x {\frac{1}{{\sqrt {2\pi } }}e^{ - \frac{{z^2 }}{2}} dz} \]"  >
 * 
 */
public class HillasWidth implements Processor {
	static Logger log = LoggerFactory.getLogger(HillasWidth.class);
	private String distribution = null;
	private String sourcePosition = null;
	private String outputKey = "alpha";
	
	@Override
	public Data process(Data input) {
		if(!input.containsKey(distribution)){
			log.info("No shower in evernt. Not calculating width");
			return input;
		}
		EventUtils.isKeyValid(getClass(), input, distribution, PixelDistribution2D.class);
	
		PixelDistribution2D dist = (PixelDistribution2D) input.get(distribution);
		double width = (double) Math.sqrt(dist.getEigenVarianceX());
	    input.put(outputKey, width);
		
	    return input;
	}

	
	
	public String getDistribution() {
		return distribution;
	}
	public void setDistribution(String distribution) {
		this.distribution = distribution;
	}



	public String getSourcePosition() {
		return sourcePosition;
	}

	public void setSourcePosition(String sourcePosition) {
		this.sourcePosition = sourcePosition;
	}



	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}


}
