/**
 * 
 */
package fact.viewer.ui;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.CsvStream;
import stream.io.SourceURL;
import fact.Constants;

/**
 * @author chris
 * 
 */
public class DefaultPixelMapping implements PixelMapping {

	static Logger log = LoggerFactory.getLogger(DefaultPixelMapping.class);

	static private int[] chId2softId;
	static private int[] software2chId;
	static private int[] chid2geomXmm;
	static private int[] chid2geomYmm;
	static private float[] chid2posYmm;
	static private float[] chid2posXmm;
	// one tile can only have <= 6 neighbours
//	public static int[][] neighboursFromSoftId;

	// one tile can only have <= 6 neighbours
	public static int[][] neighboursFromChId;

	// 
	static boolean init = false;
	// x, y
	final static private int[][] geomId2Chid = new int[44 + 2][39 + 2];

	public DefaultPixelMapping() {
		init();
	}
	
	private static void init(){
		try {
			load("fact-map.txt");
			calculateNeighboursFromChids();
			init = true;
		} catch (Exception e) {
			log.error("Failed to load pixel-map: {}", e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
	}

	private static void load(String map) throws Exception {
		int minX = 0;
		int maxX = 0;
		int minY = 0;
		int maxY = 0;

		chId2softId = new int[1440];
		software2chId = new int[1440];
		chid2geomXmm = new int[1440];
		chid2geomYmm = new int[1440];
		chid2posXmm = new float[1440];
		chid2posYmm = new float[1440];
		URL mapping = CameraPixelMap.class.getResource("/" + map);

		log.debug("Loading pixel-mapping from {}", mapping);
		CsvStream stream = new CsvStream(new SourceURL(mapping), "\\s+");
		stream.init();
		Data item = stream.readNext();
		while (item != null) {
			log.trace("{}", item);


			//			Integer id = new Integer(item.get("softID").toString());
			//int id = new Double((Double) item.get("softID")).intValue();
			//int cbpx = new Double((Double) item.get("hardID")).intValue();

			int id = (int)(Double.parseDouble(item.get("softID").toString()));
			int cbpx = (int)(Double.parseDouble(item.get("hardID").toString()));
			//			Integer id = new Double(item.get("softID").toString());
			//			Integer cbpx = new Integer(item.get("hardID").toString());


			int crate = cbpx / 1000;
			int board = (cbpx / 100) % 10;
			int patch = (cbpx / 10) % 10;
			int pixel = (cbpx % 10);

			Integer chId = pixel + 9 * patch + 36 * board + 360 * crate;
			software2chId[id] = chId;
			chId2softId[chId] = id;


			//  int x = new Double((Double) item.get("geom_i")).intValue();
			//	int y = new Double((Double) item.get("geom_j")).intValue();


			int geomX = (int)(Double.parseDouble(item.get("geom_i").toString()));
			int geomY = (int)(Double.parseDouble(item.get("geom_j").toString()));

			/**
			 * using a 2d array to map geomIds to softids. geoIds go from -22 -
			 * +22 for xValues and from -19 - +20 for yValues xIds from -22 to
			 */
			int iX, iY;

			iX = geomX + 22;
			iY = geomY + 20;
			geomId2Chid[iX][iY] = chId;

			if (geomX < minX)
				minX = geomX;

			if (geomX > maxX)
				maxX = geomX;

			if (geomY < minY)
				minY = geomY;

			if (geomY > maxY)
				maxY = geomY;

			chid2geomXmm[chId] = geomX;
			chid2geomYmm[chId] = geomY;

			float posX = new Float(item.get("pos_X").toString());
			float posY = new Float(item.get("pos_Y").toString());
			chid2posXmm[chId] = posX * 9.5f;
			chid2posYmm[chId] = posY * 9.5f;
			// log.info("softId " + id + " = ( {}, {} )", x, y);

			// HexTile cell = addCell( id, x, y );
			// cell.setId( id );
			item = stream.readNext();
		}

		log.trace(" x range is {}, {}", minX, maxX);
		log.trace(" y range is {}, {}", minY, maxY);
	}
	
	//get the pixel under the following x,y  values which are given in millimeters
	public static int geomToChid(float x, float y){
		if(!init){
			init();
		}
		x = x/9.5f;
		y = y/9.5f;
		float ix =  (float) (x /Math.sin(60* (Math.PI/180)));
		ix =  Math.round(ix);
		float iy = y;
		if(ix % 2 == 0){
			iy = y-0.5f;
		}
		iy = -Math.round(iy);
		
		int chid = 0;
		for(float kx : chid2geomXmm){
			if(kx == ix ){
				if(chid2geomYmm[chid] == iy){
					break;
				}
			}
			chid++;
		}
		if(chid == 1440){
			return -1;
		}
		return chid;
	}

	/**
	 * @see fact.viewer.ui.PixelMapping#sortPixels(short[], short[])
	 */
	@Override
	public double[][] sortPixels(double[] data) {

		int roi = data.length / 1440;
		double[][] pixels = new double[1440][roi];

		for (int pix = 0; pix < 1440; pix++) {
			for (int slice = 0; slice < roi; slice++) {
				int hwId = software2chId[pix];
				int pixId = hwId * roi + slice;
				double value = data[pixId];
				pixels[pix][slice] = value;
			}
		}

		return pixels;
	}

	private static void calculateNeighboursFromChids() {
		neighboursFromChId  = new int[Constants.NUMBEROFPIXEL][6];
//		neighboursFromSoftId = new int[Constants.NUMBEROFPIXEL][6];
		for (int chid = 0; chid < Constants.NUMBEROFPIXEL; chid++) {
//			int chid = software2chId[softId];
			int x = chid2geomXmm[chid];
			int y = chid2geomYmm[chid];
			int iX, iY;

			iX = x + 22;
			iY = y + 20;

			int[] neighbours = new int[6];
			neighbours[0] = check(x, y - 1) ? geomId2Chid[iX][iY - 1] : -1;
			neighbours[1] = check(x, y + 1) ? geomId2Chid[iX][iY + 1] : -1;
			neighbours[2] = check(x - 1, y) ? geomId2Chid[iX - 1][iY] : -1;
			neighbours[4] = check(x + 1, y) ? geomId2Chid[iX + 1][iY] : -1;
			if (x % 2 == 0) {
				neighbours[3] = check(x - 1, y - 1) ? geomId2Chid[iX - 1][iY - 1]
						: -1;
				neighbours[5] = check(x + 1, y - 1) ? geomId2Chid[iX + 1][iY - 1]
						: -1;
			} else {
				neighbours[3] = check(x - 1, y + 1) ? geomId2Chid[iX - 1][iY + 1]
						: -1;
				neighbours[5] = check(x + 1, y + 1) ? geomId2Chid[iX + 1][iY + 1]
						: -1;
			}

			neighboursFromChId[chid] = neighbours;
		}

	}

	private static boolean check(int x, int y) {
		// check for camera borders
		if (Math.abs(x) > 22 || y < -19 || y > 20) {
			return false;
		}
		if ((x != 0 || y != 0) && geomId2Chid[x + 22][y + 20] == 0) {
			return false;
		}

		return true;
	}
	
	/**
	 * Takes a pixel and rotates the geometric pixel coordinates by the given angle 
	 * @param chid
	 */
	public static double[] rotate(int chid , double phi){
		double x =  getGeomX(chid)*Math.cos(phi) - getGeomY(chid) * Math.sin(phi) ;
		double y =  getGeomX(chid)*Math.sin(phi) + getGeomY(chid) * Math.cos(phi) ;
//		System.out.println("rotating  x old: " + getGeomX(chid) + "    new x :" + x );
//		System.out.println("rotating  y old: " + getGeomY(chid) + "    new y :" + y );
		double[] c = {x,y};
		return c;
	}
	
	public static int getChidFromSoftId(Integer softId) {
		if(!init){
			init();
		}
		return software2chId[softId];
	}

	public static int getSoftwareID(Integer chid) {
		if(!init){
			init();
		}
		return chId2softId[chid];
	}
/**
 * 
 * @param chid
 * @return the geometric X value. This is the abstract position shifted by 22 pixelunits.
 */
	public static int getGeomX(Integer chid) {
		if(!init){
			init();
		}
		return chid2geomXmm[chid];
	}
	public static int getGeomY(Integer chid) {
		if(!init){
			init();
		}
		return chid2geomYmm[chid];
	}
	
	
	/**
	 * 
	 * @param chid
	 * @return the x coordinate of the pixel in mm. As seen from the camera coordinate system
	 */
	public static float getPosX(Integer chid) {
		if(!init){
			init();
		}
		return chid2posXmm[chid];
	}
	public static float getPosY(Integer chid) {
		if(!init){
			init();
		}
		return chid2posYmm[chid];
	}
	
	

	public static int[] getNeighborsFromChid(int chid) {
		if(!init){
			init();
		}
		return neighboursFromChId[chid];
	}
	
	
	public static float[] getGeomXArray() {
		if(!init){
			init();
		}
		return chid2posXmm;
	}

	public static float[] getGeomYArray() {
		if(!init){
			init();
		}
		return chid2posYmm;
	}
	
	public static int getPatch(int chid) {
		return chid / 9;
	}
	
	
}