package fact.features.snake.post;

import static org.junit.Assert.*;

import org.junit.Test;

import stream.Data;
import stream.data.DataFactory;

public class CenterTest 
{

	@Test
	public void testCenter() throws Exception 
	{
		Data item = DataFactory.create();

		double[] x = { 3.0, 3.0, 1.0, 1.0 };
		double[] y = { 11.0, 9.0, 9.0, 11.0 };

		item.put("x", x);
		item.put("y", y);

		PolygonCenter center = new PolygonCenter();
		center.setPolygonX("x");
		center.setPolygonY("y");
		center.setOutkeyX("outX");
		center.setOutkeyY("outY");

		center.process(item);

		assertTrue("outX nicht enthalten", item.containsKey("outX"));
		assertTrue("outY nicht enthalten", item.containsKey("outY"));

		double ergX = (Double) item.get("outX");
		double ergY = (Double) item.get("outY");

		assertTrue("Center falsch berechnet!", ergX == 2.0);
		assertTrue("Center falsch berechnet!", ergY == 10.0);

	}
}