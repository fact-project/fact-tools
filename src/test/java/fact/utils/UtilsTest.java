package fact.utils;

import static org.junit.Assert.*;

import fact.Utils;
import org.junit.Test;

public class UtilsTest {

	@Test
	public void testPointToShowerCoord(){
		double cogx = 5.0;
		double cogy = 7.0;
		double x = 15.0;
		double y = 7 + Math.tan(30.0/180.0*Math.PI)*10.0;
		double delta = -45.0/180.0*Math.PI;
		double[] pointRot = {0,0};
		pointRot[0] = Math.cos(-15.0/180.0*Math.PI)*10.0/Math.cos(30.0/180.0*Math.PI);
		pointRot[1] = Math.sin(-15.0/180.0*Math.PI)*10.0/Math.cos(30.0/180.0*Math.PI);
		
		double[] result = Utils.rotatePointInShowerSystem(x, y, cogx, cogy, delta);
		
		double diff = Math.abs( Math.sqrt( Math.pow(pointRot[0]-result[0],2) + Math.pow(pointRot[1]-result[1],2) ) );
		
		assertTrue("Rotation of point into shower coordinates didn't worked.\nCalculated result: " +
				 result[0] + "," + result[1] + "\n correct Result: " +
				 pointRot[0] + "," + pointRot[1],diff < 1E-3 );
		
	}

}
