package fact.features.snake;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

import stream.Data;
import stream.io.SourceURL;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;

import fact.features.snake.FitEllipse;

public class FitEllipseTest 
{
	@Test
	public void testFit() throws Exception
	{
		try {
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);
			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
			
			double[] x = new double[360];
			double[] y = new double[360];
			
			double centerX = 20;
			double centerY = 80;
			double radiusMajor = 50;
			double radiusMinor = 40;
			
			double angle = Math.toRadians(140);
			double sinbeta = Math.sin(angle);
			double cosbeta = Math.cos(angle);
			
			for(int i=0; i<360; i++)
			{
				double alpha = Math.toRadians(i);
				double sinalpha = Math.sin(alpha);
				double cosalpha = Math.cos(alpha);
				
				 x[i] = centerX + (radiusMajor * cosalpha * cosbeta - radiusMinor * sinalpha * sinbeta);
				 y[i] = centerY + (radiusMajor * cosalpha * sinbeta + radiusMinor * sinalpha * cosbeta);
			}
			
			
			item.put("x", x);
			item.put("y", y);
			
			FitEllipse fit = new FitEllipse();
			fit.setSnakeX("x");
			fit.setSnakeY("y");
			fit.setOutkeyAlpha("outAlpha");
			fit.setOutkeyCenterX("outCenterX");
			fit.setOutkeyCenterY("outCenterY");
			fit.setOutkeyMajor("outMajor");
			fit.setOutkeyMinor("outMinor");
			
			
			fit.process(item);
			
			assertTrue("outAlpha nicht enthalten", item.containsKey("outAlpha"));			
			assertTrue("outCenterX nicht enthalten", item.containsKey("outCenterX"));
			assertTrue("outCenterY nicht enthalten", item.containsKey("outCenterY"));
			assertTrue("outMajor nicht enthalten", item.containsKey("outMajor"));
			assertTrue("outMinor nicht enthalten", item.containsKey("outMinor"));
			
			double item_Alpha = Math.toDegrees((Double) item.get("outAlpha"));
			double item_CenterX = (Double) item.get("outCenterX");
			double item_CenterY = (Double) item.get("outCenterY");
			double item_Major = (Double) item.get("outMajor");
			double item_Minor = (Double) item.get("outMinor");
			
			System.out.println("Alpha:" + item_Alpha+ " Center:(" + item_CenterX + ", " + item_CenterY + ") Major:" + item_Major + " Minor:" + item_Minor);
			
			// 4 Digit Precission
			item_Alpha = (double)Math.round(item_Alpha * 10000) / 10000;
			item_CenterX = (double)Math.round(item_CenterX * 10000) / 10000;
			item_CenterY = (double)Math.round(item_CenterY * 10000) / 10000;
			item_Major = (double)Math.round(item_Major * 10000) / 10000;
			item_Minor = (double)Math.round(item_Minor * 10000) / 10000;			
			
			//System.out.println("Alpha:" + item_Alpha + " Center:(" + item_CenterX + ", " + item_CenterY + ") Major:" + item_Major + " Minor:" + item_Minor);
			
			assertTrue("Winkel Falsch", item_Alpha == 140);
			assertTrue("CenterX Falsch", item_CenterX == centerX);
			assertTrue("CenterY Falsch", item_CenterY == centerY);
			assertTrue("Major Falsch", item_Major == radiusMajor);
			assertTrue("Minor Falsch", item_Minor == radiusMinor);
			

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			fail("Could not read stream");
		}
	}
}
