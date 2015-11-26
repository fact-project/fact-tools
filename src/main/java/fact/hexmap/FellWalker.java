package fact.hexmap;

import org.apache.commons.lang3.ArrayUtils;
import stream.Data;
import stream.Processor;

import java.util.ArrayList;

/* Watershed algorithm to cluster the camera image.
 * The image is interpreted as "landscape" with hills and valleys, where the photoncharge is used as height of a pixel.
 * FellWalker algorithm clusters the pixels by grouping all pixels which belongs to a hill. The algorithm starts at a pixel
 * and searches in the neighborhood for the highest pixel. From this neighbor it searches for the next higher pixel in the
 * neighborhood and so on, until there is no higher pixel and the top of the hill is reached. Every pixel, which is used
 * during a path to the top is added to a list. If the path ends at the top of a hill, every pixel on this list is marked
 * with the same cluster ID. Every path to a top gets another cluster ID. If a path reaches a pixel which has already a clusterID
 * the path up to this pixel is marked with the same cluster ID, because it would lead to the top of the same hill.
 * After all pixels are used for a path, the whole image is clustered. In the last step all clusters are removed that contains
 * less than 2 (fixed at the moment) cleaning pixels.
 *
 * Created by lena on 05.08.15.
 */
public class FellWalker implements Processor {

    FactPixelMapping mapping = FactPixelMapping.getInstance();

    ArrayList<Integer> aktuellerPfad = new ArrayList<>();


    @Override
    public Data process(Data data) {
        int [] cleaning = ((int[]) data.get("shower"));
        double[] arrivalTime = ((double[]) data.get("ArrtimePos"));
        double[] photoncharge = ((double[]) data.get("photoncharge"));
/*        double cogX = (double) data.get("COGx");
        double cogY = (double) data.get("COGy");*/

        int[] clusterID = new int[1440];


        for(int i=0; i<1440;i++){
            clusterID[i] = 0;
        }


        int startPath = NextStartPixel(clusterID);

        int cluster = 1;


        //FellWalker
        while(startPath != -1){

            int brightestNeighbourID;
            int currentPixel = startPath;
            boolean pathend = false;

            aktuellerPfad.add(startPath);

            while (!pathend) {
                //find neighbours and brightest neighbour
                FactCameraPixel[] allNeighbours = mapping.getNeighboursFromID(currentPixel);

                //find usable neighbours (pixel marked with clusterID = 0 after cleaning)
                ArrayList<FactCameraPixel> usableNeighbours = new ArrayList<>();

                for(FactCameraPixel n: allNeighbours){
                    if(clusterID[n.id] != -2){
                        usableNeighbours.add(n);
                    }
                }


                brightestNeighbourID = findMaxChargeNeighbour(usableNeighbours, currentPixel, photoncharge);

                aktuellerPfad.add(brightestNeighbourID);

                if (brightestNeighbourID == currentPixel) {
                    int brightestNeighbourIDLarge = findMaxChargeLargeNeighbour(currentPixel, photoncharge);

                    if(brightestNeighbourIDLarge != currentPixel){

                        if(clusterID[brightestNeighbourIDLarge] != 0){
                            pathToExistingCluster(clusterID, aktuellerPfad, clusterID[brightestNeighbourIDLarge]);
                            pathend = true;
                        }
                        else {
                             currentPixel = brightestNeighbourIDLarge;
                        }
                    }
                    else {
                        pathToNewCluster(clusterID, aktuellerPfad, cluster);
                        cluster++;
                        pathend = true;
                    }

                } else {
                    if (clusterID[brightestNeighbourID] != 0) {

                        pathToExistingCluster(clusterID, aktuellerPfad, clusterID[brightestNeighbourID]);
                        pathend = true;

                    } else {
                        currentPixel = brightestNeighbourID;
                    }
                }
            }
            startPath = NextStartPixel(clusterID);
        }
        //end FellWalker



        int numCluster = getNumberOfClusters(clusterID);

        int[] clusterNoCleaning = clusterID.clone();


        // remove cluster with no cleaning pixels-------------------------------------
        clusterID = removeClusterCleaning(clusterID, cleaning, numCluster);

        //make successive clusterIDs
        clusterID = renameClusterID(clusterID);



        //---------features for gmma/hadron-separation-----------------------------------
        int numClusterPixel = countClusterPixel(clusterID);

        numCluster = getNumberOfClusters(clusterID);

        if(numCluster != 0){
            //count pixels in every cluster
            int [] clusterSize = getClusterSize(clusterID, numCluster);

            //find the pixel containing max photoncharge
            int[] maxima = findMaxIdInCluster(clusterID, photoncharge, numCluster);

            //sort cluster by size (size means the number of pixels in the cluster). Biggest cluster-> clusterID = 1, smallest cluster -> clusterID = numCluster
            clusterID = sortClusterBySize(clusterSize, clusterID);

            clusterSize = getClusterSize(clusterID, numCluster);

            //ratio of photoncharge in the biggest cluster
            double chargeMaxClusterRatio = chargeInBiggestCluster(photoncharge, clusterID)/clusterCharge(clusterID, photoncharge);

            //count cluster with no neighbors
            int isolatedCluster = isolatedCluster(clusterID, numCluster);

            double clusterSizeStd = sizeStd(clusterID, photoncharge, numCluster);

            double stdArrivaltimeMaxima = stdArrivaltimeMaxima(arrivalTime, maxima, numCluster);

/*            double[] sizeX = sizeAxisX(clusterID, photoncharge, numCluster);
            double[] sizeY = sizeAxisY(clusterID, photoncharge, numCluster);*/
           //int cogID = mapping.getPixelBelowCoordinatesInMM(cogX, cogY).id;




            data.put("ClusterID", clusterID);
            data.put("ClusterNoCleaning", clusterNoCleaning);
            data.put("NumCluster", numCluster);
            data.put("ChargeMax", chargeMaxClusterRatio);
            data.put("SizeCluster1", clusterSize[1]);
            data.put("NumClusterPixel", numClusterPixel);
            data.put("IsolatedCluster", isolatedCluster);
            data.put("StdArrivaltimeMaxima", stdArrivaltimeMaxima);
            data.put("ClusterSizeStd", clusterSizeStd);
        }

        return data;
    }



