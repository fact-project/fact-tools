package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.data.EventUtils;
import fact.statistics.PixelDistribution2D;

public class HillasWidth implements Processor {
	static Logger log = LoggerFactory.getLogger(HillasWidth.class);
	private String distribution = null;
	private String sourcePosition = null;
	private String outputKey = "alpha";
	
	@Override
	public Data process(Data input) {
		if(!EventUtils.isKeyValid(input, distribution, PixelDistribution2D.class)){
			return null;
		}
	
		PixelDistribution2D dist = (PixelDistribution2D) input.get(distribution);
		float width = (float) Math.sqrt(dist.getEigenVarianceX());
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
