package fact.cleaning.snake;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import fact.mapping.FactPixelMapping;
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
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();
	
	public ImageForceTest()
	{
		super(new double[0], 0, 0);	
	}

	@Before
	public void setup() throws Exception
	{
		double[] testData = new double[1440];
		
		// siehe FACTmapV5.txt -> Resources
		testData[ pixelMap.getChidFromSoftID( 0)] =     1; // Center 	 ( 4)		
		testData[ pixelMap.getChidFromSoftID( 1)] =     3; // Bot		 ( 5)		
		testData[ pixelMap.getChidFromSoftID( 2)] =    10; // leftBot	 ( 2)		
		testData[ pixelMap.getChidFromSoftID( 3)] =    30; // leftTop	 ( 1)		
		testData[ pixelMap.getChidFromSoftID( 4)] =   100; // top		 ( 3)		
		testData[ pixelMap.getChidFromSoftID( 5)] =   300; // rightTop  	 ( 7)		
		testData[ pixelMap.getChidFromSoftID( 6)] =  1000; // rightBot	 ( 6)
		
		testData[ pixelMap.getChidFromSoftID(15)] =  3000; // top2	  	 ( 8)
		testData[ pixelMap.getChidFromSoftID(16)] = 10000; // rightTop2   ( 9)
		testData[ pixelMap.getChidFromSoftID(17)] = 30000; // rightBot2   (10)
		
		data = testData;
	}
	
	@Test
	public void testGradient()
	{
		int center1a = DefaultPixelMapping.getChidFromSoftId(0);	
		int center2a = DefaultPixelMapping.getChidFromSoftId(5);
		int center1 = pixelMap.getChidFromSoftID(0);	
		int center2 = pixelMap.getChidFromSoftID(5);
		
		System.out.println(center1a +" "+ center1 + " " + center2a + " " + center2);
		
		System.out.println("grad(center1): "+gradX(center1));
		// Test 1 Spalte:																  // R - L = m // (rt) | (rb)
		assertTrue("X-Gradient 1 wird nicht richtig berechnet!", gradX(center1) == 1890); // (1700-53) | (2303 - 170)
		assertTrue("Y-Gradient 1 wird nicht richtig berechnet!", gradY(center1) == -486); // (530 - 1016) // T - B   
				
		// Test 2 Spalte:		
		assertTrue("X-Gradient 2 wird nicht richtig berechnet!", gradX(center2) == 59848.5); // -(71000 - 3201) | -(53000 - 1102)
		assertTrue("Y-Gradient 2 wird nicht richtig berechnet!", gradY(center2) == -15901); // (16100 - 32001)
					
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
