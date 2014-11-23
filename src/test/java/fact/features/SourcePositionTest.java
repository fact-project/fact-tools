package fact.features;

import fact.Constants;
import fact.features.source.SourcePosition;
import fact.hexmap.FactPixelMapping;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SourcePositionTest {
	
	private SourcePosition sourcePosition;
    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Before
	public void setup()
	{
		sourcePosition = new SourcePosition();
		sourcePosition.setOutputKey("test");
	}

    /**
     *
     * For some known az, dec coordinates for Ceta Tauri calculate the corresponding sourceposition
     * in the camera and compare that to the position of the pixels which are known to show the image of ceta tauri
     *
     * @throws Exception
     */
	@Test
	public void testCetaTauri() throws Exception
	{
		double C_T_rightAscension = (5.0 + 37.0/60 + 38.7/3600) / 24.0 * 360.0;
		double C_T_declination = 21.0 + 8.0/60 + 33.0/3600;
		
		int[] C_T_chids = {1378,215,212};
		double[] C_T_coord = new double[2];
		
		for (int i = 0 ; i < 3 ; i++)
		{
			C_T_coord[0] += pixelMap.getPixelFromId(C_T_chids[i]).getXPositionInMM();
			C_T_coord[1] += pixelMap.getPixelFromId(C_T_chids[i]).getYPositionInMM();
		}
		
		C_T_coord[0] /= 3;
		C_T_coord[1] /= 3;
		
		sourcePosition.setSourceRightAscension(C_T_rightAscension);
		sourcePosition.setSourceDeclination(C_T_declination);
		
		
		double pointingRa = 83.1375;
		double pointingDec = 21.628055555555555;
		double gmst = 1.1289573103059787;
		
		double[] pointingAzDe = sourcePosition.getAzZd(pointingRa, pointingDec, gmst);
		double[] sourceAzDe = sourcePosition.getAzZd(C_T_rightAscension, C_T_declination, gmst);
		
		double[] sPos =  sourcePosition.getSourcePosition(pointingAzDe[0], pointingAzDe[1], sourceAzDe[0], sourceAzDe[1]);
				
		assertTrue("Calculated coordinates of ceta tauri doesn't fit the coordinates I got from flashy pixels:\n"
				+ "Coord_from_pixel: ("+C_T_coord[0]+","+C_T_coord[1]+")\n"
				+ "Coord_calculated: ("+sPos[0]+","+sPos[1]+")", (Math.abs(C_T_coord[0]-sPos[0])<Constants.PIXEL_SIZE && Math.abs(C_T_coord[1]-sPos[1])<Constants.PIXEL_SIZE));
	}
	
	@Test
	public void testSouthOrientation()
	{
		double zdSource = 0.6;
		double azSource = 0.0;
		double zdPointing = 0.0;
		double azPointing = 0.0;
		
		double[] r = sourcePosition.getSourcePosition(azPointing, zdPointing, azSource, zdSource);
		
		assertTrue("Calculated position for source in (zd=0.6 degree ,az=0.0) for pointing in(zd=0.0 degree,az=0.0) is wrong\n"
				+ "Calculated position: ("+r[0]+","+r[1]+")\n"
				+ "sought position: (51.20983219603325,-0.0)", (Math.abs(r[0]-51.20983219603325)<1E-14 && Math.abs(r[1]-0.0)<1E-14));		
	}
}
