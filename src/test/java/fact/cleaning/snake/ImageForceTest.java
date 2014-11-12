package fact.cleaning.snake;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import cern.colt.Arrays;
import fact.cleaning.snake.ImageForce;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;

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
	private FactPixelMapping PixelMapping_;
	
	public ImageForceTest()
	{
		super(new double[0], 0, 0);	
	}

	@Before
	public void setup() throws Exception
	{
		PixelMapping_ = FactPixelMapping.getInstance();
		
		System.out.println( PixelMapping_.getChidFromSoftID( 0) );
		
		double[] testData = new double[1440];	
		
		FactCameraPixel[] list = PixelMapping_.getNeighborsForPixelWithDirection(
				PixelMapping_.getPixelFromId(
						PixelMapping_.getChidFromSoftID( 0)		)	);
		
		System.out.println(Arrays.toString(list) );
		// siehe FACTmapV5.txt -> Resources
		testData[ PixelMapping_.getChidFromSoftID( 0)] =     1; // Center 	 ( 4)		
		testData[ PixelMapping_.getChidFromSoftID( 1)] =     3; // Bot		 ( 5)		
		testData[ PixelMapping_.getChidFromSoftID( 2)] =    10; // leftBot	 ( 2)		
		testData[ PixelMapping_.getChidFromSoftID( 3)] =    30; // leftTop	 ( 1)		
		testData[ PixelMapping_.getChidFromSoftID( 4)] =   100; // top		 ( 3)		
		testData[ PixelMapping_.getChidFromSoftID( 5)] =   300; // rightTop  	 ( 7)		
		testData[ PixelMapping_.getChidFromSoftID( 6)] =  1000; // rightBot	 ( 6)
		
		testData[ PixelMapping_.getChidFromSoftID(15)] =  3000; // top2	  	 ( 8)
		testData[ PixelMapping_.getChidFromSoftID(16)] = 10000; // rightTop2   ( 9)
		testData[ PixelMapping_.getChidFromSoftID(17)] = 30000; // rightBot2   (10)
		
		data = testData;
	}
	
	@Test
	public void testGradient()
	{
		int center1 = PixelMapping_.getChidFromSoftID(0);	
		int center2 = PixelMapping_.getChidFromSoftID(5);
		
		//Example: 
		int x1_1 = (2*300 + 100 + 1000) - (2*10 + 30 + 3);
		int x1_2 = (2*1000 + 300 + 3) - (2*30 + 100 + 10);
		int x1 = (x1_1 + x1_2)/2;
		System.out.println("Grad: " + x1 + " | " + gradX(center1));
		
		int y1 = (2*100 + 300 + 30) - (2*3 + 1000 + 10);
		System.out.println("Grad: " + y1 + " | " + gradY(center1));
		
		
		
		// Test 1 Spalte:																  // R - L = m // (rt) | (rb)
		assertTrue("X-Gradient 1 wird nicht richtig berechnet!", gradX(center1) == +1890); // (1700-53) | (2303 - 170)
		assertTrue("Y-Gradient 1 wird nicht richtig berechnet!", gradY(center1) == -486); // (530 - 1016) // T - B   
				
		// Test 2 Spalte:		
		assertTrue("X-Gradient 2 wird nicht richtig berechnet!", gradX(center2) == +59848.5); // -(71000 - 3201) | -(53000 - 1102)
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