    //find next pixel without clusterID to start a new path, return ID
    public int NextStartPixel(int [] clusterID) {
        int next;
        int i = 0;
        while(clusterID[i] != 0){
            i++;
            if(i == 1440){
                i = -1;
                break;
            }
            }
        next = i;
        //System.out.println("Next" + next);
        return next;
    }


    //find brightest neighbour, return the currentPixel if there is no brighter neighbour!!
    public int findMaxChargeNeighbour(ArrayList<FactCameraPixel> usableNeighbours, int currentPixel, double[] photoncharge){

        double maxBrightness = photoncharge[currentPixel];
        int maxBrightnessID = currentPixel;

        for (FactCameraPixel n : usableNeighbours) {
            if (photoncharge[n.id] > maxBrightness) {
                maxBrightness = photoncharge[n.id];
                maxBrightnessID = n.id;
            }
        }
        return maxBrightnessID;
    }


    //find brightest neighbour in large neighbourhood, return the currentPixel if there is no brighter neighbour!!
    public int findMaxChargeLargeNeighbour(int currentPixel, double[] photoncharge){
        FactCameraPixel[] largeNeighbours = mapping.getSecondOrderNeighboursFromID(currentPixel);

        double maxBrightness = photoncharge[currentPixel];
        int maxBrightnessID = currentPixel;

        for (FactCameraPixel n : largeNeighbours) {
            if (photoncharge[n.id] > maxBrightness) {
                maxBrightness = photoncharge[n.id];
                maxBrightnessID = n.id;
            }
        }
        return maxBrightnessID;
    }



    //give all pixel from path the same new clusterID
    public static void pathToNewCluster(int[] clusterID, ArrayList<Integer> aktuellerPfad, int clusterNum){
        for (int p:aktuellerPfad){

            clusterID[p] = clusterNum;
        }

        aktuellerPfad.clear();
    }

