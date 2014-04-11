package fact.snake;

import java.util.List;
import java.util.Map;

import fact.Constants;
import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;
import stream.Context;
import stream.Data;
import stream.Process;
import stream.Processor;
import stream.io.Sink;
import stream.io.Source;

public class CenterOfCluster implements Processor
{
	private String clusterOut = null;
	private String clusterSize = null;	
	private String number = null;
	
	@Override
	public Data process(Data input) 
	{
		int clN = 1;
		if(number != null)
		{
			clN = Integer.parseInt(number);
		}
		
		EventUtils.mapContainsKeys(getClass(), input, clusterOut, clusterSize);	
		
		int[] clustSize = (int[]) input.get(clusterSize);
		int[] clustMap = (int[]) input.get(clusterOut);
		
		if(clustSize[clN] == 0) 
		{
			return input;
		}
		
		double x = 0;
		double y = 0;
				
		for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
		{
			if(clustMap[i] == clN)
			{
				x += DefaultPixelMapping.getPosX(i);
				y += DefaultPixelMapping.getPosY(i);
			}
		}
		
		x = x / clustSize[clN];
		y = y / clustSize[clN];
		
		
		return input;
	}


}
