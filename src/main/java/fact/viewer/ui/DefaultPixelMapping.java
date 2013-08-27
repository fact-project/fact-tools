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
	public static int[][] neighboursFromSoftId;

	// one tile can only have <= 6 neighbours
	public static int[][] neighboursFromChId;

	// 
	static boolean init = false;
	// x, y
	final static private int[][] geomId2SoftId = new int[44 + 2][39 + 2];

	public DefaultPixelMapping() {
		init();
	}
	
	private static void init(){
		try {
			load("fact-map.txt");
			calculateNeighboursFromSoftIds();
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


			int x = (int)(Double.parseDouble(item.get("geom_i").toString()));
			int y = (int)(Double.parseDouble(item.get("geom_j").toString()));

			/**
			 * using a 2d array to map geomIds to softids. geoIds go from -22 -
			 * +22 for xValues and from -19 - +20 for yValues xIds from -22 to
			 */
			int iX, iY;

			iX = x + 22;
			iY = y + 20;
			geomId2SoftId[iX][iY] = id;

			if (x < minX)
				minX = x;

			if (x > maxX)
				maxX = x;

			if (y < minY)
				minY = y;

			if (y > maxY)
				maxY = y;

			chid2geomXmm[chId] = x;
			chid2geomYmm[chId] = y;

			float posX = new Float(item.get("pos_X").toString());
			float posY = new Float(item.get("pos_Y").toString());
			chid2posXmm[chId] = posX * 10;
			chid2posYmm[chId] = posY * 10;
			// log.info("softId " + id + " = ( {}, {} )", x, y);

			// HexTile cell = addCell( id, x, y );
			// cell.setId( id );
			item = stream.readNext();
		}

		log.trace(" x range is {}, {}", minX, maxX);
		log.trace(" y range is {}, {}", minY, maxY);
	}

	/**
	 * @see fact.viewer.ui.PixelMapping#sortPixels(short[], short[])
	 */
	@Override
	public float[][] sortPixels(float[] data) {

		int roi = data.length / 1440;
		float[][] pixels = new float[1440][roi];

		for (int pix = 0; pix < 1440; pix++) {
			for (int slice = 0; slice < roi; slice++) {
				int hwId = software2chId[pix];
				int pixId = hwId * roi + slice;
				float value = data[pixId];
				pixels[pix][slice] = value;
			}
		}

		return pixels;
	}

	private static void calculateNeighboursFromSoftIds() {
		neighboursFromChId  = new int[Constants.NUMBEROFPIXEL][6];
		neighboursFromSoftId = new int[Constants.NUMBEROFPIXEL][6];
		for (int softId = 0; softId < Constants.NUMBEROFPIXEL; softId++) {
			int x = chid2geomXmm[software2chId[softId]];
			int y = chid2geomYmm[software2chId[softId]];
			int iX, iY;

			iX = x + 22;
			iY = y + 20;

			int[] neighbours = new int[6];
			neighbours[0] = check(x, y - 1) ? geomId2SoftId[iX][iY - 1] : -1;
			neighbours[1] = check(x, y + 1) ? geomId2SoftId[iX][iY + 1] : -1;
			neighbours[2] = check(x - 1, y) ? geomId2SoftId[iX - 1][iY] : -1;
			neighbours[4] = check(x + 1, y) ? geomId2SoftId[iX + 1][iY] : -1;
			if (x % 2 == 0) {
				neighbours[3] = check(x - 1, y - 1) ? geomId2SoftId[iX - 1][iY - 1]
						: -1;
				neighbours[5] = check(x + 1, y - 1) ? geomId2SoftId[iX + 1][iY - 1]
						: -1;
			} else {
				neighbours[3] = check(x - 1, y + 1) ? geomId2SoftId[iX - 1][iY + 1]
						: -1;
				neighbours[5] = check(x + 1, y + 1) ? geomId2SoftId[iX + 1][iY + 1]
						: -1;
			}

			int[] chidNeighbours = new int[6];
			for (int i = 0; i < 6; i++) {
				chidNeighbours[i] = (neighbours[i] != -1) ? software2chId[neighbours[i]]
						: -1;
			}
			neighboursFromChId[software2chId[softId]] = chidNeighbours;
			neighboursFromSoftId[softId] = neighbours;
		}

	}

	private static boolean check(int x, int y) {
		// check for camera borders
		if (Math.abs(x) > 22 || y < -19 || y > 20) {
			return false;
		}
		if ((x != 0 || y != 0) && geomId2SoftId[x + 22][y + 20] == 0) {
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
	
	public static int getChidID(Integer softId) {
		if(!init){
			init();
		}
		return software2chId[softId];
	}

	public static int getSoftwareID(Integer hardId) {
		if(!init){
			init();
		}
		return chId2softId[hardId];
	}

	public static int getGeomX(Integer hardId) {
		if(!init){
			init();
		}
		return chid2geomXmm[hardId];
	}

	public static int getGeomY(Integer hardId) {
		if(!init){
			init();
		}
		return chid2geomYmm[hardId];
	}

	public static int[] getNeighborsFromChid(int chid) {
		if(!init){
			init();
		}
		return neighboursFromChId[chid];
	}

	public static int[] getNeighborsFromSoftID(int softID) {
		if(!init){
			init();
		}
		return neighboursFromSoftId[softID];
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
	
	
}