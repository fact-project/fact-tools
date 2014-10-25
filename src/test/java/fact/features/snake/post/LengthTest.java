package fact.features.snake.post;

import static org.junit.Assert.*;

import org.junit.Test;

import stream.Data;
import stream.data.DataFactory;

public class LengthTest 
{
	@Test
	public void testLength() throws Exception 
	{

		Data item = DataFactory.create();

		double[] x = { 1.0, 1.0, -1.0, -1.0 };
		double[] y = { 1.0, -1.0, -1.0, 1.0 };

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
}
