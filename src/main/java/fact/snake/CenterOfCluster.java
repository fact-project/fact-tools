package fact.snake;

import java.util.List;
import java.util.Map;

import stream.Context;
import stream.Data;
import stream.Process;
import stream.Processor;
import stream.io.Sink;
import stream.io.Source;

public class CenterOfCluster implements Processor
{
	private String cluster = null;	
	private String number = null;
	
	@Override
	public Data process(Data input) 
	{
		int clN = 1;
		if(number != null)
		{
			clN = Integer.parseInt(number);
		}
		
		if(cluster == null)
		{
			throw new RuntimeException("Key \"cluster\" not set");
		}
		
		int[] clus = (int[]) input.get(cluster);
		
		if(clus[clN] == 0) 
		{
			return input;
		}
		
		
		
		
		return input;
	}


}
