package fact.cleaning.snake;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

import stream.Data;
import stream.data.DataFactory;
import stream.io.SourceURL;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;

public class SnakeTest extends CreateGaus
{
	@Test
	public void testSnake() throws Exception
	{
		try {			
			Data item = DataFactory.create();			
			
			
			this.setSigmaX(15);
			this.setSigmaY(33);
			
			this.setX0(-22);
			this.setY0(45);
			
			this.setOutputKey("gaus");			
			this.process(item);
			
			SnakeSingle sn = new SnakeSingle();
			
			sn.setForceName("Photoncharge");
			
			sn.init(null);
			
			sn.setPixelDataName("gaus");
			
			item.put("Mean", 0.5);
			sn.setMean("Mean");
			
			item.put("centerX", -22.0);
			sn.setShowerCenterX("centerX");
			
			item.put("centerY", 45.0);
			sn.setShowerCenterY("centerY");
			
			sn.setSnakeOutX("snakeOutX");
			sn.setSnakeOutY("snakeOutY");
			sn.setNumberOfVerticesOut("numberOfVertices");
			
			sn.process(item);
			
			int nov = (Integer) item.get("numberOfVertices");
			
			assertTrue( 6 < nov );
			
			assertTrue(item.containsKey("snakeOutX"));
			assertTrue(item.containsKey("snakeOutY"));

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			fail("Could not read stream");
		}		
	}
}