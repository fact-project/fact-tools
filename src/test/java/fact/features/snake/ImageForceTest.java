package fact.features.snake;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import fact.viewer.ui.DefaultPixelMapping;

import fact.cleaning.snake.ImageForce;

public class ImageForceTest extends ImageForce
{
/*        )--.
      ___/    \___
     /   \ 8  /   \
 ,--(  3  )--(  9  )
/    \___/    \___/
\  1 /   \ 7  /   \
 )--(  4  )--(  10 )
/    \___/    \___/
\  2 /   \ 6  /
 `--(  5  )--'
     \___/

 */
	
	public ImageForceTest()
	{
		super(new double[0], 0, 0);	
	}

	@Before
	public void setup() throws Exception
	{
		double[] testData = new double[1440];
		
		// siehe FACTmapV5.txt -> Resources
		testData[ DefaultPixelMapping.getChidFromSoftId( 0)] =     1; // Center 	 ( 4)		
		testData[ DefaultPixelMapping.getChidFromSoftId( 1)] =     3; // Bot		 ( 5)		
		testData[ DefaultPixelMapping.getChidFromSoftId( 2)] =    10; // leftBot	 ( 2)		
		testData[ DefaultPixelMapping.getChidFromSoftId( 3)] =    30; // leftTop	 ( 1)		
		testData[ DefaultPixelMapping.getChidFromSoftId( 4)] =   100; // top		 ( 3)		
		testData[ DefaultPixelMapping.getChidFromSoftId( 5)] =   300; // rightTop  ( 7)		
		testData[ DefaultPixelMapping.getChidFromSoftId( 6)] =  1000; // rightBot	 ( 6)
		
		testData[ DefaultPixelMapping.getChidFromSoftId(15)] =  3000; // top2	  	 ( 8)
		testData[ DefaultPixelMapping.getChidFromSoftId(16)] = 10000; // rightTop2 ( 9)
		testData[ DefaultPixelMapping.getChidFromSoftId(17)] = 30000; // rightBot2 (10)
		
		data = testData;
	}
	
	@Test
	public void testGradient()
	{
		int center1 = DefaultPixelMapping.getChidFromSoftId(0);	

		// Test 1 Spalte:																  // R - L = m // (rt) | (rb)
		assertTrue("X-Gradient 1 wird nicht richtig berechnet!", gradX(center1) == 1890); // (1700-53) | (2303 - 170)
		assertTrue("Y-Gradient 1 wird nicht richtig berechnet!", gradY(center1) == -486); // (530 - 1016) // T - B   
				

					
	}

	@Override
	public double forceX(double x, double y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double forceY(double x, double y) {
		// TODO Auto-generated method stub
		return 0;
	}

}
