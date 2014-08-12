package fact.features.source;

import fact.Utils;
import fact.statistics.PixelDistribution2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Quite simply the distance between the CoG of the shower and the calculated source position.
 * @author kaibrugge
 *
 */
public class HillasDistance implements Processor {
	static Logger log = LoggerFactory.getLogger(HillasDistance.class);
    @Parameter(required = true)
	private String distribution;
    @Parameter(required = true)
	private String sourcePosition;
    @Parameter(required = true)
	private String outputKey;
	/**
	 * @return input. The original DataItem with a double named {@code outputKey}. Will return null one inputKey was invalid 
	 */
	@Override
	public Data process(Data input) {
		if(!input.containsKey(distribution)){
			log.info("No shower in evernt. Not calculating distance");
			return input;
		}
		Utils.mapContainsKeys( input, distribution, sourcePosition);

		PixelDistribution2D dist = (PixelDistribution2D) input.get(distribution);
		double[] source  = (double[]) input.get(sourcePosition);

		double x = source[0];
		double y = source[1];

		input.put(outputKey, 
				Math.sqrt( (dist.getCenterY() - y) * (dist.getCenterY() - y)
						+ (dist.getCenterX() - x) * (dist.getCenterX() - x) )
				);
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
