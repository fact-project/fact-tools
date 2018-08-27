/**
 *
 */
package fact.hexmap;

import fact.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.net.URL;
import java.util.ArrayList;

/**
 * This class provides a mapping between different Pixel ids and geometric information from the
 * camera layout.
 * <p>
 * This class cen get instatiated as a singleton with the getInstance() method.
 * <p>
 * The geometric coordinates stored in the text file to build this map are stored in the "odd -q" vertical layout
 * See http://www.redblobgames.com/grids/hexagons/ for details and pictures.
 * <p>
 * The coordinates are offset by 22 on the x-axis and by 19 on the y-axis
 *
 * @author Kai
 */
public class FactPixelMapping implements PixelMapping {
    static Logger log = LoggerFactory.getLogger(FactPixelMapping.class);

    //store each pixel by its 'geometric' or axial coordinate.
    private final CameraPixel[][] offsetCoordinates = new CameraPixel[45][40];
    public final CameraPixel[] pixelArray = new CameraPixel[Constants.N_PIXELS];
    private final int[] chId2softId = new int[Constants.N_PIXELS];
    private final int[] software2chId = new int[Constants.N_PIXELS];

    private final int[][][] neighbourOffsets = {
            {{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {0, 1}}, //even
            {{1, 1}, {1, 0}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}}  //pixel with a uneven x coordinate
    };

    private final int[][][] neighbourOffsetsLarge = {
            {{-2, -1}, {-2, 0}, {-2, 1}, {-1, -2}, {-1, 1}, {0, -2}, {0, 2}, {1, -2}, {1, 1}, {2, -1}, {2, 0}, {2, 1}},
            {{-2, -1}, {-2, 0}, {-2, 1}, {-1, -1}, {-1, 2}, {0, -2}, {0, 2}, {1, -1}, {1, 2}, {2, -1}, {2, 0}, {2, 1}}
    };
    private int xOffset = 22;
    private int yOffset = 19;

    private static FactPixelMapping mapping;

    public static FactPixelMapping getInstance() {
        if (mapping == null) {
            URL map = FactPixelMapping.class.getResource("/pixel-map.csv");
            mapping = new FactPixelMapping(map);
        }
        return mapping;
    }

