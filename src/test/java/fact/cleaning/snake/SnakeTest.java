package fact.cleaning.snake;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

import stream.Data;
import stream.data.DataFactory;

public class SnakeTest
{
	@Test
	public void testSnake() throws Exception
	{
				
			Data item = DataFactory.create();			
			
			CreateGaus gaus = new CreateGaus();			
			gaus.setSigmaX(15);
			gaus.setSigmaY(33);
			
			gaus.setX0(-22);
			gaus.setY0(45);
			
			gaus.setOutputKey("gaus");			
			gaus.process(item);
			
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
			
			assertTrue("Snake numberOfVertices kleiner 6", 6 < nov );
			
	}
	
	@Test
	public void testMatrix() throws Exception
	{
		Snake sn = new Snake();
		
		sn.initConstant(0.03, 0.01, 0.08, 1.0);
		sn.initMatrix();
		
	}
	
	
}
