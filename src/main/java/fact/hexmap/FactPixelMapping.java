/**
 * 
 */
package fact.hexmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.net.URL;
import java.util.ArrayList;

/**
 * The geometric coordinates stored in the text file to build this map are stored in the "odd -q" vertical layout
 * See http://www.redblobgames.com/grids/hexagons/ for details and pictures.
 *
 * The coordinates are offset by 22 on the x-axis and by 19 on the y-axis
 *
 * @author Kai
 * 
 */
public class FactPixelMapping implements PixelMapping {

    //store each pixel by its 'geometric' or axial coordinate.
    private final FactCameraPixel[][] offsetCoordinates = new FactCameraPixel[45][40];
    public final FactCameraPixel[] pixelArray = new FactCameraPixel[1440];
    private final int[] chId2softId = new int[1440];
    private final int[] software2chId = new int[1440];

    private final int[][][] neighbourOffsets = {
            {{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {0, 1}}, //uneven
            {{1, 1}, {1, 0}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}}  //pixel with a even x coordinate
    };

    // up,down,topleft,topright,botleft,botright
    // x even , x uneven
    private final int[][][] neighbourOffsetsDirectional = {
    		{{0, -1}, {0, +1}, {-1, -1}, {+1, -1}, {-1,  0}, {+1,  0}} , //even
            {{0, -1}, {0, +1}, {-1,  0}, {+1,  0}, {-1, +1}, {+1, +1}}  //pixel with a uneven x coordinate
    };
    
    private int xOffset = 22;
    private int yOffset = 19;


    static Logger log = LoggerFactory.getLogger(FactPixelMapping.class);


    private static FactPixelMapping mapping;

    public static FactPixelMapping getInstance() {
        if (mapping ==  null){
            URL map = FactPixelMapping.class.getResource("/pixel-map.csv");
            mapping = new FactPixelMapping(map);
        }
        return mapping;
    }

    private FactPixelMapping(URL mappingURL) {
        if(mappingURL == null){
            log.error("Could not find the getColorFromValue. URL was null");
        }
        if (mappingURL != null) {
            if(mappingURL.getFile().isEmpty()){
                throw new RuntimeException("Could not find Fact-mapping file");
            }
        }
        load(mappingURL);
    }

    public int getNumberRows() {
        return 45;
    }

    public int getNumberCols() {
        return 40;
    }

    /**
     * Get the FactCameraPixel sitting below the coordinates passed to the method.
     * The center of the coordinate system in the camera is the center of the camera.
     *
     * @param xCoordinate
     * @param yCoordinate
     * @return The pixel below the point or NULL if the pixels does not exist.
     */
    public FactCameraPixel getPixelBelowCoordinatesInMM(double xCoordinate, double yCoordinate){
        //get some pixel near the point provided
        //in pixel units
        xCoordinate /= 9.5;
        yCoordinate /= -9.5;
        yCoordinate += 0.5;

        //if (xCoordinate*xCoordinate + yCoordinate*yCoordinate >= 440){
        //    return null;
        //}
        //distance from center to corner
        double size  = 1.0/Math.sqrt(3);

        double axial_q = 2.0/3.0 * xCoordinate/size;
        double axial_r = (0.5773502693 * yCoordinate - 1.0/3.0 *xCoordinate)/size;


        double cube_x = axial_q;
        double cube_z = axial_r;
        double cube_y = -cube_x-cube_z;


        //now round maybe violating the constraint
        int rx = (int) Math.round(cube_x);
        int rz = (int) Math.round(cube_z);
        int ry = (int) Math.round(cube_y);

        //artificially fix the constraint.
        double x_diff = Math.abs(rx -cube_x);
        double z_diff = Math.abs(rz -cube_z);
        double y_diff = Math.abs(ry -cube_y);

        if(x_diff > y_diff && x_diff > z_diff){
            rx = -ry-rz;
        } else if(y_diff > z_diff){
            ry = -rx-rz;
        } else {
            rz = -rx-ry;
        }


        //now convert cube coordinates back to even-q
        int qd = rx;
        int rd = rz + (rx - (rx&1))/2;

        FactCameraPixel p = getPixelFromOffsetCoordinates(qd, rd);
        return p;



    }
    public FactCameraPixel[] getNeighboursFromID(int id){
        return getNeighboursForPixel(getPixelFromId(id));
    }
    public FactCameraPixel[] getNeighboursForPixel(CameraPixel p) {
        ArrayList<FactCameraPixel> l = new ArrayList<>();
        //check if x coordinate is even or not
        int parity = (p.geometricX & 1);
        //get the neighbour in each direction and store them in hte list
        for (int direction = 0; direction <= 5; direction++) {
            int[] d = neighbourOffsets[parity][direction];
            FactCameraPixel np = getPixelFromOffsetCoordinates(p.geometricX + d[0], p.geometricY + d[1]);
            if (np != null){
                l.add(np);
            }
        }
        FactCameraPixel[] t = new FactCameraPixel[l.size()];
        return l.toArray(t);
    }

