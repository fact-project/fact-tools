package corsika;

import stream.Data;
import stream.Processor;

public class decider implements Processor
{

	@Override
	public Data process(Data input)
	{
		double a = (int) input.get("numPixelInShower");
		
		if(a > 1)
			input.put("survivedCleaning", 1);
		else
			input.put("survivedCleaning", 0);
		
		return input;
	}

}
