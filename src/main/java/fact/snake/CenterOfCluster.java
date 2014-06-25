package fact.snake;


import java.util.Arrays;

import fact.Constants;
import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;
import stream.Data;
import stream.Processor;


public class CenterOfCluster implements Processor
{
	private String clusterMarks = null;
	private String clusterSize = null;	
	private String number = null;
	
	private String clusterCenterX = null;
	private String clusterCenterY = null;
	
	@Override
	public Data process(Data input) 
	{		
		int clN = 1;
		if(number != null)
		{
			clN = Integer.parseInt(number);
		}
		
		EventUtils.mapContainsKeys(getClass(), input, clusterMarks, clusterSize);	
		
		int[] clustSize = (int[]) input.get(clusterSize);
		int[] clustMap = (int[]) input.get(clusterMarks);
		
		int numberOfFrames = clustSize.length / Constants.NUMBEROFPIXEL;
		
		double[] centerX = new double[numberOfFrames];
		double[] centerY = new double[numberOfFrames];	
		
		
		for(int f=0; f<numberOfFrames; f++)
		{		
			int[] tmpSize = Arrays.copyOfRange(clustSize, f*Constants.NUMBEROFPIXEL, (f+1)*Constants.NUMBEROFPIXEL);
			Arrays.sort(tmpSize);
			 
			//System.out.println("Frame:" + f + "  Size:" + tmpSize[0]);
			for(int i=0; i<tmpSize.length; i++)
			{
				if(clustSize[i] == tmpSize[clN])
				{
					clN = i;
				}
			}
			
			int offset = f * Constants.NUMBEROFPIXEL;
			if(clustSize[clN + offset] == 0) 
			{
				centerX[f] = 0;				
				centerY[f] = 0;
			}
			else
			{
				double x = 0;
				double y = 0;
						
				for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
				{
					if(clustMap[i + offset] == clN)
					{
						x += DefaultPixelMapping.getPosXinMM(i);
						y += DefaultPixelMapping.getPosYinMM(i);
					}
				}
				
				centerX[f] = x / clustSize[clN + offset];
				centerY[f] = y / clustSize[clN + offset];
			}			
		}
	
		
		input.put(clusterCenterX, centerX);
		input.put(clusterCenterY, centerY);
		
		return input;
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

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getClusterCenterX() {
		return clusterCenterX;
	}

	public void setClusterCenterX(String clusterCenterX) {
		this.clusterCenterX = clusterCenterX;
	}

	public String getClusterCenterY() {
		return clusterCenterY;
	}

	public void setClusterCenterY(String clusterCenterY) {
		this.clusterCenterY = clusterCenterY;
	}


}
