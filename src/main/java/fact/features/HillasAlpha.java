package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.statistics.PixelDistribution2D;
/**
 * This feature is supposed to be the angle between the line defined by the major axis of the 2D distribution (aka the shower ellipse)... <a>
 *I have no idea.
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class HillasAlpha implements Processor {
	static Logger log = LoggerFactory.getLogger(HillasAlpha.class);
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
		double[] source = null;
		try{
			source  = (double[]) input.get(sourcePosition);
			if(source ==  null){
				log.info("No sourcePosition found in this event. Not calculating alpha");
				return input;
			}
		} catch (ClassCastException e){
			log.error("wrong types" + e.toString());
		}

		double alpha = 0.0;
	    double auxiliary_angle  = Math.atan( (source[1] - dist.getCenterY() )/(source[0] - dist.getCenterX()) );
	
	    //auxiliary_angle         = auxiliary_angle / Math.PI * 180;
	
	    alpha                  =  (dist.getAngle() - auxiliary_angle);
	
	    if (alpha > Math.PI / 2)
	    {
	        alpha              = alpha - Math.PI;
	    }
	    if (alpha < -Math.PI / 2)
	    {
	        alpha              = Math.PI + alpha;
	    }
	    input.put(outputKey, alpha);
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