    //add path to existing cluster
    public static void pathToExistingCluster(int [] clusterID, ArrayList<Integer> aktuellerPfad, int clusterNum){
        for (int p:aktuellerPfad){
            clusterID[p] = clusterNum;
        }
        aktuellerPfad.clear();
    }


   //count pixel with the same clusterID, return array with size for each cluster
    public int[] getClusterSize(int[] clusterID, int cluster){
        int [] clusterSize = new int[cluster+1];

        //number of all pixel without clusterID (clusterID=-2) in clusterSize[0]
        int countUnderThreshold = 0;
        for(int i=0; i<1440; i++){
            if(clusterID[i]==-2){countUnderThreshold++;}
            else{
                clusterSize[clusterID[i]]++;
            }
        }

        clusterSize[0] = countUnderThreshold;

        return clusterSize;
    }

    //remove clusters with less than minClusterSize pixel
/*    public int[] removeSmallCluster(int[] clusterID, int[] clusterSize, int minClusterSize){
        for(int c=1; c<clusterSize.length; c++){
            if(clusterSize[c] <= minClusterSize){
                for(int i=0; i<1440; i++){
                    if(clusterID[i] == c){clusterID[i] = -2;}
                }
            }
        }
        return clusterID;
    }*/


    //make succseccive clusterIDs
    public int[] renameClusterID(int[] clusterID){
        ArrayList<Integer> ClusterList = new ArrayList<>();

        for(int i=0; i<1440; i++){
            if(clusterID[i] != -2){
                if(!ClusterList.contains(clusterID[i])){
                    ClusterList.add(clusterID[i]);
                }
            }
        }

        for(int c=0; c<ClusterList.size(); c++){
            int ID = ClusterList.get(c);
            for(int i=0; i<1440; i++){
                if(clusterID[i] == ID){
                    clusterID[i] = c+1;
                }
            }
        }
        return clusterID;
    }

    public int[] sortClusterBySize(int [] clusterSize, int[] clusterID){
        int[] newClusterID = clusterID.clone();
        int[] sizeSorted = new int[clusterSize.length];
        int[] size = clusterSize.clone();


        for(int c=1; c<(sizeSorted.length); c++){

            int maxSize = -1;
            int maxID = -1;

            for(int i=1; i<size.length; i++){
                if(size[i] >= maxSize){
                    maxSize = size[i];
                    maxID = i;
                }
            }

            sizeSorted[c] = maxID;
            size[maxID] = -2;
        }

        for(int c=1; c<sizeSorted.length; c++){
            for(int i=0; i<1440; i++){
                if(clusterID[i] == sizeSorted[c]){
                    newClusterID[i] = c;
                }
            }
        }

        return newClusterID;
    }

    public int getNumberOfClusters(int[] clusterID){
        ArrayList<Integer> ClusterList = new ArrayList<>();
        for(int i=0; i<1440; i++){
            if(clusterID[i] != -2){
                if(!ClusterList.contains(clusterID[i])){
                    ClusterList.add(clusterID[i]);
                }
            }
        }
        return (ClusterList.size());
    }

    //count pixel in all clusters
    public static int countClusterPixel(int[] clusterID){
        int countPixel = 0;
        for(int i=0; i<1440; i++){
            if(clusterID[i]!=-2){ countPixel++;}
        }
        return countPixel;
    }

    public static int[] findMaxIdInCluster(int[] clusterID, double[] brightness, int numCluster){
        int[] clusterMax = new int[numCluster+1];
        clusterMax[0] = 0;
        for(int c=1; c<=numCluster;c++){
            int maxID = -1;
            double maxBrightness = -500;
            for(int i=0; i<1440; i++){
                if(clusterID[i] == c){
                    if(brightness[i] > maxBrightness){
                        maxBrightness = brightness[i];
                        maxID = i;
                    }
                }
            }
            clusterMax[c] = maxID;
        }

        return clusterMax;
    }

    public static int[] findMinIdInCluster(int[] clusterID, double[] brightness, int numCluster){
        int[] clusterMin = new int[numCluster+1];
        clusterMin[0] = 0;
        for(int c=1; c<=numCluster;c++){
            int minID = -1;
            double minBrightness = 500;
            for(int i=0; i<1440; i++){
                if(clusterID[i] == c){
                    if(brightness[i] < minBrightness){
                        minBrightness = brightness[i];
                        minID = i;
                    }
                }
            }
            clusterMin[c] = minID;
        }

        return clusterMin;
    }


