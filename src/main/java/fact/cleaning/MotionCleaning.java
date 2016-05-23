package fact.cleaning;

import fact.container.PixelSet;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * MotionCleaning:
 * Compares the camera image on different points of time (slices) by calculating the difference between two slices for every pixel.
 * Pixels which "move" (showerpixel) have a greater difference between the values of two slices than pixels which contain only noise.
 * The under(left) bound is fixed at slice 25. This should be an image without a visible shower. The upper (right) bound is
 * chosen as the mean maximum of all pixels. At this point of time the shower should be clearly visible. Every slice up to
 * the right bound is compared with the fixed left bound by calculating the difference.
 * If these difference is bigger than a threshold value (default = 30), a counter
 * (accumulativeDifferences) is set +1.
 * After all slices in the window are compared with the left bound, every pixel whose counter
 * is bigger than threshold2 (=2 at the moment) is classified as showerpixel.
 *
 * Created by lena on 18.08.15.
 */
public class MotionCleaning implements Processor {

    FactPixelMapping map = FactPixelMapping.getInstance();

    int roi = 300;


    @Parameter(required = true)
    private String outputKey;

    @Parameter(required = true)
    private String numPixelKey;

    @Parameter(required = false, description="minimal difference between the values of two slices to set the accumulative difference to 1", defaultValue="30")
    protected double threshold = 30;         //<------------------------------------hardcode: mc-pedestal: 30--------------------------

    @Override
    public Data process(Data data) {

        double[] data_array = (double[]) data.get("DataCalibrated");
        int[] accumulativeDifferences = new int[1440];
        int [] cleaningID = new int[1440];

        int numShowerPixel;


        /*positionMax: position (slice number) of the maximum mean amplitude, differences are calculated up to this slice
        * other idea: BrightnessStatistics.java calculates the position of max amplitude for each pixel ("AmplitudePixel"),
        * could be used as bound
         */
        int min = 50;
        int max = 120;
        int positionMax = positionOfMaxMeanAmplitude(data_array, min, max);

        //System.out.println(maxAmplitude);

        for(FactCameraPixel p : map.pixelArray){
            double[] pixelData = p.getPixelData(data_array, roi);
            accumulativeDifferences[p.id] = 0;


            double ref = pixelData[25];         //  <------------------------------------------hardcode---------------



            for(int i=25; i<=positionMax; i++){
                double diff = pixelData[i] - ref;

                if(diff > threshold){             //  <----------------------------------- threshold 1 -----------------
                    accumulativeDifferences[p.id] += 1;

                }
            }

            if(accumulativeDifferences[p.id] > 2){ // <--------------------------threshold2-----hardcode---------------
                cleaningID[p.id] = 0;
            }
            else {
                cleaningID[p.id] = -2;
            }
        }

        //cosmetic: remove isolated pixels and fill holes in the shower
        cleaningID = removeIsolatedPixel(cleaningID);
        cleaningID = fillHoles(cleaningID);

        int[] showerPixelArray = makeShowerPixelArray(cleaningID);

        //PixelSet (HashSet) contains all pixel from showerPixelArray
        PixelSet pixelSet = new PixelSet();

        //Array -> PixelSet
        if(showerPixelArray != null){
            numShowerPixel = showerPixelArray.length;
            for (int id : showerPixelArray) {
                pixelSet.addById(id);
            }
        }
        else{
            numShowerPixel = 0;
            pixelSet.add(null);

        }

        //get xy-positions of all showerpixel (useful for arrivaltime cleaning)
/*        double[] showerPixel_X = getShowerPixel_X(showerPixelArray);
        double[] showerPixel_Y = getShowerPixel_Y(showerPixelArray);*/


        //data.put("accDiff", accumulativeDifferences); //could be used as feature for machine learner cleaning(???)
        data.put(outputKey, pixelSet);
        //data.put("showerPixel_X", showerPixel_X);
        //data.put("showerPixel_Y", showerPixel_Y);
        data.put(numPixelKey, numShowerPixel);
        data.put("Cleaning", cleaningID);

        return data;
    }


    //calculates the maximum of the mean over all pixels. Used for the upper(right) bound for the "difference-window"
    public int positionOfMaxMeanAmplitude(double [] data, int min, int max){
        double tempValue = 0;
        int tempPosition = min;
        double brightnessMean = -1;

        for(int slice = min; slice<max; slice++){

            for(int i=0;i<1440; i++){
                brightnessMean += data[i*roi + slice]/roi;
            }
            if(brightnessMean > tempValue){
                tempValue = brightnessMean;
                tempPosition = slice;
            }
        }
        return tempPosition;
    }

    public int[] removeIsolatedPixel(int[] clusterID) {
        for (int i = 0; i < 1440; i++) {
            if (clusterID[i] == 0) {
                int numberNeighbours = 0;
                FactCameraPixel[] neighbours = map.getNeighboursFromID(i);
                for (FactCameraPixel n:neighbours) {
                    if (clusterID[n.id] == 0) {
                        numberNeighbours++;
                    }
                }
                if (numberNeighbours == 0) {
                    clusterID[i] = -2;
                }
            }
        }
        return clusterID;
    }

    public int [] fillHoles(int[] clusterID){
        for (int i = 0; i < 1440; i++) {
            if(clusterID[i] == -2){
                int numberNeighbours = 0;
                FactCameraPixel[] neighbours = map.getNeighboursFromID(i);
                for(FactCameraPixel n : neighbours){
                    if(clusterID[n.id] == 0){
                        numberNeighbours++;
                    }
                }
                if(numberNeighbours >= neighbours.length-1){
                    clusterID[i] = 0;
                }
            }
        }
        return clusterID;
    }

    //returns an array with all showerpixel IDs. If no pixel survived the cleaning, returns null
    public int [] makeShowerPixelArray(int[] cleaningID){
        ArrayList<Integer> showerPixel= new ArrayList<>();
        for( int i=0; i<1440; i++){
            if(cleaningID[i] == 0){
                showerPixel.add(i);
            }
        }
        int[] showerPixelArray;
        if(showerPixel.size() > 0){
            showerPixelArray = new int[showerPixel.size()];
            for(int i = 0; i < showerPixel.size(); i++){
                showerPixelArray[i] =  showerPixel.get(i);
            }
        }
        else {
            showerPixelArray = null;
        }
        return showerPixelArray;
    }

    // returns an array with all x-position of the showerpixels
    public double[] getShowerPixel_X(int[] showerPixelArray){
        double[] showerPixel_X;
        if(showerPixelArray == null){
            showerPixel_X = null;
        }
        else {
            showerPixel_X = new double[showerPixelArray.length];
            for (int i = 0; i < showerPixelArray.length; i++) {
                showerPixel_X[i] = map.getPixelFromId(showerPixelArray[i]).posX;
            }
        }
        return showerPixel_X;
    }
    // returns an array with all y-position of the showerpixels
    public double[] getShowerPixel_Y(int[] showerPixelArray) {
        double[] showerPixel_Y;
        if(showerPixelArray == null){
            showerPixel_Y = null;
        }
        else{
            showerPixel_Y = new double[showerPixelArray.length];
            for (int i = 0; i < showerPixelArray.length; i++) {
                showerPixel_Y[i] = map.getPixelFromId(showerPixelArray[i]).posY;
            }
        }
        return showerPixel_Y;
    }



    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
    public void setNumPixelKey(String numPixelKey) {this.numPixelKey = numPixelKey; }
}