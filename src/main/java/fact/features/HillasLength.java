package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.EventUtils;
import fact.statistics.PixelDistribution2D;

public class HillasLength implements Processor {
	static Logger log = LoggerFactory.getLogger(HillasLength.class);
	private String distribution = null;
	private String outputKey = "length";
	
	
	@Override
	public Data process(Data input) {
		if(!input.containsKey(distribution)){
			log.info("No shower in evernt. Not calculating length");
			return input;
		}
		EventUtils.isKeyValid(getClass(), input, distribution, PixelDistribution2D.class);
	
		PixelDistribution2D dist = (PixelDistribution2D) input.get(distribution);

		double length = Math.sqrt(dist.getEigenVarianceX());
	    input.put(outputKey, length);
		
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