    public int[] removeClusterCleaning(int[] clusterID, int[] shower, int numCluster){
        for(int c = 1; c<=numCluster; c++){
            int count = 0;
            for(int i=0; i<1440; i++){
                if(clusterID[i] == c && ArrayUtils.contains(shower, i)){
                    count++;
                }
            }
            if(count <= 2){    //<----------------------------------------------------threshold cleaning-pixel in cluster
                clusterID = setClusterToId(clusterID,c,-2);
            }
        }
        return clusterID;
    }


    public static int[] setClusterToId(int[] clusterID, int oldClusterID, int newClusterID){
        for(int i=0; i<1440; i++){
            if(clusterID[i] == oldClusterID){
                clusterID[i] = newClusterID;
            }
        }

        return clusterID;
    }


    //----------------------------------more or less useful features for later gamma/hadron-separation ----------------

    public static double stdArrivaltimeMaxima(double arrivalTime[], int[]maxima, int numCluster){
        double[] arrivaltimeMax = new double[numCluster+1];
        double meanArrivaltime = 0;

        for(int i=1; i<=numCluster; i++){
            arrivaltimeMax[i] = arrivalTime[maxima[i]];
        }

        for(int i=1; i<arrivaltimeMax.length; i++){
            meanArrivaltime = meanArrivaltime + arrivaltimeMax[i]/arrivaltimeMax.length;
        }

        double sum = 0;
        for(int i=1; i<arrivaltimeMax.length; i++){
            sum = sum + Math.pow((meanArrivaltime - arrivaltimeMax[i]),2)/arrivaltimeMax.length;
        }

        return Math.sqrt(sum);
    }

    public static double clusterCharge(int[] clusterID, double brightness[]){
        double charge = 0;
        for(int i=0; i<1440; i++){
            if(clusterID[i] != -2){
                charge = charge + brightness[i];
            }
        }
        return charge;
    }


    public double[] sizeAxisX(int[] clusterID, double[] brightness, int numCluster){
        int[] minima = findMinIdInCluster(clusterID, brightness, numCluster);
        double[] sizeX = new double[numCluster+1];
        for(int c=1; c<=numCluster; c++){
            double sum1 = 0;
            double sum2 = 0;
            double sum3 = 0;
            for(int  i=0;i<1440;i++){
                if(clusterID[i] == c){
                    double d = brightness[i] - brightness[minima[c]];
                    double x = mapping.getPixelFromId(i).posX;// + 19.05;
                    sum1 = sum1 + d*x*x;
                    sum2 = sum2 + d;
                    sum3 = sum3 + d*x;
                }
            }

            sizeX[c] = Math.sqrt(Math.abs(sum1/sum2 - Math.pow((sum3/sum2),2)));



        }
        return sizeX;
    }


    public double[] sizeAxisY(int[] clusterID, double[] brightness, int numCluster){
        int[] minima = findMinIdInCluster(clusterID, brightness, numCluster);
        double[] sizeY = new double[numCluster+1];
        for(int c=1; c<=numCluster; c++){
            double sum1 = 0;
            double sum2 = 0;
            double sum3 = 0;
            for(int  i=0;i<1440;i++){
                if(clusterID[i] == c){
                    double d = brightness[i]- brightness[minima[c]];
                    double y = mapping.getPixelFromId(i).posY;// + 19.0;
                    sum1 = sum1 + d*y*y;
                    sum2 = sum2 + d;
                    sum3 = sum3 + d*y;
                }
            }
            sizeY[c] = Math.sqrt(sum1/sum2 - Math.pow((sum3/sum2),2));
        }
        return sizeY;
    }

/*    public double sizeAbs(int[] clusterID, double[] brightness, int numCluster){
        double [] sizeX = sizeAxisX(clusterID, brightness, numCluster);
        double [] sizeY = sizeAxisX(clusterID, brightness, numCluster);

        double abs = 0;
        for(int c=1; c<sizeX.length; c++){
            abs = abs + Math.sqrt(sizeX[c] * sizeX[c] + sizeY[c] * sizeY[c])/sizeX.length;
        }

        return abs;
    }*/

