package fact.features.snake;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.QRDecomposition;

import fact.Constants;
import fact.EventUtils;

import fact.viewer.ui.SnakeDraw;
import stream.Data;
import stream.Processor;


public class FitEllipse  implements Processor
{
	private String outkeyAlpha = null;	
	private String outkeyCenterX = null;	
	private String outkeyCenterY = null;	
	private String outkeyMinor = null;	
	private String outkeyMajor = null;	
	
	private String snakeX = null;
	private String snakeY = null;
	
	private String drawEllipse = null;
	
	
	private double centerX = 0;
	private double centerY = 0;
	
	private double major = 0;
	private double minor = 0;
	
	private double angle = 0;

	void calcParams(double[] data)
	{
		double a = data[0];
		double b = data[1] / 2.0;
		double c = data[2];
		double d = data[3] / 2.0;
		double f = data[4] / 2.0;
		double g = data[5];

		double num = b*b-a*c;

		double x0 = (c*d-b*f) / num;
		double y0 = (a*f-b*d) / num;

			
		double up = 2*(a*f*f+c*d*d+g*b*b-2*b*d*f-a*c*g);

		double down1 = (b*b-a*c) * (Math.sqrt( ((a-c)*(a-c)) + (4*b*b) ) - (a+c));
		double down2 = (b*b-a*c) * ((-Math.sqrt( ((a-c)*(a-c)) + (4*b*b) )) - (a+c));		

		double res1 = Math.sqrt(up/down1);
		double res2 = Math.sqrt(up/down2);

		double alpha = 0;

		if(b == 0 && a<c)
		{
			alpha = 0;
		}
		else if(b == 0 && a>c)
		{
			alpha = 0.5 * 3.1415926;
		}
		else if(b != 0 && Math.abs(a)<Math.abs(c))
		{
			alpha = 0.5 * Math.atan((2.0 * b)/(a-c));	// counterclockwise angle of rotation from the x-axis to the major axis
		}
		else if(b != 0 && Math.abs(a)>Math.abs(c))
		{			
			alpha = 0.5 * Math.atan((2.0 * b)/(a-c)) + (0.5 * 3.1415926);
		}
		

		centerX = x0;
		centerY = y0;

		major = (res1 < res2) ? res2 : res1;
		minor = (res1 < res2) ? res1 : res2;

		if(alpha <= 0)
		{
			angle = Math.PI + alpha;
		}
		else
		{
			angle = alpha;
		}
		
	}
	
	
	@Override
	public Data process(Data input) 	//http://autotrace.sourceforge.net/WSCG98.pdf
	{
		if(outkeyAlpha == null) throw new RuntimeException("Missing parameter: outkeyAlpha");
		if(outkeyCenterX == null) throw new RuntimeException("Missing parameter: outkeyCenterX");
		if(outkeyCenterY == null) throw new RuntimeException("Missing parameter: outkeyCenterY");
		if(outkeyMinor == null) throw new RuntimeException("Missing parameter: outkeyMinor");
		if(outkeyMajor == null) throw new RuntimeException("Missing parameter: outkeyMajor");
		
		EventUtils.mapContainsKeys(getClass(), input, snakeX, snakeY);		
		double[] x = (double[]) input.get(snakeX);
		double[] y = (double[]) input.get(snakeY);
		
		int size = x.length;
		
		RealMatrix D1 = new Array2DRowRealMatrix(size,3);
		RealMatrix D2 = new Array2DRowRealMatrix(size,3);
		
		for(int i=0; i<size; i++)
		{
			D1.setEntry(i, 0, x[i] * x[i]) ;
			D1.setEntry(i, 1, x[i] * y[i]) ;
			D1.setEntry(i, 2, y[i] * y[i]) ;

			D2.setEntry(i, 0, x[i]) ;
			D2.setEntry(i, 1, y[i]) ;
			D2.setEntry(i, 2, 1) ;
		}
		
		RealMatrix S1 = D1.transpose().multiply(D1);
		RealMatrix S2 = D1.transpose().multiply(D2);
		RealMatrix S3 = D2.transpose().multiply(D2);
		

		RealMatrix tmp = new QRDecomposition(S3).getSolver().getInverse();
		
		RealMatrix T = tmp.multiply(S2.transpose()).scalarMultiply(-1);
		
		RealMatrix M = S1.add(S2.multiply(T));

		RealMatrix C1 = new Array2DRowRealMatrix(3,3);
		C1.setEntry(0, 0, 0);
		C1.setEntry(0, 1, 0);
		C1.setEntry(0, 2, 0.5);
		C1.setEntry(1, 0, 0);
		C1.setEntry(1, 1, -1);
		C1.setEntry(1, 2, 0);
		C1.setEntry(2, 0, 0.5);
		C1.setEntry(2, 1, 0);
		C1.setEntry(2, 2, 0);

		

		M = C1.multiply(M);

			
	
		EigenDecomposition eigenSys = new EigenDecomposition(M);
		
		Complex[] eigenVal = new Complex[3];
		RealMatrix[] eigenVec = new RealMatrix[3];
		
		for(int i=0; i<3; i++)
		{
			eigenVal[i] = Complex.valueOf(eigenSys.getRealEigenvalue(i), eigenSys.getImagEigenvalue(i));
			
			eigenVec[i] = new Array2DRowRealMatrix(3,1);
			
			for(int j=0;j<3;j++)
			{
				eigenVec[i].setEntry(j,0, eigenSys.getEigenvector(i).getEntry(j)); 
			}
			
		}
		
		

		RealMatrix a1 = new Array2DRowRealMatrix(3,1);

		
		
		for(int i = 0; i< 3; i++)
		{
			double cond = 4.0 * (eigenVec[i].getEntry(0, 0)*eigenVec[i].getEntry(2, 0)) - ( eigenVec[i].getEntry(1, 0) * eigenVec[i].getEntry(1, 0) );			

			if(cond > 0)
			{				
				for(int j=0; j<3; j++)
				{					
					a1.setEntry(j, 0, eigenVec[i].getEntry(j,0) );
				}			
			}
		}

		RealMatrix a2 = T .multiply(a1);

		double[] parameter = new double[6];
		
		parameter[0] = a1.getEntry(0, 0);
		parameter[1] = a1.getEntry(1, 0);
		parameter[2] = a1.getEntry(2, 0);
		parameter[3] = a2.getEntry(0, 0);
		parameter[4] = a2.getEntry(1, 0);
		parameter[5] = a2.getEntry(2, 0);
		
		calcParams(parameter);
		
		
		input.put(outkeyAlpha, angle);
		input.put(outkeyCenterX, centerX);
		input.put(outkeyCenterY, centerY);
		input.put(outkeyMajor, major);
		input.put(outkeyMinor, minor);
		
		
		if(drawEllipse != null)
		{
			SnakeDraw sn = new SnakeDraw();
			double[][] ellipseX = new double[1][360];
			double[][] ellipseY = new double[1][360];
		
			for(int i=0; i<360;i++)
			{			
				double tmpAng = (i / 180.0) * 3.1415926;

				ellipseX[0][i]= major*Math.cos(tmpAng)*Math.cos(angle) - minor*Math.sin(tmpAng)*Math.sin(angle);
				ellipseY[0][i]= minor*Math.cos(angle)*Math.sin(tmpAng) + major*Math.cos(tmpAng)*Math.sin(angle);				

				ellipseX[0][i] = ellipseX[0][i] + centerX;
				ellipseY[0][i] = ellipseY[0][i] + centerY; 			
			}
			sn.setShape(ellipseX, ellipseY);
		
			input.put(Constants.SNAKE_ELLIPSE_OVERLAY, sn);
		}
		
		
		return input;
	}


	public String getOutkeyAlpha() {
		return outkeyAlpha;
	}


	public void setOutkeyAlpha(String outkeyAlpha) {
		this.outkeyAlpha = outkeyAlpha;
	}


	public String getOutkeyCenterX() {
		return outkeyCenterX;
	}


	public void setOutkeyCenterX(String outkeyCenterX) {
		this.outkeyCenterX = outkeyCenterX;
	}


	public String getOutkeyCenterY() {
		return outkeyCenterY;
	}


	public void setOutkeyCenterY(String outkeyCenterY) {
		this.outkeyCenterY = outkeyCenterY;
	}


	public String getOutkeyMinor() {
		return outkeyMinor;
	}


	public void setOutkeyMinor(String outkeyMinor) {
		this.outkeyMinor = outkeyMinor;
	}


	public String getOutkeyMajor() {
		return outkeyMajor;
	}


	public void setOutkeyMajor(String outkeyMajor) {
		this.outkeyMajor = outkeyMajor;
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


	public String getDrawEllipse() {
		return drawEllipse;
	}


	public void setDrawEllipse(String drawEllipse) {
		this.drawEllipse = drawEllipse;
	}
	
	

	
	
}
