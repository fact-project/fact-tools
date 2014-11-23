package fact.features.snake.post;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 *	EllipseError
 *	Absolute Abweichung der Ellipse zu einem Polygonzug
 *	Die Ellipse wird bei der Berechnung als Polygonzug interpoliert
 *	
 *  @author Dominik Baack &lt;dominik.baack@udo.edu&gt;
 *
 */
public class EllipseError implements Processor
{
	@Parameter(required = true, description = "Input: Winkel zur X-Achse")
	private String ellipseAlpha = null;
	@Parameter(required = true, description = "Input: SemiMajor")
	private String ellipseMajor = null;
	@Parameter(required = true, description = "Input: SemiMinor")
	private String ellipseMinor = null;
	@Parameter(required = true, description = "Input: X")
	private String ellipseX = null;
	@Parameter(required = true, description = "Input: Y")
	private String ellipseY = null;
	
	@Parameter(required = true, description = "Input: SnakeX")
	private String snakeX = null;
	@Parameter(required = true, description = "Input: SnakeY")
	private String snakeY = null;
	
	@Parameter(required = true, description = "Output: Abweichung von der Ellipse fuer jede Stuetzstelle")
	private String outputKey = null;
	
	@Override
	public Data process(Data input) 
	{
		Utils.isKeyValid(input, ellipseAlpha, Double.class);
		Utils.isKeyValid(input, ellipseMajor, Double.class);
		Utils.isKeyValid(input, ellipseMinor, Double.class);
		Utils.isKeyValid(input, ellipseX, Double.class);
		Utils.isKeyValid(input, ellipseY, Double.class);
		
		Utils.isKeyValid(input, snakeX, double[].class);
		Utils.isKeyValid(input, snakeY, double[].class);
		
		
		double alpha = (Double) input.get(ellipseAlpha);
		double major = (Double) input.get(ellipseMajor);
		double minor = (Double) input.get(ellipseMinor);
		double elliX = (Double) input.get(ellipseX);
		double elliY = (Double) input.get(ellipseY);
		
		
		double[] polyX = (double[]) input.get(snakeX);
		double[] polyY = (double[]) input.get(snakeY);
		
		double erg[] = new double[polyX.length];
		
		for(int i=0; i<polyX.length; i++)
		{
			double x = polyX[i] - elliX;
			double y = polyY[i] - elliY;
			
			double xNew = Math.cos(-alpha)*x - Math.sin(-alpha)*y; 
			double yNew = Math.sin(-alpha)*x + Math.cos(-alpha)*y;
						
			double dist = calcClose(xNew, yNew, major, minor);			
			
			erg[i] = dist;
		}
				
		input.put(outputKey, erg);		
		
		return input;
	}

	
	// Major parallel zur X-Achse
	// Mittelpunkt bei (0,0)
	private double calcClose(double px, double py, double major, double minor)
	{
		int NIteration = 3600;
		
		double ellX = 0;
		double ellY = 0;
		
		double close = Double.POSITIVE_INFINITY;
		//double closeA = 0;			
				
		for(int i=0; i<NIteration; i++)
		{
			double tmpAngle = 2.0 * (((double)i) / NIteration) * 3.1415926;
			ellX = Math.cos(tmpAngle)*major;
			ellY = Math.sin(tmpAngle)*minor;
			
			double dist = Math.sqrt( Math.pow(ellX-px,2) + Math.pow(ellY-py,2) );
			
			if(dist < close)
			{				
				close  = dist;
				//closeA = tmpAngle;			
			}			
		}
		return close;//intervallHalf(px,py,closeA1, closeA2, major, minor, 5);		
	}	
	
	/*private double intervallHalf(double px, double py, double A1, double A3, double major, double minor, int itr)
	{		
		double A2 = (A1 + A3)/2.0;
		double x1 = Math.cos(A1)*major + Math.sin(A1)*minor;
		double y1 = Math.sin(A1)*major + Math.cos(A1)*minor;
		double x2 = Math.cos(A2)*major + Math.sin(A2)*minor;
		double y2 = Math.sin(A2)*major + Math.cos(A2)*minor;
		double x3 = Math.cos(A3)*major + Math.sin(A3)*minor;
		double y3 = Math.sin(A3)*major + Math.cos(A3)*minor;
		
		if(itr<=0)
		{
			return Math.sqrt( (x2-px)*(x2-px) + (y2-py)*(y2-py) );
		}
		
		double dist1 = Math.sqrt( (x1-px)*(x1-px) + (y1-py)*(y1-py) );
		double dist2 = Math.sqrt( (x2-px)*(x2-px) + (y2-py)*(y2-py) );
		double dist3 = Math.sqrt( (x3-px)*(x3-px) + (y3-py)*(y3-py) );
		
		if(dist1 < dist3 && dist2 < dist3)
		{
			return intervallHalf(px,py, A1, A2, major, minor, itr-1);
		}
		if(dist2 < dist1 && dist3 < dist1)
		{
			return intervallHalf(px,py, A2, A3, major, minor, itr-1);
		}
		if(dist1 < dist2 && dist3 < dist2)
		{
			
		}
		
		return 0;
	}*/


	public String getEllipseAlpha() {
		return ellipseAlpha;
	}


	public void setEllipseAlpha(String ellipseAlpha) {
		this.ellipseAlpha = ellipseAlpha;
	}


	public String getEllipseMajor() {
		return ellipseMajor;
	}


	public void setEllipseMajor(String ellipseMajor) {
		this.ellipseMajor = ellipseMajor;
	}


	public String getEllipseMinor() {
		return ellipseMinor;
	}


	public void setEllipseMinor(String ellipseMinor) {
		this.ellipseMinor = ellipseMinor;
	}


	public String getEllipseX() {
		return ellipseX;
	}


	public void setEllipseX(String ellipseX) {
		this.ellipseX = ellipseX;
	}


	public String getEllipseY() {
		return ellipseY;
	}


	public void setEllipseY(String ellipseY) {
		this.ellipseY = ellipseY;
	}


	public String getSnakeX() {
		return snakeX;
	}


	public void setSnakeX(String snakeX) {
		this.snakeX = snakeX;
	}


	public String getSnakeY() {
		return snakeY;
	}


	public void setSnakeY(String snakeY) {
		this.snakeY = snakeY;
	}


	public String getOutputKey() {
		return outputKey;
	}


	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	
	
	
}
