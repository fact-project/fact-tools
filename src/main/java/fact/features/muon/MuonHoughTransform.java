package fact.features.muon;

import fact.Utils;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.PixelSetOverlay;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;



/**
 * This processor delivers several features that can be used to seperate muon rings from
 * other data using the Hough Transform for circles.
 * 
 * @author MaxNoe
 * 
 */


public class MuonHoughTransform implements Processor {
    
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
    private String pixelKey; 
    
    @Parameter(required = true, description = "PhotonCharge")
    private String photonChargeKey;
    //If showRingkey == true, the PixelSets for the three best circles are returned for the Viewer
    @Parameter(required = false, description = "if this key is true, the three best rings will be shown in the viewer", defaultValue="false")
    private boolean showRingKey = false;
    //if true the 2D-HoughMatrix for x and y at best Radius is printed on the terminal
    @Parameter(required = false, description = "if this key is true, the Hough Accumulator at the bestR will be printetd on the terminal", defaultValue="false")
    private boolean showMatrixKey = false;
    

    
    final Logger log = LoggerFactory.getLogger(MuonHoughTransform.class);   

    FactPixelMapping m = FactPixelMapping.getInstance();

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        int npix = (Integer) input.get("NPIX");
        
        int[] cleaningPixel = (int[]) input.get(pixelKey);
        double[] photonCharge = (double[]) input.get(photonChargeKey);
                
        double[] xPositions = new double[cleaningPixel.length];
        double[] yPositions = new double[cleaningPixel.length];


        // Defining the parameterspace in which we look for circles:

        double min_radius = 40; //minimal radius in mm
        double max_radius = 120; //maximal  -->radius in mm
        double min_x = -300; //minimal center X in mm
        double max_x = 300; //maximal center X in mm
        double min_y = -300; //minimal center y in mm
        double max_y = 300; //maximal center y in mm

        // resolution
        int res_r = 24;
        int res_x = 60;
        int res_y = 60;
        
        // Get X and Y Positions of the Pixel that survived Cleaning
        
        for(int i=0; i<cleaningPixel.length; i++){
            xPositions[i] = m.getPixelFromId(cleaningPixel[i]).getXPositionInMM();
            yPositions[i] = m.getPixelFromId(cleaningPixel[i]).getYPositionInMM();
        }
        
        // generate Hough-Voting-Matrix n:
        
        double[][][] HoughMatrix = new double[res_r+1][res_x+1][res_y+1];
        
        //Fill the parameter space
        
        double[] circle_radius = new double[res_r+1];
        double[] circle_x = new double[res_x+1];
        double[] circle_y = new double[res_y+1];
        
        for (int i=0; i<=res_r; i++){
            circle_radius[i] = (max_radius - min_radius) * i/res_r + min_radius;
        }
        for (int i=0; i<=res_x; i++){
            circle_x[i] = (max_x - min_x) * i/res_x + min_x;
        }
        for (int i=0; i<=res_y; i++){
            circle_y[i] = (max_y - min_y) * i/res_y + min_y;
        }
        
        // HoughTransform:
        
        
        int NoneZeroElems = 0;
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        //Position in parameter space of the three best circles
        int[][] max_positions = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
                
        for (int r = 0; r < circle_radius.length; r++)
        {
            for (int x = 0; x < circle_x.length; x++)
            {
                for (int y = 0; y < circle_y.length; y++)
                {
                    for(int pix = 0; pix < cleaningPixel.length; pix++)
                    {
                        double distance = Math.sqrt(Math.pow((xPositions[pix] - circle_x[x]), 2.0) + Math.pow((yPositions[pix] - circle_y[y]), 2.0));
                        if(Math.abs(distance - circle_radius[r]) <= fact.Constants.PIXEL_SIZE )
                        {
                            HoughMatrix[r][x][y] += photonCharge[cleaningPixel[pix]];
                        }
                    }
                    stats.addValue(HoughMatrix[r][x][y]);
                    if (HoughMatrix[r][x][y] !=0)
                    {
                        NoneZeroElems += 1;
                    }
                    if (HoughMatrix[r][x][y] >= stats.getMax()) {
                        for (int i=2; i > 0; i--)
                        {
                            for (int j=0; j < 3; j++)
                            {
                                max_positions[i][j] = max_positions[i-1][j];
                            }
                        }
                        max_positions[0][0] = r;
                        max_positions[0][1] = x;
                        max_positions[0][2] = y;
                    }
                }
            }
        }
        

        // Calculate the Features 

        // Hough-Distance and Peakness
        
        double[] best_r = new double[3];
        double[] best_x = new double[3];
        double[] best_y = new double[3];

        for (int i=0; i < 3; i++)
        {
            best_r[i] = circle_radius[max_positions[i][0]];
            best_x[i] = circle_x[max_positions[i][1]];
            best_y[i] = circle_y[max_positions[i][2]];
        }

        input.put(bestRadiusKey, best_r[0]);
        input.put(bestXKey, best_x[0]);
        input.put(bestYKey, best_y[0]);

        double ParamDistanceSum = calc_hough_distance(best_r, best_x, best_y);
        input.put(distanceKey, ParamDistanceSum);


        double HoughMaximum = stats.getMax();
        double HoughSum = stats.getSum();
        
        double peakness = HoughMaximum/(HoughSum/NoneZeroElems);
        input.put(peaknessKey, peakness);
        
        // Pixels belonging to the best ring:
        
        ArrayList<Integer> bestRingPixelList = new ArrayList<Integer>();
        
