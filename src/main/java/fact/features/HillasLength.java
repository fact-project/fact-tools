package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.statistics.PixelDistribution2D;

public class HillasLength implements Processor {
	static Logger log = LoggerFactory.getLogger(HillasLength.class);
	private String distribution = null;
	private String sourcePosition = null;
	private String outputKey = "alpha";
	
	@Override
	public Data process(Data input) {
		PixelDistribution2D dist;
		try{
			dist = (PixelDistribution2D) input.get(distribution);
			if(dist ==  null){
				log.info("No showerpixel in this event. Not calculating alpha");
				return input;
			}
		} catch (ClassCastException e){
			log.error("distribution is not of type PixelDistribution2D. Aborting");
			return null;
		}
		float width = (float) Math.sqrt(dist.getEigenVarianceY());
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
