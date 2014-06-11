package fact.cleaning.snake;



import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import fact.Constants;
import fact.EventUtils;
import fact.cleaning.CoreNeighborClean;
import fact.statistics.PixelDistribution2D;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.cleaning.snake.ImageForce;
import fact.cleaning.snake.StdForce;



public class SnakeSingle extends Snake implements StatefulProcessor
{
	private static Logger log = LoggerFactory.getLogger(CoreNeighborClean.class);
	
	private String drawSnake = null;

	private String pixelDataName = null;
	private String distribution = null;	
	private String showerCenterX = null;
	private String showerCenterY = null;
	private String mean = null;
	
	
	private String SnakeOutX = null;
	private String SnakeOutY = null;
	private String numberOfVerticesOut = null;
	
	private double centerX = 0;
	private double centerY = 0;
	
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
		if(SnakeOutX == null) throw new RuntimeException("Missing parameter: SnakeOutX");
		if(SnakeOutY == null) throw new RuntimeException("Missing parameter: SnakeOutY");
		if(numberOfVerticesOut == null) throw new RuntimeException("Missing parameter: numberOfVerticesOut");
		
		
		try
		{			
			EventUtils.mapContainsKeys(getClass(), input, pixelDataName);
			photonCharge = (double[]) input.get(pixelDataName);
			
			if(photonCharge == null)
			{
				log.error("No weights found in event. Aborting.");
				throw new RuntimeException("No weights found in event. Aborting.");
			}					
			
			if(distribution != null)
			{
				PixelDistribution2D dist;
				dist = (PixelDistribution2D) input.get(distribution);
			
				centerX = dist.getCenterX();
				centerY = dist.getCenterY();
			}
			else if(showerCenterX != null && showerCenterY != null)
			{
				centerX = (Double) input.get(showerCenterX);
				centerY = (Double) input.get(showerCenterY);
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
									
		
		double[] data = new double[Constants.NUMBEROFPIXEL];
		for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
		{
			data[i] = 10 * photonCharge[i];
		}
		
		this.initStartPos(centerX, centerY, 15.0, 6);
		
		
		ImageForce force = new StdForce(data, (float) centerX, (float) centerY);	
		if(mean == null) 
			force.setMedian(0.7);
		else
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
		
			double[][][] tmpX = new double[300][][];
			double[][][] tmpY = new double[300][][];
			for(int i=0; i<300; i++)
			{
				tmpX[i] = xBuf;
				tmpY[i] = yBuf;
			}		
		
			input.put(Constants.KEY_SNAKE_VIEWER_X, tmpX);
			input.put(Constants.KEY_SNAKE_VIEWER_Y, tmpY);
			input.put(SnakeOutX, this.getSnakeX());
			input.put(SnakeOutY, this.getSnakeY());
			
		}
		else
		{				
			for(int i=0; i<NIteration; i++)
			{			
				step(force);
			}
			
			input.put(SnakeOutX, this.getSnakeX());
			input.put(SnakeOutY, this.getSnakeY());
		}
		input.put(numberOfVerticesOut, this.getNumberOfVertices());
		
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

	public String getPixelDataName(){
		return pixelDataName;
	}

	public void setPixelDataName(String pixelDataName) {
		this.pixelDataName = pixelDataName;
	}
	
	public String getDistribution() {
		return distribution;
	}

	public void setDistribution(String distribution) {
		this.distribution = distribution;
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

	public String getDrawSnake() {
		return drawSnake;
	}

	public void setDrawSnake(String drawSnake) {
		this.drawSnake = drawSnake;
	}

	
	public String getSnakeOutX() {
		return SnakeOutX;
	}

	public void setSnakeOutX(String snakeOutX) {
		SnakeOutX = snakeOutX;
	}

	public String getSnakeOutY() {
		return SnakeOutY;
	}

	public void setSnakeOutY(String snakeOutY) {
		SnakeOutY = snakeOutY;
	}

	public String getNumberOfVerticesOut() {
		return numberOfVerticesOut;
	}

	public void setNumberOfVerticesOut(String numberOfVerticesOut) {
		this.numberOfVerticesOut = numberOfVerticesOut;
	}
	
	
	
	
}