    private FactPixelMapping(URL mappingURL) {
        if (mappingURL == null) {
            log.error("Could not find the getColorFromValue. URL was null");
        }
        if (mappingURL != null) {
            if (mappingURL.getFile().isEmpty()) {
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
     * Get the CameraPixel sitting below the coordinates passed to the method.
     * The center of the coordinate system in the camera is the center of the camera.
     *
     * @param xCoordinate
     * @param yCoordinate
     * @return The pixel below the point or NULL if the pixels does not exist.
     */
    public CameraPixel getPixelBelowCoordinatesInMM(double xCoordinate, double yCoordinate) {


        // undo coordinate system rotation
        double x = yCoordinate;
        double y = -xCoordinate;

        //get some pixel near the point provided
        //in pixel units
        x /= 9.5;
        y /= -9.5;
        y += 0.5;

        //distance from center to corner
        double size = 1.0 / Math.sqrt(3);

        double axial_q = 2.0 / 3.0 * x / size;
        double axial_r = (Math.sqrt(3.0) / 3.0 * y - x / 3.0) / size;

        int[] roundedPoint = cube_round(new double[]{axial_q, -axial_q - axial_r, axial_r});
        int rx = roundedPoint[0];
        int rz = roundedPoint[2];

        return getPixelFromCubeCoordinates(rx, rz);
    }

    /**
     * Return an array of CameraPixel which are direct neighbours to the pixel ID passed into this method.
     *
     * @param id the id of pixel
     * @return neighbouring Pixels
     */
    public CameraPixel[] getNeighborsFromID(int id) {
        return getNeighborsForPixel(getPixelFromId(id));
    }

    /**
     * Return an array of CameraPixel which direct are neighbours to the pixel passed into this method.
     *
     * @param p the pixel to get the neighbours from
     * @return neighbouring Pixels
     */
    public CameraPixel[] getNeighborsForPixel(CameraPixel p) {
        ArrayList<CameraPixel> l = new ArrayList<>();
        //check if x coordinate is even or not
        int parity = (p.geometricX & 1);
        //get the neighbour in each direction and store them in hte list
        for (int direction = 0; direction <= 5; direction++) {
            int[] d = neighbourOffsets[parity][direction];
            CameraPixel np = getPixelFromOffsetCoordinates(p.geometricX + d[0], p.geometricY + d[1]);
            if (np != null) {
                l.add(np);
            }
        }
        CameraPixel[] t = new CameraPixel[l.size()];
        return l.toArray(t);
    }

    /**
     * Return an array of CameraPixel which are the neighbours of the neighbours without the direct neighbours. These are all pixels with distance 2
     *
     * @param id
     * @return array of CameraPixel; not always the same length
     */
    public CameraPixel[] getSecondOrderNeighboursFromID(int id) {
        return getSecondOrderNeighboursForPixel(getPixelFromId(id));
    }

    /**
     * Return an array of CameraPixel which are the neighbours of the neighbours without the direct neighbours. These are all pixels with distance 2
     *
     * @param p
     * @return array of CameraPixel; not always the same length
     */
    public CameraPixel[] getSecondOrderNeighboursForPixel(CameraPixel p) {
        ArrayList<CameraPixel> l = new ArrayList<>();
        //check if x coordinate is even or not
        int parity = (p.geometricX & 1);
        //get the neighbour in each direction and store them in hte list
        for (int direction = 0; direction < 12; direction++) {
            int[] d = neighbourOffsetsLarge[parity][direction];
            CameraPixel np = getPixelFromOffsetCoordinates(p.geometricX + d[0], p.geometricY + d[1]);
            if (np != null) {
                l.add(np);
            }
        }
        CameraPixel[] t = new CameraPixel[l.size()];
        return l.toArray(t);
    }

    /* Return an array which contains the three cube coordinates of a pixel.
     * (Cube coordinates: x,y,z -> three axes in an angle of 120 deg)
     */
    public int[] getCubeCoordinatesFromId(int id) {
        int[] cube = new int[3];
        int col = getPixelFromId(id).geometricX;
        int row = getPixelFromId(id).geometricY;

        int x, y, z;

        x = col;
        z = row - (col - Math.abs(col % 2)) / 2;
        y = -x - z;


        cube[0] = x;
        cube[1] = y;
        cube[2] = z;

        return cube;
    }

    public CameraPixel getPixelFromCubeCoordinates(long x, long z) {
        int col = (int) x;
        int row = (int) (z + (x - (x & 1)) / 2);

        return getPixelFromOffsetCoordinates(col, row);
    }


    /**
     * Find lines between two pixels on the hexagonal grid. Line here means the 'path' with the minimal number of pixels between these two pixels.
     * The length of the line is calculated as hexagonal distance.
     * Returns an ArrayList containing the IDs of the line-pixel (including end-pixels id1 and id2).
     *
     * @param id1
     * @param id2
     * @return ArrayList<Integer>
     */
    public ArrayList<Integer> line(int id1, int id2) {

        ArrayList<Integer> line = new ArrayList<>();

        int[] cube1 = getCubeCoordinatesFromId(id1);
        int[] cube2 = getCubeCoordinatesFromId(id2);

        int hexDistance = (Math.abs(cube2[0] - cube1[0]) + Math.abs(cube2[1] - cube1[1]) + Math.abs(cube2[2] - cube1[2])) / 2;

        double N = (double) hexDistance;

        for (int i = 0; i <= hexDistance; i++) {
            double[] point = linePoint(cube1, cube2, 1.0 / N * i);
            int[] pixel = cube_round(point);
            CameraPixel linePixel = getPixelFromCubeCoordinates(pixel[0], pixel[2]);
            if (linePixel != null) {
                line.add(linePixel.id);
            }

        }

        return line;
    }

    private double[] linePoint(int[] cube1, int[] cube2, double t) {
        double[] linePoint = new double[3];
        linePoint[0] = (double) cube1[0] + ((double) cube2[0] - (double) cube1[0]) * t;
        linePoint[1] = (double) cube1[1] + ((double) cube2[1] - (double) cube1[1]) * t;
        linePoint[2] = (double) cube1[2] + ((double) cube2[2] - (double) cube1[2]) * t;

        return linePoint;
    }

    private int[] cube_round(double[] linePoint) {
        int rx = (int) Math.round(linePoint[0]);
        int ry = (int) Math.round(linePoint[1]);
        int rz = (int) Math.round(linePoint[2]);

        double x_diff = Math.abs(rx - linePoint[0]);
        double y_diff = Math.abs(ry - linePoint[1]);
        double z_diff = Math.abs(rz - linePoint[2]);

        if (x_diff > y_diff && x_diff > z_diff) {
            rx = -ry - rz;
        } else if (y_diff > z_diff) {
            ry = -rx - rz;
        } else {
            rz = -rx - ry;
        }

        int[] linePixel = new int[3];
        linePixel[0] = rx;
        linePixel[1] = ry;
        linePixel[2] = rz;

        return linePixel;
    }


    /**
     * Takes a data item containing a row from the mapping file.
     *
     * @return a pixel with the info from the item
     */
    private CameraPixel getPixelFromCSVItem(Data item) {
        CameraPixel p = new CameraPixel();
        p.setSoftID((Integer) (item.get("softID")));
        p.setHardID((Integer) (item.get("hardID")));
        p.geometricX = (Integer) (item.get("geom_i"));
        p.geometricY = (Integer) (item.get("geom_j"));

        double x = Float.parseFloat(item.get("pos_X").toString());
        double y = Float.parseFloat(item.get("pos_Y").toString());

        // rotate camera by 90 degrees to have the following coordinate definition:
        // When looking from the telescope dish onto the camera, x points right, y points up
        p.posX = -y;
        p.posY = x;

        return p;
    }

    /**
     * This expects a file containing information on 1440 Pixel
     *
     * @param mapping url to the mapping file
     */
    private void load(URL mapping) {

        // use the csv stream to read stuff from the csv file
        CsvStream stream = null;
        try {
            stream = new CsvStream(new SourceURL(mapping), ",");
            stream.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // we should sort this by chid
        for (int i = 0; i < Constants.N_PIXELS; i++) {
            Data item = null;
            try {
                item = stream.readNext();
            } catch (Exception e) {
                log.error(e.toString());
            }
            CameraPixel p = getPixelFromCSVItem(item);

            software2chId[p.softid] = p.chid;
            chId2softId[p.chid] = p.softid;

            offsetCoordinates[p.geometricX + xOffset][p.geometricY + yOffset] = p;
            pixelArray[p.id] = p;

        }
    }

    @Override
    public CameraPixel getPixelFromOffsetCoordinates(int x, int y) {
        if (x + xOffset > 44 || y + yOffset >= 40) {
            return null;
        }
        if (x + xOffset < 0 || y + yOffset < 0) {
            return null;
        }
        return offsetCoordinates[x + xOffset][y + yOffset];
    }


    @Override
    public int getNumberOfPixel() {
        return Constants.N_PIXELS;
    }

    @Override
    public CameraPixel getPixelFromId(int id) {
        return pixelArray[id];
    }

    public int getChidFromSoftID(int softid) {
        return software2chId[softid];
    }

    public int getSoftIDFromChid(int chid) {
        return chId2softId[chid];
    }

}
