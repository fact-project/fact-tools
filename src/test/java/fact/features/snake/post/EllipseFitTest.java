package fact.features.snake.post;

import static org.junit.Assert.*;

import org.junit.Test;

import stream.Data;
import stream.data.DataFactory;

public class EllipseFitTest 
{

	@Test
	public void testEllipseFit()
	{
		Data item = DataFactory.create();
		
		final int numberOfNodes = 50;
		
		double[] nodeX = new double[50];
		double[] nodeY = new double[50];
		
		double centerX = 20;
		double centerY = 80;
		double radiusMajor = 50;
		double radiusMinor = 40;
		
		double beta = Math.toRadians(0);
		
		for(int i = 0; i<numberOfNodes; i++)
		{
			double ang = (2*Math.PI / numberOfNodes) * i;
			
			nodeX[i] = centerX + radiusMajor * Math.cos(ang) * Math.cos(beta) - radiusMinor * Math.sin(ang) * Math.sin(beta);
			nodeY[i] = centerY + radiusMajor * Math.cos(ang) * Math.sin(beta) + radiusMinor * Math.sin(ang) * Math.cos(beta);			
		}		
		
		item.put("X", nodeX);
		item.put("Y", nodeY);
		
		FitEllipse fe = new FitEllipse();
		fe.setSnakeX("X");
		fe.setSnakeY("Y");
		
		fe.setOutkeyAlpha("a");
		fe.setOutkeyCenterX("cx");
		fe.setOutkeyCenterY("cy");
		fe.setOutkeyMajor("major");
		fe.setOutkeyMinor("minor");
		
		fe.process(item);
		
		double r_major = (Double)item.get("major");
		double r_minor = (Double)item.get("minor");
		double r_beta  = (Double)item.get("a");
		double r_cx    = (Double)item.get("cx");
		double r_cy    = (Double)item.get("cy");
		
		// 4 Digit Precission
		r_beta = (double)Math.round(r_beta * 10000) / 10000;
		r_cx = (double)Math.round(r_cx * 10000) / 10000;
		r_cy = (double)Math.round(r_cy * 10000) / 10000;
		r_major = (double)Math.round(r_major * 10000) / 10000;
		r_minor = (double)Math.round(r_minor * 10000) / 10000;	
					
		assertTrue("r1 assert", radiusMajor == r_major );
		assertTrue("r2 assert", radiusMinor == r_minor );
		assertTrue("beta assert", beta == r_beta );
		assertTrue("c1 assert", centerX == r_cx );
		assertTrue("c2 assert", centerY == r_cy );
		
		System.out.println(" --Ellipse Fit Diff-- " );
		System.out.println("Major: " + (r_major - radiusMajor));
		System.out.println("Minor: " + (r_minor - radiusMinor));
		System.out.println("Center: " + Math.sqrt(Math.pow(r_cx - centerX, 2) + Math.pow(r_cy - centerY, 2)));
		System.out.println("Winkel: " + (r_beta - beta));
		
	}
	
	
}
