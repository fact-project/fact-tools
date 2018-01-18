package fact.features.muon;

import fact.Constants;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This processor delivers several features that can be used to seperate muon rings from
 * other data using the Hough Transform for circles.
 *
 * @author MaxNoe
 */


public class HoughTransform implements StatefulProcessor {

    // OutputKeys
    @Parameter(required = false, description = "outputkey for the hough peakness")
    public String peaknessKey = "hough:peakness";

    @Parameter(required = false, description = "outputkey for the hough distance")
    public String distanceKey = "hough:distance";

    @Parameter(required = false, description = "outputkey for the octantsHit parameter")
    public String octantsHitKey = "hough:octants";

    @Parameter(required = false, description = "outputkey for the cleaningPercentage parameter")
    public String cleaningPercentageKey = "hough:cleaningPercentage";

    @Parameter(required = false, description = "outputkey for the ringPercentage parameter")
    public String ringPercentageKey = "hough:ringPercentage";

    @Parameter(required = false, description = "outputkey for the hough pixelset of the best Ring")
    public String bestCircleKey = "hough:Ring";

    @Parameter(required = false, description = "outputkey for x coordinate of the center point of the best ring")
    public String bestXKey = "hough:x";

    @Parameter(required = false, description = "outputkey for y coordinate of the center point of the best ring")
    public String bestYKey = "hough:y";

    @Parameter(required = false, description = "outputkey for the radius of the best ring")
    public String bestRadiusKey = "hough:r";

    @Parameter(required = false, description = "outputkey for pixel chids on the best ring")
    public String bestRingPixelKey = "hough:pixel";


    //InputKeys
    @Parameter(required = true, description = "The Pixelset on which the hough transform is performed, usually the cleaning output")
    public String pixelSetKey;

    @Parameter(required = true, description = "PhotonCharge")
    public String photonChargeKey;

    @Parameter(required = false, description = "if this key is true, the three best rings will be shown in the viewer", defaultValue = "false")
    public boolean showRingKey = false;

    @Parameter(required = false, description = "if this key is true, the Hough Accumulator at the bestR will be printetd on the terminal", defaultValue = "false")
    public boolean showMatrixKey = false;

    double min_radius = 40;  // minimal radius in mm
    double max_radius = 120; // maximal  -->radius in mm
    double min_x = -300;     // minimal center X in mm
    double max_x = 300;      // maximal center X in mm
    double min_y = -300;     // minimal center y in mm
    double max_y = 300;      // maximal center y in mm

    // resolution
    int res_r = 24;
    int res_x = 60;
    int res_y = 60;

    final Logger log = LoggerFactory.getLogger(HoughTransform.class);


    FactPixelMapping m = FactPixelMapping.getInstance();
    private double[] circle_y;
    private double[] circle_x;
    private double[] circle_r;

    public ArrayList<ArrayList<int[]>> chid2circles = new ArrayList<>(Constants.N_PIXELS);
    public HashMap<RingId, ArrayList<Integer>> circle2chids = new HashMap<>();

    public final class RingId {
        int ir, ix, iy;

        public RingId(int ir, int ix, int iy) {
            this.ir = ir;
            this.ix = ix;
            this.iy = iy;
        }

        public int hashCode() {
            return (ir << 10 ^ ix << 5 ^ iy);
        }

        public boolean equals(Object o) {
            if (!(o instanceof RingId))
                return false;

            RingId k = (RingId) o;

            return (k.ir == ir && k.ix == ix && k.iy == iy);
        }
    }

    @Override
    public Data process(Data item) {
        PixelSet cleaningPixel = (PixelSet) item.get(pixelSetKey);
        double[] photonCharge = (double[]) item.get(photonChargeKey);


        // generate Hough-Voting-Matrix n:
        double[][][] HoughMatrix = new double[res_r + 1][res_x + 1][res_y + 1];

        // HoughTransform:
        int noneZeroElems = 0;

        // Position in parameter space of the three best circles
        int[][] max_positions = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};


        double houghSum = 0;
        for (CameraPixel pixel : cleaningPixel) {
            for (int[] idx : chid2circles.get(pixel.id)) {

                int r = idx[0];
                int x = idx[1];
                int y = idx[2];

                if (HoughMatrix[r][x][y] == 0) {
                    noneZeroElems += 1;
                }

                HoughMatrix[r][x][y] += photonCharge[pixel.id];
                houghSum += photonCharge[pixel.id];
            }
        }

        double houghMaximum = 0;
        for (int r = 0; r < circle_r.length; r++) {
            for (int x = 0; x < circle_x.length; x++) {
                for (int y = 0; y < circle_y.length; y++) {
                    if (HoughMatrix[r][x][y] >= houghMaximum) {
                        houghMaximum = HoughMatrix[r][x][y];
                        int[] idx = {r, x, y};
                        for (int i = 0; i < idx.length; i++) {
                            max_positions[2][i] = max_positions[1][i];
                            max_positions[1][i] = max_positions[0][i];
                            max_positions[0][i] = idx[i];
                        }
                    }

                }
            }
        }


        // Calculate the Features

        // Hough-Distance and Peakness

        double[] best_r = new double[3];
        double[] best_x = new double[3];
        double[] best_y = new double[3];

