package fact.cleaning;

import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.FellWalker;
import stream.Data;
import stream.Processor;

import java.util.ArrayList;

/**
 * Created by lena on 18.08.15.
 */
public class MotionCleaning implements Processor {

    FactPixelMapping map = FactPixelMapping.getInstance();

    int roi = 300;
    double T = 30;                      //<------------------------------------hardcode: mc-pedestal: 30--------------------------

    @Override
    public Data process(Data data) {

        double[] data_array = (double[]) data.get("DataCalibrated");
        double[] maximum = (double[]) data.get("AmplitudePixel");
        int[] accumulativeDifferences = new int[1440];
        int [] cleaningID = new int[1440];
        //double[] brightness = data.get("")
        //int roi = (Integer) data.get("NROI");
        int npix = (Integer) data.get("NPIX");


        /*positionMax: position (slice number) of the maximum mean amplitude, differences are calculated up to this slice
        * other idea: BrightnessStatistics.java calculates the position of max amplitude for each pixel ("AmplitudePixel"),
        * could be used as boundary
         */
        int min = 50;
        int max = 120;
        int positionMax = positionOfMaxMeanAmplitude(data_array, min, max);

        //System.out.println(positionMax);
        for(FactCameraPixel p : map.pixelArray){
            double[] pixelData = p.getPixelData(data_array, roi);
            accumulativeDifferences[p.id] = 0;

            //double brightnessMax = pixelData[maximum(pixelData, min, max)];

            double ref = pixelData[25];

            for(int i=25; i<=positionMax; i++){   //  <------------------------------------------hardcode------------------
                double diff = pixelData[i] - ref;

                if(diff > T){
                    accumulativeDifferences[p.id] += 1;

                }


                //ref = pixelData[i];
            }

            //System.out.println(accumulativeDifferences[p.id]);

            if(accumulativeDifferences[p.id] > 2){
                cleaningID[p.id] = 0;
            }
            else {
                cleaningID[p.id] = -2;
            }
        }

        cleaningID = removeIsolatedPixel(cleaningID);
        cleaningID = fillHoles(cleaningID);

        int NumCleaningPixel = -FellWalker.countClusterPixel(cleaningID);
        //System.out.println(NumClusterPixel);

            int[] showerPixelArray = makeShowerPixelArray(cleaningID);
            /*System.out.println(showerPixelArray.length);*/
            double[] showerPixel_X = getShowerPixel_X(showerPixelArray);
            double[] showerPixel_Y = getShowerPixel_Y(showerPixelArray);




        //data.put("ClusterID", clusterID);
        //data.put("Diff", differences);
        data.put("AcDiff", accumulativeDifferences);
        data.put("shower", showerPixelArray);
        data.put("showerPixel_X", showerPixel_X);
        data.put("showerPixel_Y", showerPixel_Y);
        data.put("NumClusterPixel", NumCleaningPixel);
        data.put("Cleaning", cleaningID);

        return data;
    }

    public int maximum(double[] pixelData, int min, int max){
        double tempValue = 0;
        int tempMax = min;
        for(int i=min; i<max; i++){
            if(pixelData[i]>tempValue){
                tempMax = i;
            }
        }
        return tempMax;
    }

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
                    if (clusterID[n.id] == 0) {                             //while???
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
            showerPixelArray = new int[1];
            showerPixelArray[0] = -1;
        }
        return showerPixelArray;
    }

    public double[] getShowerPixel_X(int[] showerPixelArray){
        double[] showerPixel_X;
        if(showerPixelArray[0] == -1){
            showerPixel_X = new double[1];
            showerPixel_X[0] = -1000;
        }
        else {
            showerPixel_X = new double[showerPixelArray.length];
            for (int i = 0; i < showerPixelArray.length; i++) {
                showerPixel_X[i] = map.getPixelFromId(showerPixelArray[i]).posX;
            }
        }

        return showerPixel_X;
    }

    public double[] getShowerPixel_Y(int[] showerPixelArray) {
        double[] showerPixel_Y;
        if(showerPixelArray[0] == -1){
            showerPixel_Y = new double[1];
            showerPixel_Y[0] = -1000;
        }
        else{
            showerPixel_Y = new double[showerPixelArray.length];
            for (int i = 0; i < showerPixelArray.length; i++) {
            showerPixel_Y[i] = map.getPixelFromId(showerPixelArray[i]).posY;
        }
    }

        return showerPixel_Y;
    }
}