    public double sizeStd(int[] clusterID, double[] brightness, int numCluster){
        double [] sizeX = sizeAxisX(clusterID, brightness, numCluster);
        double [] sizeY = sizeAxisY(clusterID, brightness, numCluster);

        double mean = 0;
        double std = 0;
        for(int c=1; c<sizeX.length; c++){
            mean = mean + Math.sqrt(sizeX[c]*sizeX[c] + sizeY[c]*sizeY[c])/sizeX.length;
        }

        for(int c=1; c<sizeX.length; c++){
            std = std  + (1.0/numCluster)*Math.pow((mean - Math.sqrt(sizeX[c]*sizeX[c] + sizeY[c]*sizeY[c])),2);
        }

        std = Math.sqrt(std);


        return std;
    }



    public static double chargeInBiggestCluster(double[] photoncharge, int[] clusterID){
        double charge = 0;
        for(int i=0; i<1440; i++){
            if(clusterID[i] == 1){
                charge = charge + photoncharge[i];
            }
        }
        return charge;
    }

    public int isolatedCluster(int [] clusterID, int numCluster){
        int[] isolated  = new int[numCluster + 1];
        int countIsolatedCluster = 0;
        if(numCluster > 1){
            for (int i = 0; i < 1440; i++){
                if (clusterID[i] != -2){

                        FactCameraPixel[] allNeighbours = mapping.getNeighboursFromID(i);
                        for (FactCameraPixel p : allNeighbours){
                            if (clusterID[p.id] != clusterID[i] && clusterID[p.id] != -2){
                                isolated[clusterID[i]] = -1;
                                isolated[clusterID[p.id]] = -1;
                            }
                        }
                }
            }

            for (int c=1; c<isolated.length; c++) {

                if (isolated[c]== 0) {

                    countIsolatedCluster++;
                }
            }
        }
        return countIsolatedCluster;
    }


    //some features not used at the moment, calculateClusterCog fails because of not existing cog-pixels. not fixed.
    //lin regression could be used for some features that works with the cluster cog's. not used at the moment

/*    public double[][] calculateClusterCogInMM(int numCluster, int[] clusterID, double[] photoncharge){
        double [][] cogCluster = new double[numCluster+1][2];

        for(int c=1; c<=numCluster; c++) {

            double size = 0;
            ArrayList<Integer> clusterPixel = new ArrayList<>();
            for (int i = 0; i < 1440; i++) {
                if (clusterID[i] == c) {
                    clusterPixel.add(i);
                }
            }

            for (int i : clusterPixel) {
                cogCluster[c][0] += photoncharge[i]
                        * mapping.getPixelFromId(i).getXPositionInMM();
                cogCluster[c][1] += photoncharge[i]
                        * mapping.getPixelFromId(i).getYPositionInMM();
                size += photoncharge[i];

            }

            cogCluster[c][0] /= size;
            cogCluster[c][1] /= size;

        }

        return cogCluster;
    }*/


/*    public int[] clusterCogID(int numCluster, int[] clusterID, double[] photoncharge){
        double[][] cogInMM = calculateClusterCogInMM(numCluster, clusterID, photoncharge);
        int[] clusterCogID = new int[numCluster+1];
        for(int c=1; c<=numCluster; c++){
            // could fail.
             clusterCogID[c] = mapping.getPixelBelowCoordinatesInMM(cogInMM[c][0], cogInMM[c][1]).id;
        }

        return clusterCogID;
    }


    public double[] linRegression(double[] x, double[] y){
        SimpleRegression regression = new SimpleRegression();
        double[] regParameter = new double[4];
        for(int i=0; i<x.length; i++) {
            regression.addData(x[i], y[i]);
        }

        regParameter[0] = regression.getIntercept();
        regParameter[1] = regression.getSlope();
        regParameter[2] = regression.getInterceptStdErr();
        regParameter[3] = regression.getRegressionSumSquares();

        return regParameter;
    }*/

}
