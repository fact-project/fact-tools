package fact.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This operator simply sums up all values with the given key.
 * 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class SumKey extends SimpleFactEventProcessor<float[], Double> {
	static Logger log = LoggerFactory.getLogger(SumKey.class);
	
	
	@Override
	public Double processSeries(float[] data) {
		double sum = 0;
		for(float i : data){
			sum += i;
		}
		return sum;	
	}
	
}
