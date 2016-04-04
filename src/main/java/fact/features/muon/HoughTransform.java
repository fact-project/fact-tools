package fact.features.muon;

import com.google.common.primitives.Ints;
import fact.Constants;
import fact.Utils;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSet;
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
 *
 */


public class HoughTransform implements StatefulProcessor {

    // OutputKeys
    @Parameter(required = false, description = "outputkey for the hough peakness")
    private String peaknessKey = "hough:peakness";
    @Parameter(required = false, description = "outputkey for the hough distance")
    private String distanceKey = "hough:distance";
    @Parameter(required = false, description = "outputkey for the octantsHit parameter")
    private String octantsHitKey = "hough:octants";
    @Parameter(required = false, description = "outputkey for the cleaningPercentage parameter")
    private String cleaningPercentageKey = "hough:cleaningPercentage";
    @Parameter(required = false, description = "outputkey for the ringPercentage parameter")
    private String ringPercentageKey = "hough:ringPercentage";
    @Parameter(required = false, description = "outputkey for the hough pixelset of the best Ring")
    private String bestCircleKey = "hough:Ring";
    @Parameter(required = false, description = "outputkey for x coordinate of the center point of the best ring")
    private String bestXKey = "hough:x";
    @Parameter(required=false, description = "outputkey for y coordinate of the center point of the best ring")
    private String bestYKey = "hough:y";
    @Parameter(required = false, description = "outputkey for the radius of the best ring")
    private String bestRadiusKey = "hough:r";
    @Parameter(required = false, description = "outputkey for pixel chids on the best ring")
    private String bestRingPixelKey = "hough:pixel";



    //InputKeys
    @Parameter(required = true, description = "The Pixelset on which the hough transform is performed, usually the cleaning output")
    private String pixelSetKey;

    @Parameter(required = true, description = "PhotonCharge")
    private String photonChargeKey;
    //If showRingkey == true, the PixelSets for the three best circles are returned for the Viewer
    @Parameter(required = false, description = "if this key is true, the three best rings will be shown in the viewer", defaultValue="false")
    private boolean showRingKey = false;
    //if true the 2D-HoughMatrix for x and y at best Radius is printed on the terminal
    @Parameter(required = false, description = "if this key is true, the Hough Accumulator at the bestR will be printetd on the terminal", defaultValue="false")
    private boolean showMatrixKey = false;

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

    public ArrayList<int[]>[] chid2circles = new ArrayList[Constants.NUMBEROFPIXEL];
    public HashMap<RingId, ArrayList<Integer>> circle2chids = new HashMap<>();

    public final class RingId{
        int ir , ix , iy;
        public RingId(int ir , int ix , int iy){
            this.ir = ir;
            this.ix = ix;
            this.iy = iy;
        }

        public int hashCode(){
            return (ir << 10 ^ ix  << 5 ^ iy);
        }

        public boolean equals(Object o){
            if(!(o instanceof RingId))
                return false;

            RingId k = (RingId) o;

            return (k.ir == ir && k.ix == ix && k.iy == iy);
        }
    }

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        int npix = (Integer) input.get("NPIX");

        int[] cleaningPixel = ((PixelSet) input.get(pixelSetKey)).toIntArray();
        double[] photonCharge = (double[]) input.get(photonChargeKey);


        // generate Hough-Voting-Matrix n:
        double[][][] HoughMatrix = new double[res_r + 1][res_x + 1][res_y + 1];

        // HoughTransform:
        int noneZeroElems = 0;

