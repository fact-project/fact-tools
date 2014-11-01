package fact.features.snake.post;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class DistFromCenter  implements Processor
{

	@Parameter(required = true, description = "Input: X Center")
	private String centerX = null;
	@Parameter(required = true, description = "Input: Y Center")
	private String centerY = null;
	
	@Parameter(required = true, description = "Input: SnakeX")
	private String snakeX = null;
	@Parameter(required = true, description = "Input: SnakeY")
	private String snakeY = null;
	
	@Parameter(required = true, description = "Output: Mittlere Abweichung")
	private String outputKey = null;
	
	
	
	
	
	@Override
	public Data process(Data input)
	{		
		Utils.isKeyValid(input, centerX, Double.class);
		Utils.isKeyValid(input, centerY, Double.class);
		
		Utils.isKeyValid(input, snakeX, double[].class);
		Utils.isKeyValid(input, snakeY, double[].class);
		
		
		double cx = (Double) input.get(centerX);
		double cy = (Double) input.get(centerY);
		
		
		
		double[] polyX = (double[]) input.get(snakeX);
		double[] polyY = (double[]) input.get(snakeY);
		
		double erg[] = new double[polyX.length];
		
		
		for(int i=0; i<polyX.length; i++)
		{
			final double dist = Math.sqrt( Math.pow(cx-polyX[i],2) + Math.pow(cy-polyY[i],2) );
			
			erg[i] = dist;
		}	
			
		
		input.put(outputKey, erg);
		
		return input;
	}





	public String getCenterX()
	{
		return centerX;
	}





	public void setCenterX(String centerX)
	{
		this.centerX = centerX;
	}





	public String getCenterY()
	{
		return centerY;
	}





	public void setCenterY(String centerY)
	{
		this.centerY = centerY;
	}





	public String getSnakeX()
	{
		return snakeX;
	}





	public void setSnakeX(String snakeX)
	{
		this.snakeX = snakeX;
	}





	public String getSnakeY()
	{
		return snakeY;
	}





	public void setSnakeY(String snakeY)
	{
		this.snakeY = snakeY;
	}





	public String getOutputKey()
	{
		return outputKey;
	}





	public void setOutputKey(String outputKey)
	{
		this.outputKey = outputKey;
	}

	
	
	
	
}