    /**
     *  
     * @param p CameraPixel
     * @return Pixel in Order (up,down,topleft,topright,botleft,botright) with null at non existing pixel
     */
    public FactCameraPixel[] getNeighborsForPixelWithDirection(CameraPixel p) 
    {
    	 FactCameraPixel[] t = new FactCameraPixel[6];
    	 
        //check if x coordinate is even or not    	
        int parity = (Math.abs(p.geometricX % 2));
        //get the neighbor in each direction and store them in the list
        for (int direction = 0; direction <= 5; direction++) 
        {
            int[] d = neighbourOffsetsDirectional[parity][direction];
            t[direction] = getPixelFromOffsetCoordinates(p.geometricX + d[0], p.geometricY + d[1]);                
        }       
        return t;
    }
    
    /**
     * Takes a data item containing a row from the mapping file.
     *
     * @return a pixel with the info from the item
     */
    private FactCameraPixel getPixelFromCSVItem(Data item){
        FactCameraPixel p = new FactCameraPixel();
        p.setSoftID( (Integer)(item.get("softID"))  );
        p.setHardid( (Integer)(item.get("hardID"))  );
        p.geometricX = (Integer)(item.get("geom_i"));
        p.geometricY = (Integer)(item.get("geom_j"));
        p.posX = Float.parseFloat(item.get("pos_X").toString());
        p.posY = Float.parseFloat(item.get("pos_Y").toString());

        return p;
    }

    /**
     * This expects a file containing information on 1440 Pixel
     * @param mapping url to the mapping file
     */
	private void load(URL mapping){


        //use the csv stream to read stuff from the csv file
        CsvStream stream = null;
        try {
            stream = new CsvStream(new SourceURL(mapping), ",");
            stream.init();
        } catch (Exception e){
            log.error(e.toString());
        }

        //we should sort this by chid
        for (int i = 0; i < 1440; i++) {
            Data item = null;
            try {
                item = stream.readNext();
            } catch (Exception e) {
                log.error(e.toString());
            }
            FactCameraPixel p = getPixelFromCSVItem(item);

            software2chId[p.softid] = p.chid;
            chId2softId[p.chid] = p.softid;

            offsetCoordinates[p.geometricX + xOffset][p.geometricY + yOffset] = p;
            pixelArray[p.id] = p;

        }
	}

    @Override
    public FactCameraPixel getPixelFromOffsetCoordinates(int x, int y){
        if (x + xOffset > 44 || y + yOffset >= 40){
            return null;
        }
        if (x + xOffset < 0  || y + yOffset <0){
            return null;
        }
        return offsetCoordinates[x +xOffset][y + yOffset];
    }


    @Override
    public int getNumberOfPixel() {
        return 1440;
    }

    @Override
    public FactCameraPixel getPixelFromId(int id) {
        return pixelArray[id];
    }

    public int getChidFromSoftID(int softid){
        return software2chId[softid];
    }
    public int getSoftIDFromChid(int chid){
        return chId2softId[chid];
    }

}