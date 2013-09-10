package fact.camera;

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
	
	
}