        for (int i = 0; i < best_r.length; i++) {
            best_r[i] = circle_r[max_positions[i][0]];
            best_x[i] = circle_x[max_positions[i][1]];
            best_y[i] = circle_y[max_positions[i][2]];
        }

        item.put(bestRadiusKey, best_r[0]);
        item.put(bestXKey, best_x[0]);
        item.put(bestYKey, best_y[0]);

        double paramDistanceSum = calc_hough_distance(best_r, best_x, best_y);
        item.put(distanceKey, paramDistanceSum);


        double peakness = houghMaximum / (houghSum / noneZeroElems);
        item.put(peaknessKey, peakness);

        // Pixels belonging to the best ring:
        RingId bestRing = new RingId(max_positions[0][0], max_positions[0][1], max_positions[0][2]);
        int numPixBestRing = circle2chids.get(bestRing).size();

        PixelSet bestRingPixel = new PixelSet();
        for (int chid : circle2chids.get(bestRing)) {
            bestRingPixel.addById(chid);
        }
        item.put(bestRingPixelKey, bestRingPixel);


        // percentage and octantshit

        double onRingPixel = 0;
        double phi = 0;
        int octantsHit = 0;
        boolean[] octants = {false, false, false, false, false, false, false, false};

        for (CameraPixel pix : cleaningPixel) {
            double pix_x = pix.getXPositionInMM();
            double pix_y = pix.getYPositionInMM();

            double distance = euclidean_distance2d(pix_x, pix_y, best_x[0], best_y[0]);

            if (Math.abs(distance - best_r[0]) <= Constants.PIXEL_SIZE_MM) {
                onRingPixel += 1;

                phi = Math.atan2(pix_x - best_x[0], pix_y - best_y[0]);
                octants[(int) (((phi + Math.PI) / (Math.PI / 4))) % 8] = true;
            }
        }

        for (int i = 0; i < 8; i++) {
            if (octants[i]) {
                octantsHit += 1;
            }
        }

        item.put(octantsHitKey, octantsHit);


        double cleaningPercentage = onRingPixel / cleaningPixel.size();
        double ringPercentage = onRingPixel / numPixBestRing;
        item.put(cleaningPercentageKey, cleaningPercentage);
        item.put(ringPercentageKey, ringPercentage);


        if (showMatrixKey) {
            for (int x = 0; x < circle_x.length; x++) {
                for (int y = 0; y < circle_y.length; y++) {
                    System.out.print(String.valueOf(HoughMatrix[max_positions[0][0]][x][y]) + " ");
                }
                System.out.print("\n");
            }
        }


        if (showRingKey) {
            double distance;
            for (int i = 0; i < 3; i++) {
                PixelSet CirclePixelSet = new PixelSet();
                for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
                    CameraPixel p = m.getPixelFromId(pix);
                    double pix_x = p.getXPositionInMM();
                    double pix_y = p.getYPositionInMM();
                    distance = euclidean_distance2d(pix_x, pix_y, best_x[i], best_y[i]);
                    if (Math.abs(distance - best_r[i]) <= Constants.PIXEL_SIZE_MM) {
                        CirclePixelSet.addById(pix);
                    }
                }
                item.put(bestCircleKey + String.valueOf(i + 1), CirclePixelSet);
            }
        }

        return item;
    }

    private double calc_hough_distance(
            double[] r,
            double[] x,
            double[] y
    ) {
        double distance = 0;
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < i; j++) {
                distance += Math.sqrt(Math.pow(r[i] - r[j], 2) + Math.pow(x[i] - x[j], 2) + Math.pow(y[i] - y[j], 2));
            }
        }
        return distance;
    }


    @Override
    public void init(ProcessContext processContext) throws Exception {

        //Fill the parameter space

        circle_r = new double[res_r + 1];
        circle_x = new double[res_x + 1];
        circle_y = new double[res_y + 1];

        for (int i = 0; i <= res_r; i++) {
            circle_r[i] = (max_radius - min_radius) * i / res_r + min_radius;
        }
        for (int i = 0; i <= res_x; i++) {
            circle_x[i] = (max_x - min_x) * i / res_x + min_x;
        }
        for (int i = 0; i <= res_y; i++) {
            circle_y[i] = (max_y - min_y) * i / res_y + min_y;
        }


        for (int chid = 0; chid < Constants.N_PIXELS; chid++) {
            ArrayList<int[]> circles = new ArrayList<>();
            CameraPixel pix = m.getPixelFromId(chid);
            double pix_x = pix.getXPositionInMM();
            double pix_y = pix.getYPositionInMM();
            for (int r = 0; r < circle_r.length; r++) {
                for (int x = 0; x < circle_x.length; x++) {
                    for (int y = 0; y < circle_y.length; y++) {
                        double distance = euclidean_distance2d(pix_x, pix_y, circle_x[x], circle_y[y]);
                        if (Math.abs(distance - circle_r[r]) <= fact.Constants.PIXEL_SIZE_MM) {
                            int[] idx = {r, x, y};
                            RingId ring = new RingId(r, x, y);
                            circles.add(idx);

                            if (circle2chids.get(ring) == null) {
                                circle2chids.put(ring, new ArrayList<>());
                            }
                            circle2chids.get(ring).add(chid);
                        }
                    }
                }
            }
            chid2circles.add(circles);
        }


    }

    private double euclidean_distance2d(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x1 - x2), 2.0) + Math.pow((y1 - y2), 2.0));
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
