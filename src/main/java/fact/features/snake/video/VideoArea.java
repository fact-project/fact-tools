package fact.features.snake.video;

import java.awt.Polygon;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class VideoArea implements Processor
{

	@Parameter(required = true, description = "Input: Area")
	private String inkeyArea = null;
	@Parameter(required = true, description = "Input: Polygon center x")
	private String inkeyCenterX = null;
	@Parameter(required = true, description = "Input: Polygon center y")
	private String inkeyCenterY = null;
	@Parameter(required = true, description = "Input: Index of Slice")
	private String inkeyIndex = null;
	
	
	@Parameter(required = true, description = "Input: Photoncharge Snake X")
	private String inkeySnakeX = null;
	@Parameter(required = true, description = "Input: Photoncharge Snake Y")
	private String inkeySnakeY = null;
	
	@Parameter(required = true, description = "Output: Maximum")
	private String outkeyMax = null;
	@Parameter(required = true, description = "Output: Slope before max")
	private String outkeySlope1 = null;
	@Parameter(required = true, description = "Output: Slope after max")
	private String outkeySlope2 = null;
	
	
	@Override
	public Data process(Data input) 
	{
		Utils.isKeyValid(input, inkeyArea, Double[].class);
		Utils.isKeyValid(input, inkeyCenterX, Double[].class);
		Utils.isKeyValid(input, inkeyCenterY, Double[].class);
		Utils.isKeyValid(input, inkeyIndex, Integer[].class);
		
		Utils.isKeyValid(input, inkeySnakeX, double[].class);
		Utils.isKeyValid(input, inkeySnakeY, double[].class);
				
		Double[] data = (Double[]) input.get(inkeyArea);
		Double[] centerX =(Double[]) input.get(inkeyCenterX);
		Double[] centerY =(Double[]) input.get(inkeyCenterY);
		Integer[] index = (Integer[]) input.get(inkeyIndex);
		
		double[] snakeX = (double[]) input.get(inkeySnakeX);
		double[] snakeY = (double[]) input.get(inkeySnakeY);
		
		Double[] dataSmooth = new Double[data.length];		
		Double[] diff		= new Double[data.length];
		
		
		
		Polygon poly = new Polygon();
		for(int i=0; i<snakeX.length; i++)
		{
			poly.addPoint((int)snakeX[i], (int)snakeY[i]);
		}
		
		
		dataSmooth[0] = 2*data[0] + data[1];
		dataSmooth[0] = dataSmooth[0] / 3.0;
		dataSmooth[1] = data[0] + 2*data[1] + data[2];
		dataSmooth[1] = dataSmooth[1] / 4.0;
		for(int i=2;i<dataSmooth.length-2; i++)
		{
			dataSmooth[i] = data[i-2] + 2*data[i-1] + 4*data[i] + 2*data[i+1] + data[i+2];
			dataSmooth[i] = dataSmooth[i] / 8.0;
		}
		dataSmooth[dataSmooth.length-2] = data[dataSmooth.length-3] + 2*data[dataSmooth.length-2] + data[dataSmooth.length-1];
		dataSmooth[dataSmooth.length-2] = dataSmooth[dataSmooth.length-2] / 4.0;
		dataSmooth[dataSmooth.length-1] = data[dataSmooth.length-2] + 2*data[dataSmooth.length-1];
		dataSmooth[dataSmooth.length-1] = dataSmooth[dataSmooth.length-1] / 3.0;
		
		
		double max = 0;
		int maxSlice = -1;
		int maxNum = -1;
		
		for(int i=1; i<dataSmooth.length-1; i++)
		{			
			if(dataSmooth[i] > max)
			{
				if(poly.contains(centerX[i], centerY[i]))
				{
					max = dataSmooth[i];
					maxSlice = index[i];
					maxNum = i;
				}
			}
		}
		
		
		diff[0] = 0.0;		
		for(int i=1; i<dataSmooth.length-1; i++)
		{
			diff[i] = dataSmooth[i+1] - dataSmooth[i-1];
		}
		diff[dataSmooth.length-1] = 0.0;
		
		
		if(maxSlice == -1)
		{
			input.put(outkeyMax, 0);		
			input.put(outkeySlope1, 0.0);
			input.put(outkeySlope2, 0.0);
			
			return input;
		}
		
		double slope1=0;
		int slopeCount1=0;
		double slope2=0;
		int slopeCount2=0;
		
		for(int i=0; i<=maxNum; i++)
		{
			slope1 += diff[i];
			slopeCount1++;
		}
		for(int i=maxNum; i<diff.length; i++)
		{
			slope2 += diff[i];
			slopeCount2++;
		}
		slope1 /= slopeCount1;
		slope2 /= slopeCount2;
		
		
		//input.put("areaSmoothed", dataSmooth);
		//input.put("diff", diff);
		
		input.put(outkeyMax, index[maxNum]);		
		input.put(outkeySlope1, slope1);
		input.put(outkeySlope2, slope2);
		
		return input;
	}


	


	public String getInkeyIndex() {
		return inkeyIndex;
	}





	public void setInkeyIndex(String inkeyIndex) {
		this.inkeyIndex = inkeyIndex;
	}





	public String getInkeyArea() {
		return inkeyArea;
	}





	public void setInkeyArea(String inkeyArea) {
		this.inkeyArea = inkeyArea;
	}





	public String getInkeyCenterX() {
		return inkeyCenterX;
	}





	public void setInkeyCenterX(String inkeyCenterX) {
		this.inkeyCenterX = inkeyCenterX;
	}





	public String getInkeyCenterY() {
		return inkeyCenterY;
	}





	public void setInkeyCenterY(String inkeyCenterY) {
		this.inkeyCenterY = inkeyCenterY;
	}





	public String getInkeySnakeX() {
		return inkeySnakeX;
	}





	public void setInkeySnakeX(String inkeySnakeX) {
		this.inkeySnakeX = inkeySnakeX;
	}





	public String getInkeySnakeY() {
		return inkeySnakeY;
	}





	public void setInkeySnakeY(String inkeySnakeY) {
		this.inkeySnakeY = inkeySnakeY;
	}





	public String getOutkeyMax() {
		return outkeyMax;
	}


	public void setOutkeyMax(String outkeyMax) {
		this.outkeyMax = outkeyMax;
	}


	public String getOutkeySlope1() {
		return outkeySlope1;
	}


	public void setOutkeySlope1(String outkeySlope1) {
		this.outkeySlope1 = outkeySlope1;
	}


	public String getOutkeySlope2() {
		return outkeySlope2;
	}


	public void setOutkeySlope2(String outkeySlope2) {
		this.outkeySlope2 = outkeySlope2;
	}

	
	
	
}
