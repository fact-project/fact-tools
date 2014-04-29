package fact.cleaning.snake;




import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import fact.Constants;
import fact.EventUtils;
import fact.cleaning.CoreNeighborClean;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.cleaning.snake.ImageForce;
import fact.cleaning.snake.StdForce;





public class SnakeComplete extends Snake implements StatefulProcessor
{
	private static Logger log = LoggerFactory.getLogger(CoreNeighborClean.class);	
	
	private String drawSnake = null;
	
	private String pixelDataName = null;	
	private String showerCenterX = null;
	private String showerCenterY = null;
	private String mean = null;

	private String SnakeOutX = null;
	private String SnakeOutY = null;	
	
	private double centerX[] = null;
	private double centerY[] = null;
	
	private double[] photonCharge = null;
	final int NIteration = 500;
	
	//////////////////////////////////////////////////////////////////////////////
	
	
	//////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void init(ProcessContext context) throws Exception 
	{
		this.initMatrix();
	}
	
	@Override
	public Data process(Data input) 
	{
		if(SnakeOutX == null || SnakeOutY == null)
		{
			SnakeOutX = "snake_X";
			SnakeOutY = "snake_Y";
		}
		
		try
		{			
			EventUtils.mapContainsKeys(getClass(), input, pixelDataName, mean);
			photonCharge = (double[]) input.get(pixelDataName);
			if(photonCharge == null)
			{
				log.error("No weights found in event. Aborting.");
				throw new RuntimeException("No weights found in event. Aborting.");
			}
			
			if(showerCenterX != null && showerCenterY != null)
			{
				centerX = (double[]) input.get(showerCenterX);
				centerY = (double[]) input.get(showerCenterY);
			}
			else
			{
				throw new RuntimeException("No center parameter set!");
			}
			
				
		} 
		catch(ClassCastException e)
		{
			log.error("CastError");
		}	
		
		
		
		double[][][] tmpSaveX = null;
		double[][][] tmpSaveY = null;
		
		int numberOfFrames = photonCharge.length / Constants.NUMBEROFPIXEL;
		if(drawSnake != null)
		{
			//Aufbau: [FRAME][ITERATION][PUNKT]
			tmpSaveX = new double[numberOfFrames][][];
			tmpSaveY = new double[numberOfFrames][][];
		}

		double[][] snakeXBuffer = new double[numberOfFrames][];
		double[][] snakeYBuffer= new double[numberOfFrames][];
		
		
		for(int frame = 0; frame < numberOfFrames; frame++)
		{
			
			this.initStartPos(centerX[frame], centerY[frame], 15.0, 6);			
			
			
			double[] data = new double[Constants.NUMBEROFPIXEL];
			for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
			{
				data[i + Constants.NUMBEROFPIXEL*frame] = photonCharge[i];
			}
			
			ImageForce force = new StdForce(data, (float) centerX[frame], (float) centerY[frame]);	
			force.setMedian((Double) input.get(mean));
		
			
			if(drawSnake != null)
			{
				double[][] xBuf = new double[NIteration][];
				double[][] yBuf = new double[NIteration][];
				
				for(int i=0; i<NIteration; i++)
				{			
					step(force);
					xBuf[i] = this.getSnakeX();
					yBuf[i] = this.getSnakeY();
				}									
								
				tmpSaveX[frame] = xBuf;
				tmpSaveY[frame] = yBuf;					
			}
			else
			{				
				for(int i=0; i<NIteration; i++)
				{			
					step(force);
				}					
			}				
			
			snakeXBuffer[frame] = this.getSnakeX();
			snakeYBuffer[frame] = this.getSnakeY();			
		}
		
		if(drawSnake != null)
		{		
			input.put(Constants.KEY_SNAKE_VIEWER_X, tmpSaveX);
			input.put(Constants.KEY_SNAKE_VIEWER_Y, tmpSaveY);				
		}		
		
		input.put(SnakeOutX, snakeXBuffer);
		input.put(SnakeOutY, snakeYBuffer);
			
		return input;
	}

	@Override
	public void finish() throws Exception 
	{
		// TODO Auto-generated method stub	
	}	

	@Override
	public void resetState() throws Exception 
	{
	// TODO Auto-generated method stub	
	}
	
	///////////////////////////////////////////////////////////////////////////////////

	public String getPixelDataName() 
	{
		return pixelDataName;
	}

	public void setPixelDataName(String pixelDataName) 
	{
		this.pixelDataName = pixelDataName;
	}	
	
	public String getMean() {
		return mean;
	}

	public void setMean(String mean) {
		this.mean = mean;
	}

	public String getShowerCenterX() {
		return showerCenterX;
	}

	public void setShowerCenterX(String showerCenterX) {
		this.showerCenterX = showerCenterX;
	}

	public String getShowerCenterY() {
		return showerCenterY;
	}

	public void setShowerCenterY(String showerCenterY) {
		this.showerCenterY = showerCenterY;
	}
	
}
