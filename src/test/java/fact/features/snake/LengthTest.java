package fact.features.snake;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

import stream.Data;
import stream.io.SourceURL;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;

public class LengthTest 
{
	@Test
	public void testLength() throws Exception
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
			
			PolygonLength length = new PolygonLength();
			length.setPolygonX("x");
			length.setPolygonY("y");
			length.setOutkey("out");
			
			length.process(item);
			
			assertTrue("out nicht enthalten", item.containsKey("out"));
			
			double erg = (Double) item.get("out");			
			
			assertTrue("Länge falsch berechnet!", erg == 8.0);
			

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			fail("Could not read stream");
		}		
	}
}
