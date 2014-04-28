package fact.features.snake;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

import stream.Data;
import stream.io.SourceURL;
import fact.features.snake.Area;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;

public class AreaTest 
{
	
	@Test
	public void testArea() throws Exception
	{
		try {
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);
			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
			
			
			double[] x = {  1.0,  1.0, -1.0 , -1.0};
			double[] y = {  1.0, -1.0, -1.0 ,  1.0};
			
			item.put("x", x);
			item.put("y", y);
			
			Area area = new Area();
			area.setSnakeX("x");
			area.setSnakeY("y");
			area.setOutkey("out");
			
			area.process(item);
			
			assertTrue("out nicht enthalten", item.containsKey("out"));
			
			double erg = (Double) item.get("out");
			
			//assertTrue("Fläche falsch berechnet!", erg == 4.0);
			

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			fail("Could not read stream");
		}		
	}

}
