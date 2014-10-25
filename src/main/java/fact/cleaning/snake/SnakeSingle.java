package fact.cleaning.snake;


import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import fact.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.cleaning.snake.ImageForce;
import fact.cleaning.snake.PhotonchargeForce;
import fact.hexmap.ui.overlays.PolygonOverlay;

/**
 *	SnakeSingle
 *	Stellt den Snakeprozessor zu Verfuegung und wendet die in der Klasse Snake implementierten Funktionen auf die Daten an
 *
 *  @author Dominik Baack &lt;dominik.baack@udo.edu&gt;
 *
 */

public class SnakeSingle extends Snake implements StatefulProcessor
{
	private static Logger log = LoggerFactory.getLogger(SnakeSingle.class);
	
	@Parameter(required = false, description = "UI: Should SnakePolygon drawn in the viewer?")
	private boolean drawSnake = false;

	@Parameter(required = true, description = "Input: Name of photoncharge or comparable data")
	private String pixelDataName = null;
	
	@Parameter(required = true, description = "Input: X Coordinates from shower center ")
	private String showerCenterX = null;
	@Parameter(required = true, description = "Input: Y Coordinates from shower center ")
	private String showerCenterY = null;
	@Parameter(required = true, description = "Input: Basevalue from Imagedata")
	private String mean = null;
	
	@Parameter(required = true, description = "Output: X Coordinates Snake Polygon ")
	private String snakeOutX = null;
	@Parameter(required = true, description = "Output: Y Coordinates Snake Polygon ")
	private String snakeOutY = null;
	@Parameter(required = true, description = "Output: Number of Vertices inside the Snake Polygon ")
	private String numberOfVerticesOut = null;
	
	@Parameter(required = true, description = "Input: Which force should be used?")
	private String forceName = null;	

	private double centerX = 0;
	private double centerY = 0;
	
	private double[] pixelData = null;
	
	final int NIteration = 501;
	
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
		Utils.isKeyValid(input, pixelDataName, double[].class);
		Utils.isKeyValid(input, showerCenterX, Double.class);
		Utils.isKeyValid(input, showerCenterY, Double.class);		
			
		pixelData = (double[]) input.get(pixelDataName);
			
		if(pixelData == null)
		{
			log.error("No weights found in event. Aborting.");
			throw new RuntimeException("No weights found in event. Aborting.");
		}				
			
		centerX = (Double) input.get(showerCenterX);
		centerY = (Double) input.get(showerCenterY);		
		
		this.initStartPos(centerX, centerY, 15.0, 6);
		
		
		ImageForce force;
		if(forceName.equals("Photoncharge") || forceName.equals("photoncharge") )
		{
			force = new PhotonchargeForce(pixelData, (float) centerX, (float) centerY);	
		}
		else if(forceName.equals("Slice") || forceName.equals("slice") )
		{
			force = new SliceForce(pixelData, (float) centerX, (float) centerY);	
		}
		else
		{
			System.err.println("Error: Wrong force name (" + forceName + ")");
			throw new IllegalArgumentException("Wrong force name");
		}
			
		if(mean == null) 
			force.setMedian(0.7);
		else
			force.setMedian((Double) input.get(mean));		
	
		if(drawSnake)
		{
			double[][] xBuf = new double[NIteration][];
			double[][] yBuf = new double[NIteration][];
		
			for(int i=0; i<NIteration; i++)
			{			
				step(force);
				xBuf[i] = this.getSnakeX();
				yBuf[i] = this.getSnakeY();
			}	
			
			int count = 0;
			for(int i=0; i<NIteration; i++)
			{				
				if(i % 100 == 0) count++;
			}
			
			double[][][] tmpX = new double[count][][];
			double[][][] tmpY = new double[count][][];
			
			count = 0;
			for(int it=0; it<NIteration; it++)
			{				
				if(it % 100 == 0)
				{
					tmpX[count] = new double[300][];
					tmpY[count] = new double[300][];
					
					for(int s = 0; s<300; s++)
					{													
						tmpX[count][s] = xBuf[it];
						tmpY[count][s] = yBuf[it];						
					}
					count++;
				}
			}
			
			for(int i=0; i<count; i++)
				input.put("SnakePolygonSingle_" + i, new PolygonOverlay(tmpX[i], tmpY[i]));	
		}
		else 
		{				
			for(int i=0; i<NIteration; i++)
			{			
				step(force);
			}			
		}		
				
		input.put(snakeOutX, this.getSnakeX());
		input.put(snakeOutY, this.getSnakeY());
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

	public boolean getDrawSnake() {
		return drawSnake;
	}

	public void setDrawSnake(boolean drawSnake) {
		this.drawSnake = drawSnake;
	}

	
	public String getSnakeOutX() {
		return snakeOutX;
	}

	public void setSnakeOutX(String snakeOutX) {
		this.snakeOutX = snakeOutX;
	}

	public String getSnakeOutY() {
		return snakeOutY;
	}

	public void setSnakeOutY(String snakeOutY) {
		this.snakeOutY = snakeOutY;
	}

	public String getNumberOfVerticesOut() {
		return numberOfVerticesOut;
	}

	public void setNumberOfVerticesOut(String numberOfVerticesOut) {
		this.numberOfVerticesOut = numberOfVerticesOut;
	}
	
	public String getForceName() {
		return forceName;
	}

	public void setForceName(String forceName) {
		this.forceName = forceName;
	}	
	
}