        // Position in parameter space of the three best circles
        int[][] max_positions = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};



        double houghSum = 0;
        for (int chid : cleaningPixel) {
            for (int[] idx : chid2circles[chid]) {

                int r = idx[0];
                int x = idx[1];
                int y = idx[2];

                if (HoughMatrix[r][x][y] == 0) {
                    noneZeroElems += 1;
                }

                HoughMatrix[r][x][y] += photonCharge[chid];
                houghSum += photonCharge[chid];
            }
        }

        double houghMaximum = 0;
        for (int r = 0; r < circle_r.length; r++) {
            for (int x = 0; x < circle_x.length; x++) {
                for (int y = 0; y < circle_y.length; y++) {

                    if (HoughMatrix[r][x][y] >= houghMaximum){
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

        for (int i=0; i < best_r.length; i++)
        {
            best_r[i] = circle_r[max_positions[i][0]];
            best_x[i] = circle_x[max_positions[i][1]];
            best_y[i] = circle_y[max_positions[i][2]];
        }

        input.put(bestRadiusKey, best_r[0]);
        input.put(bestXKey, best_x[0]);
        input.put(bestYKey, best_y[0]);

        double paramDistanceSum = calc_hough_distance(best_r, best_x, best_y);
        input.put(distanceKey, paramDistanceSum);


        double peakness = houghMaximum / (houghSum / noneZeroElems);
        input.put(peaknessKey, peakness);

        // Pixels belonging to the best ring:
        RingId bestRing = new RingId(max_positions[0][0], max_positions[0][1], max_positions[0][2]);
        int numPixBestRing = circle2chids.get(bestRing).size();

        int[] bestRingPixel = Ints.toArray(circle2chids.get(bestRing));
        input.put(bestRingPixelKey, bestRingPixel);


        // percentage and octantshit

        double onRingPixel=0;
        double phi=0;
        int octantsHit=0;
        boolean[] octants = {false, false, false, false, false, false, false, false};

        for (int chid: cleaningPixel)
        {
            FactCameraPixel pix = m.getPixelFromId(chid);
            double pix_x = pix.getXPositionInMM();
            double pix_y = pix.getYPositionInMM();

            double distance = euclidean_distance2d(pix_x, pix_y, best_x[0], best_y[0]);

            if(Math.abs(distance - best_r[0]) <= fact.Constants.PIXEL_SIZE)
            {
                onRingPixel += 1;

                phi = Math.atan2(pix_x - best_x[0], pix_y - best_y[0]);
                octants[ (int) (((phi + Math.PI) / (Math.PI / 4))) % 8] = true;
            }
        }

        for(int i=0; i<8; i++)
        {
            if(octants[i])
            {
                octantsHit+=1;
            }
        }

        input.put(octantsHitKey, octantsHit);


        double cleaningPercentage = onRingPixel / cleaningPixel.length;
        double ringPercentage = onRingPixel / numPixBestRing;
        input.put(cleaningPercentageKey, cleaningPercentage);
        input.put(ringPercentageKey, ringPercentage);


        if(showMatrixKey){
            for(int x=0; x < circle_x.length; x++){
                for(int y=0; y < circle_y.length; y++){
                    System.out.print(String.valueOf(HoughMatrix[max_positions[0][0]][x][y])+" ");
                }
                System.out.print("\n");
            }
        }


        if (showRingKey)
        {
            double distance;
            for (int i = 0; i < 3; i++)
            {
                PixelSet CirclePixelSet = new PixelSet();
                for (int pix = 0; pix < npix; pix++)
                {
                    FactCameraPixel p = m.getPixelFromId(pix);
                    double pix_x = p.getXPositionInMM();
                    double pix_y = p.getYPositionInMM();
                    distance = euclidean_distance2d(pix_x, pix_y, best_x[i], best_y[i]);
                    if (Math.abs(distance - best_r[i]) <= fact.Constants.PIXEL_SIZE) {
                        CirclePixelSet.addById(pix);
                    }
                }
                input.put(bestCircleKey + String.valueOf(i+1), CirclePixelSet);
            }
        }

        return input;
    }

    private double calc_hough_distance(
            double[] r,
            double[] x,
            double[] y
            )
    {
        double distance = 0;
        for (int i=0; i < r.length; i++)
        {
            for (int j=0; j < i; j++)
            {
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

        for (int i=0; i<=res_r; i++){
            circle_r[i] = (max_radius - min_radius) * i/res_r + min_radius;
        }
        for (int i=0; i<=res_x; i++){
            circle_x[i] = (max_x - min_x) * i/res_x + min_x;
        }
        for (int i=0; i<=res_y; i++){
            circle_y[i] = (max_y - min_y) * i/res_y + min_y;
        }

        for (int chid = 0; chid < Constants.NUMBEROFPIXEL; chid++){
            chid2circles[chid] = new ArrayList<>();
        }

        for (int chid = 0; chid < Constants.NUMBEROFPIXEL; chid++){
            FactCameraPixel pix = m.getPixelFromId(chid);
            double pix_x = pix.getXPositionInMM();
            double pix_y = pix.getYPositionInMM();
            for (int r = 0; r < circle_r.length; r++) {
                for (int x = 0; x < circle_x.length; x++) {
                    for (int y = 0; y < circle_y.length; y++) {
                        double distance = euclidean_distance2d(pix_x, pix_y, circle_x[x], circle_y[y]);
                        if (Math.abs(distance - circle_r[r]) <= fact.Constants.PIXEL_SIZE) {
                            int[] idx = {r, x, y};
                            RingId ring = new RingId(r, x, y);
                            chid2circles[chid].add(idx);

                            if (circle2chids.get(ring) == null){
                                circle2chids.put(ring, new ArrayList<Integer>());
                            }
                            circle2chids.get(ring).add(chid);
                        }
                    }
                }
            }
        }


    }

    private double euclidean_distance2d(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow((x1 - x2), 2.0) + Math.pow((y1 - y2), 2.0));
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    public void setDistanceKey(String distanceKey) {
        this.distanceKey = distanceKey;
    }
    public void setPeaknessKey(String peaknessKey) {
        this.peaknessKey = peaknessKey;
    }
    public void setBestCircleKey(String bestCircleKey) {
        this.bestCircleKey = bestCircleKey;
    }
    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }
    public void setPhotonChargeKey(String photonChargeKey) {
        this.photonChargeKey = photonChargeKey;
    }
    public void setoctantsHitKey(String octantsHitKey) {
        this.octantsHitKey = octantsHitKey;
    }
    public void setBestRingPixelKey(String bestRingPixelKey) {
        this.bestRingPixelKey = bestRingPixelKey;
    }
    public void setShowRingKey(boolean showRingKey) {
        this.showRingKey = showRingKey;
    }
    public void setBestXKey(String bestXKey) {
        this.bestXKey = bestXKey;
    }

    public void setBestYKey(String bestYKey) {
        this.bestYKey = bestYKey;
    }
    public void setBestRadiusKey(String bestRadiusKey) {
        this.bestRadiusKey = bestRadiusKey;
    }
    public void setShowMatrixKey(boolean showMatrixKey) {
        this.showMatrixKey = showMatrixKey;
    }

    public void setCleaningPercentageKey(String cleaningPercentageKey) {
        this.cleaningPercentageKey = cleaningPercentageKey;
    }

    public void setRingPercentageKey(String ringPercentageKey) {
        this.ringPercentageKey = ringPercentageKey;
    }

}