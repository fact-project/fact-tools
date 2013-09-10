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
		int[] n = DefaultPixelMapping.getNeighborsFromSoftID(969);
		Integer[] nS = {1079, 1080, 968, 970, 865, 864};
		List<Integer> l = Arrays.asList(nS);
		
		for(int p : n){
			if(!l.contains(p)){
				fail("Pixelmapping did not deliver the correct neighbours for softid 969");
			}
		}
		
		n = DefaultPixelMapping.getNeighborsFromSoftID(1080);
		Integer[] nS2 = {1079, 1081, 969, 970, 1197, 1196};
		l = Arrays.asList(nS2);
		for(int p : n){
			if(!l.contains(p)){
				fail("Pixelmapping did not deliver the correct neighbours for softid 1080");
			}
		}
		for (int chid = 0; chid < 1440; ++chid){
			if (DefaultPixelMapping.getNeighborsFromChid(chid).length != 6){
				fail("map did not return the right array for chid " + chid);
			}
		}
		// for (int i = 0; i < hardware2softwareID.length; i++) {
		// log.info("hardId: {}  =>  softId: {}", i, hardware2softwareID[i]);
		// }
	}
	@Test
	public void testKoordinateToChid() {
		// -180,999 .... 180,999
		float[] x = {-123.28f};
		// -180,5 .... 190,0
		float[] y = {104.3f};
		for (int i = 0 ; i<x.length ; i++)
		{
			int nearestChid = -1;
			double lowestDistance = 100000.0d;
			for (int chid = 0 ; chid < Constants.NUMBEROFPIXEL ; chid++ )
			{
				float xChid = DefaultPixelMapping.getGeomX(chid);
				float yChid = DefaultPixelMapping.getGeomY(chid);
				double distance = Math.sqrt((xChid-x[i])*(xChid-x[i])+(yChid-y[i])*(yChid-y[i]));
				if (distance < lowestDistance)
				{
					nearestChid = chid;
					lowestDistance = distance;
				}
			}
		}
	}
	@Test
	public void testGeoToChid(){
		//191    2512      -8      -3    1611   71.12   6  10     -6.93      3.50     
		float x = -6.93f;
		float y = 3.5f;
		int chid =  DefaultPixelMapping.getChidID(191);
		assertEquals(chid, DefaultPixelMapping.geomToChid(x, y));
		
		x = -19.05f;
		y = 5.5f;
		chid =  DefaultPixelMapping.getChidID(1393);
		assertEquals(chid, DefaultPixelMapping.geomToChid(x, y));
		
		x = -19.06f;
		y = 5.6f;
		chid =  DefaultPixelMapping.getChidID(1393);
		assertEquals(chid, DefaultPixelMapping.geomToChid(x, y));
		
		//außerhalb
		x = -30.06f;
		y = 9.6f;
		assertEquals(-1, DefaultPixelMapping.geomToChid(x, y));
	}
	
	
}
