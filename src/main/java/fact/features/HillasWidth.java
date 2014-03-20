package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.EventUtils;
import fact.statistics.PixelDistribution2D;
import stream.annotations.Parameter;

public class HillasWidth implements Processor {
	static Logger log = LoggerFactory.getLogger(HillasWidth.class);
    @Parameter(required = true)
	private String distribution;
    @Parameter(required = true)
	private String outputKey;
	
	@Override
	public Data process(Data input) {
		if(!input.containsKey(distribution)){
			log.info("No shower in evernt. Not calculating width");
			return input;
		}
		EventUtils.isKeyValid(getClass(), input, distribution, PixelDistribution2D.class);
	
		PixelDistribution2D dist = (PixelDistribution2D) input.get(distribution);
		double width =  Math.sqrt(dist.getEigenVarianceY());
	    input.put(outputKey, width);
		
	    return input;
	}

	
	
	public String getDistribution() {
		return distribution;
	}
	public void setDistribution(String distribution) {
		this.distribution = distribution;
	}

	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}


}