        int numPixBestRing = 0;
        
        for(int pix=0; pix<npix; pix++){
            FactCameraPixel p  = m.getPixelFromId(pix);
            double PixelPosX = p.getXPositionInMM();
            double PixelPosY = p.getYPositionInMM();
            double distance = Math.sqrt(Math.pow((PixelPosX - best_x[0]), 2.0) + Math.pow((PixelPosY - best_y[0]), 2.0));
            if(Math.abs(distance - best_r[0]) <= fact.Constants.PIXEL_SIZE){
                bestRingPixelList.add(pix); 
                numPixBestRing += 1;
            }
        }
        
        int[] bestRingPixel = new int[bestRingPixelList.size()];
        
        for(int i=0; i < bestRingPixelList.size(); i++){
            bestRingPixel[i] = bestRingPixelList.get(i);
        }
        
        input.put(bestRingPixelKey, bestRingPixel);
        
        
        // percentage and octantshit

        
        double onRingPixel=0;
            double phi=0;
            int octantsHit=0;
            boolean[] octants = {false, false, false, false, false, false, false, false};
            
            for (int pix=0; pix < cleaningPixel.length; pix++)
            {
                double distance = Math.sqrt(Math.pow((xPositions[pix] - best_x[0]), 2.0) + Math.pow((yPositions[pix] - best_y[0]), 2.0));
                if(Math.abs(distance - best_r[0]) <= fact.Constants.PIXEL_SIZE)
                {
                    onRingPixel+=1;

                    phi = Math.atan2(xPositions[pix] - best_x[0], yPositions[pix] - best_y[0]);
                    octants[(int) (((phi+Math.PI)/(Math.PI/4)))%8] = true;
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


        double cleaningPercentage = onRingPixel/cleaningPixel.length;
        double ringPercentage = onRingPixel/numPixBestRing;
        input.put(cleaningPercentageKey, cleaningPercentage);
        input.put(ringPercentageKey, ringPercentage);

        // Creating the Pixelsets for the Viewer
        if(showMatrixKey){
            for(int x=0; x<circle_x.length; x++){
                for(int y=0; y<circle_y.length; y++){
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
                PixelSetOverlay CirclePixelSet = new PixelSetOverlay();
                for (int pix = 0; pix < npix; pix++)
                {
                    FactCameraPixel p = m.getPixelFromId(pix);
                    double PixelPosX = p.getXPositionInMM();
                    double PixelPosY = p.getYPositionInMM();
                    distance = Math.sqrt(Math.pow((PixelPosX - best_x[i]), 2.0) + Math.pow((PixelPosY - best_y[i]), 2.0));
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
        for (int i=0; i <= 2; i++)
        {
            for (int j=0; j < i; j++)
            {
                distance += Math.sqrt(Math.pow(r[i] - r[j], 2) + Math.pow(x[i] - x[j], 2) + Math.pow(y[i] - y[j], 2));
            }
        }
        return Math.sqrt(distance);
    }
    
    public String getDistanceKey() {
        return distanceKey;
    }
    public void setDistanceKey(String distanceKey) {
        this.distanceKey = distanceKey;
    }
    public String getPeaknessKey() {
        return peaknessKey;
    }
    public void setPeaknessKey(String peaknessKey) {
        this.peaknessKey = peaknessKey;
    }
    public String getBestCircleKey() {
        return bestCircleKey;
    }
    public void setBestCircleKey(String bestCircleKey) {
        this.bestCircleKey = bestCircleKey;
    }
    public String getPixelKey() {
        return pixelKey;
    }
    public void setPixelKey(String pixelKey) {
        this.pixelKey = pixelKey;
    }
    public String getPhotonChargeKey() {
        return photonChargeKey;
    }
    public void setPhotonChargeKey(String photonChargeKey) {
        this.photonChargeKey = photonChargeKey;
    }


    public String getoctantsHitKey() {
        return octantsHitKey;
    }


    public void setoctantsHitKey(String octantsHitKey) {
        this.octantsHitKey = octantsHitKey;
    }


    public String getBestRingPixelKey() {
        return bestRingPixelKey;
    }


    public void setBestRingPixelKey(String bestRingPixelKey) {
        this.bestRingPixelKey = bestRingPixelKey;
    }


    public boolean isShowRingKey() {
        return showRingKey;
    }


    public void setShowRingKey(boolean showRingKey) {
        this.showRingKey = showRingKey;
    }


    public String getBestXKey() {
        return bestXKey;
    }


    public void setBestXKey(String bestXKey) {
        this.bestXKey = bestXKey;
    }


    public String getBestYKey() {
        return bestYKey;
    }


    public void setBestYKey(String bestYKey) {
        this.bestYKey = bestYKey;
    }


    public String getBestRadiusKey() {
        return bestRadiusKey;
    }


    public void setBestRadiusKey(String bestRadiusKey) {
        this.bestRadiusKey = bestRadiusKey;
    }


    public boolean isShowMatrixKey() {
        return showMatrixKey;
    }


    public void setShowMatrixKey(boolean showMatrixKey) {
        this.showMatrixKey = showMatrixKey;
    }


    public String getCleaningPercentageKey() {
        return cleaningPercentageKey;
    }

    public void setCleaningPercentageKey(String cleaningPercentageKey) {
        this.cleaningPercentageKey = cleaningPercentageKey;
    }
        public void setRingPercentageKey(String ringPercentageKey) {
        this.ringPercentageKey = ringPercentageKey;
    }


}
