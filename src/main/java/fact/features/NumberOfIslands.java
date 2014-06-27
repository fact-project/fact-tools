package fact.features;

import fact.Utils;
import fact.utils.SimpleFactEventProcessor;
/**
 * If key refers to an int[] of showerpixel. this will calculate the number of islands
 * @author kaibrugge
 *
 */
public class NumberOfIslands extends SimpleFactEventProcessor<int[], Integer> {

	
	@Override
	public Integer processSeries(int[] data) {
		return Utils.breadthFirstSearch(Utils.arrayToList(data)).size();
	}
	
	

}
