package fact.features;

import fact.data.EventUtils;
import fact.utils.SimpleFactEventProcessor;
/**
 * If key refers to an int[] of showerpixel. this will calculate the number of islands
 * @author kaibrugge
 *
 */
public class NumberOfIslands extends SimpleFactEventProcessor<int[], Integer> {

	
	@Override
	public Integer processSeries(int[] data) {
		return EventUtils.breadthFirstSearch(EventUtils.arrayToList(data)).size();
	}
	
	

}
