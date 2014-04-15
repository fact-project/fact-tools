package fact.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fact.Constants;
import fact.viewer.ui.DefaultPixelMapping;

public class PixelMappingTest {

	@Test
	public void testFactMapping() {
		int chid = DefaultPixelMapping.getChidFromSoftId(969);
		int[] n = DefaultPixelMapping.getNeighborsFromChid(chid);
		
		Integer[] nS = {1079, 1080, 968, 970, 865, 864};
		for(int i = 0; i < nS.length; i++){
			nS[i] = DefaultPixelMapping.getChidFromSoftId(nS[i]);
		}
		
		List<Integer> l = Arrays.asList(nS);
		for(int p : n){
			if(!l.contains(p)){
				fail("Pixelmapping did not deliver the correct neighbours for softid 969");
			}
		}
		
		chid = DefaultPixelMapping.getChidFromSoftId(1080);
		n = DefaultPixelMapping.getNeighborsFromChid(chid);
		
		Integer[] nS2h = {1079, 1081, 969, 970, 1197, 1196};
		for(int i = 0; i < nS2h.length; i++){
			nS2h[i] = DefaultPixelMapping.getChidFromSoftId(nS2h[i]);
		}
		
		l = Arrays.asList(nS2h);
		for(int p : n){
			if(!l.contains(p)){
				fail("Pixelmapping did not deliver the correct neighbours for softid 1080");
			}
		}
		

		for (int id = 0; id < 1440; ++id){
			if (DefaultPixelMapping.getNeighborsFromChid(id).length != 6){
				fail("map did not return the right array for chid " + id);
			}
		}
		
		
	}
	
	@Test
	public void testKoordinateToChid() {
		// -180,999 .... 180,999
		float[] xs = {120.513f, 12.22f,-80.324f, -120.6f, -6.93f  * 9.5f};
		float[] ys = {80.113f, 102.22f,-5.324f, 20.5f,3.49f  * 9.5f };
//		float x = -6.93f  * 9.5f;
//		float y = 3.5f  * 9.5f;
		
		for (int i = 0; i < xs.length; i++){
			float x = xs[i];
			float y = ys[i];
			int nearestChid = -1;
			double lowestDistance = 100000.0d;
			for (int chid = 0 ; chid < Constants.NUMBEROFPIXEL ; chid++ )
			{
				float xChid = DefaultPixelMapping.getPosXinMM(chid);
				float yChid = DefaultPixelMapping.getPosYinMM(chid);
				double distance = Math.sqrt( (xChid-x)*(xChid-x) + (yChid-y)*(yChid-y) );
				if (distance < lowestDistance)
				{
					nearestChid = chid;
					lowestDistance = distance;
				}
			}
			assertEquals("Fail: x,y : " + x + ", " + y,nearestChid, DefaultPixelMapping.coordinatesToChid(x, y));
		}
//		System.out.println("x and y of nearest chid: " + DefaultPixelMapping.getGeomX(nearestChid) + "  " + DefaultPixelMapping.getGeomY(nearestChid) + "           x and y given: " + x + " " + y);
	}


    /**
     * Test some hardcoded coordinate that I looked up on the poster
     */
	@Test
	public void testGeoToChid(){
		//check inside camera bounds
		float x = -6.93f  * 9.5f;
		float y = 3.5f  * 9.5f;
		int chid =  DefaultPixelMapping.getChidFromSoftId(191);
		//911
		assertEquals(chid, DefaultPixelMapping.coordinatesToChid(x, y));
		
		x = -19.05f * 9.5f;
		y = 5.5f * 9.5f;
		chid =  DefaultPixelMapping.getChidFromSoftId(1393);
		assertEquals(chid, DefaultPixelMapping.coordinatesToChid(x, y));
		
		x = -19.06f  * 9.5f;
		y = 5.6f  * 9.5f;
		chid =  DefaultPixelMapping.getChidFromSoftId(1393);
		assertEquals(chid, DefaultPixelMapping.coordinatesToChid(x, y));
		
		//outside camera bounds
		x = -30.06f * 9.5f;
		y = 9.6f * 9.5f;
		assertEquals(-1, DefaultPixelMapping.coordinatesToChid(x, y));
	}
	
	
}
