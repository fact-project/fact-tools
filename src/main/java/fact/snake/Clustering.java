package fact.snake;

import fact.Constants;
import fact.EventUtils;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import fact.viewer.ui.DefaultPixelMapping;

public class Clustering implements StatefulProcessor
{
	private String dataString = null;	
	private String FirstThreshold = null;	
	private String SecondThreshold = null;	
	
	private String clusterOut = null;
	private String clusterSize = null;
	
	
	private double schwelle1;
	private double schwelle2;
	
	
	@Override
	public void init(ProcessContext context) throws Exception 
	{
		if(FirstThreshold == null)
		{
			throw new RuntimeException("No 1 threshold set");
		}
		if(SecondThreshold == null)
		{
			throw new RuntimeException("No 2 threshold set");
		}
		
		if(clusterOut == null)
		{
			throw new RuntimeException("No cluster key set!");
		}
		if(clusterSize == null)
		{
			throw new RuntimeException("No clusterSize key set!");
		}
	
		schwelle1 = Double.parseDouble(FirstThreshold);
		schwelle2 = Double.parseDouble(SecondThreshold);
		
	}
	
	@Override
	public void resetState() throws Exception 
	{
				
	}

	@Override
	public void finish() throws Exception 
	{
		
	}
	
	@Override
	public Data process(Data input) 
	{		
		EventUtils.mapContainsKeys(getClass(), input, dataString);	
		
		double[] pixelData = (double[]) input.get(dataString);
		int[] cluster = new int[pixelData.length];
		
		int frames = pixelData.length / Constants.NUMBEROFPIXEL;
		int label = 0;		
		
		int[] out = new int[Constants.NUMBEROFPIXEL];
		out[0] = Constants.NUMBEROFPIXEL;
		
		for(int f=0; f<frames; f++)
		{
			for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
			{							
				int size = testAndMark(cluster, pixelData, f*Constants.NUMBEROFPIXEL,
								i, label);
				
				if(size > 0)
				{
					out[label] = size;
					out[0] = out[0] - size;
					label++;
				}
			}			
		}		
		
		input.put(clusterSize, out);
		input.put(clusterOut, cluster);
			
		return input;
	}		
	
	private int testAndMark(int[] cluster, double[] data, int offset, int pixel, int label)
	{
		int pos = pixel + offset;		
		if(cluster[pos] > 0) return 0;
		
		if(data[pos] > schwelle1)
		{			
			int[] nei =  DefaultPixelMapping.getNeighborsFromChid(pixel);			
			int count = 0;
			
			for(int i=0; i<nei.length; i++)
			{
				if(nei[i] < 0) continue;
				
				if(data[offset + nei[i]] > schwelle2) count++;
			}			
			
			if(count > 4)
			{
				cluster[pos] = label;
				int size = 0;
				
				for(int i=0; i<nei.length; i++)
				{
					if(nei[i] < 0) continue;
					
					size += testAndMark(cluster, data, offset, nei[i], label);
				}
				
				return 1 + size;
			}
		}		
		return 0;
	}
	
	
	
}
