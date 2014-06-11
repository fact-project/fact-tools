package fact.snake;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

import stream.Data;
import stream.io.SourceURL;
import fact.Constants;
import fact.snake.WeightedShowerCenter;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;

public class WeightedShowerCenterTest extends CreateGaus
{
	@Test
	public void testCenter() throws Exception
	{
		try {
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);
			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
			
						
			this.setSigmaX(10);
			this.setSigmaY(-23);
			
			this.setX0(-22);
			this.setY0(45);
			
			this.setOutputKey("gaus");			
			this.process(item);
			
			int[] shower = new int[1440];
			for(int i=0; i<Constants.NUMBEROFPIXEL; i++)
			{
				shower[i] = i;
			}
			item.put("shower", shower);
			
			WeightedShowerCenter wsc = new WeightedShowerCenter();
			wsc.setWeight("gaus");
			wsc.setShower("shower");
			wsc.setOutkeyX("outX");
			wsc.setOutkeyY("outY");
			wsc.process(item);
			
			assertTrue("outX nicht enthalten", item.containsKey("outX"));
			assertTrue("outY nicht enthalten", item.containsKey("outY"));
			
			double ergX = (Double) item.get("outX");
			double ergY = (Double) item.get("outY");
			
			System.out.println(ergX + " " + ergY);			
			
			assertEquals("CenterX falsch",-22, ergX, 0.01);
			assertEquals("CenterY falsch", 45, ergY, 0.01);

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			fail("Could not read stream");
		}		
	}
}
