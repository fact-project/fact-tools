package fact.snake;

import java.util.Arrays;

import fact.Constants;
import fact.EventUtils;
import fact.mapping.ui.overlays.PixelSetOverlay;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import fact.viewer.ui.DefaultPixelMapping;

public class Clustering implements StatefulProcessor
{
	private String dataString = null;	
	private String FirstThreshold = null;	
	private String SecondThreshold = null;	
	
	private String clusterMarks = null;
	private String clusterSize = null;
	
	private String startFrame = "0";
	private String endFrame = "300";
	
	
	private double schwelle1;
	private double schwelle2;
	
	private int start = 0;
	private int end = 0;
	
	
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
		
		if(clusterMarks == null)
		{
			throw new RuntimeException("No cluster key set!");
		}
		if(clusterSize == null)
		{
			throw new RuntimeException("No clusterSize key set!");
		}
	
		schwelle1 = Double.parseDouble(FirstThreshold);
		schwelle2 = Double.parseDouble(SecondThreshold);
		
		start = Integer.parseInt(startFrame);
		end = Integer.parseInt(endFrame);		
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
		
		int frames = pixelData.length / Constants.NUMBEROFPIXEL;
		
		int[] cluster = new int[pixelData.length];		
		int[] clusLabelSize = new int[pixelData.length];			//Cluster Siz
		
		for(int f=0; f<frames; f++)
		{
			clusLabelSize[f*Constants.NUMBEROFPIXEL] = Constants.NUMBEROFPIXEL;		
		}
		
		for(int f=start; f<end; f++)
		{			
			int label = 1;	
			
			for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
			{							
				int size = testAndMark(cluster, pixelData, f*Constants.NUMBEROFPIXEL, i, label);
			
				if(size > 0)
				{					
					clusLabelSize[label + f*Constants.NUMBEROFPIXEL] = size;
					clusLabelSize[f*Constants.NUMBEROFPIXEL] = clusLabelSize[f*Constants.NUMBEROFPIXEL] - size;
					label++;
				}				
			}				
		}	
		
		for(int x = 5; x<20; x++)
		{
			PixelSetOverlay corePixelSet = new PixelSetOverlay();
	        for (int i=1440*(5*x); i<1440*(5*(x+1)); i++) 
	        {
	        	int chid = i%1440;
	        	
	        	if(cluster[i] != 0 && clusLabelSize[ cluster[i] + (i - chid)] > 2)
	        	{    	        		
	        		corePixelSet.addById(chid);
	        	}
	        }	
	        
	        input.put(clusterMarks + x +"_"+Constants.PIXELSET, corePixelSet);
		}
					
        
        
        
		input.put(clusterSize, clusLabelSize);
		input.put(clusterMarks, cluster);
		
		
			
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
			else
			{
				return 0;
			}
			
		}		
		return 0;
	}
	
	
	public String getDataString() {
		return dataString;
	}

	public void setDataString(String dataString) {
		this.dataString = dataString;
	}

	public String getFirstThreshold() {
		return FirstThreshold;
	}

	public void setFirstThreshold(String firstThreshold) {
		FirstThreshold = firstThreshold;
	}

	public String getSecondThreshold() {
		return SecondThreshold;
	}

	public void setSecondThreshold(String secondThreshold) {
		SecondThreshold = secondThreshold;
	}

	public String getClusterMarks() {
		return clusterMarks;
	}

	public void setClusterMarks(String clusterMarks) {
		this.clusterMarks = clusterMarks;
	}

	public String getClusterSize() {
		return clusterSize;
	}

	public void setClusterSize(String clusterSize) {
		this.clusterSize = clusterSize;
	}

	public String getStartFrame() {
		return startFrame;
	}

	public void setStartFrame(String startFrame) {
		this.startFrame = startFrame;
	}

	public String getEndFrame() {
		return endFrame;
	}

	public void setEndFrame(String endFrame) {
		this.endFrame = endFrame;
	}
	
	
	
}
