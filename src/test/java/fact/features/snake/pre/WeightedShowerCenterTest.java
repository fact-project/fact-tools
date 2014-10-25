package fact.features.snake.pre;

import static org.junit.Assert.*;

import org.junit.Test;

import fact.Constants;
import fact.cleaning.snake.CreateGaus;
import stream.Data;
import stream.data.DataFactory;

public class WeightedShowerCenterTest extends CreateGaus {
	@Test
	public void testCenter() {

		Data item = DataFactory.create();

		this.setSigmaX(10);
		this.setSigmaY(-23);

		this.setX0(-22);
		this.setY0(45);

		this.setOutputKey("gaus");
		this.process(item);

		int[] shower = new int[1440];
		for (int i = 0; i < Constants.NUMBEROFPIXEL; i++) {
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

		System.out.println("Aproximate Center:" + ergX + " " + ergY);	
		System.out.println("Real Center:" + -22 + " " + 45);	

		assertEquals("CenterX falsch", -22, ergX, 0.1);
		assertEquals("CenterY falsch", 45, ergY, 0.1);

	}
}