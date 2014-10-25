package fact.features.snake.post;

import static org.junit.Assert.*;

import org.junit.Test;

import stream.Data;
import stream.data.DataFactory;

public class AreaTest 
{
	
	@Test
	public void testArea() throws Exception {

		Data item = DataFactory.create();

		double[] x = { 1.0, 1.0, -1.0, -1.0 };
		double[] y = { 1.0, -1.0, -1.0, 1.0 };

		item.put("x", x);
		item.put("y", y);

		PolygonArea area = new PolygonArea();
		area.setPolygonX("x");
		area.setPolygonY("y");
		area.setOutkey("out");

		area.process(item);

		assertTrue("out nicht enthalten", item.containsKey("out"));

		double erg = (Double) item.get("out");

		assertTrue("Fläche falsch berechnet!", erg == 4.0);

	}
}
