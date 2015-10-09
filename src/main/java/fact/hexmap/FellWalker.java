package fact.hexmap;

import stream.Data;
import stream.Processor;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;

/**
 * Created by lena on 05.08.15.
 */
public class FellWalker implements Processor {

    FactPixelMapping mapping = FactPixelMapping.getInstance();
    double[] brightness = new double[1440];

    //int [] clusterID = new int[1440];


    ArrayList<Integer> aktuellerPfad = new ArrayList<>();
    ArrayList<Integer> maxima = new ArrayList<>();


    @Override
    public Data process(Data data) {
        double[] data_array = (double[]) data.get("DataCalibrated");
        int [] cleaning = ((int[]) data.get("Cleaning")).clone();
        double[] arrivalTime = ((double[]) data.get("ArrtimePos")).clone();
        int[] clusterID = new int[1440];
        double[] photoncharge = ((double[]) data.get("photoncharge")).clone();
        double cogX = (double) data.get("COGx");
        double cogY = (double) data.get("COGy");


        int roi = (Integer) data.get("NROI");
        int npix = (Integer) data.get("NPIX");
        int minClusterSize = 5;


        for(int i=0; i<1440;i++){
            clusterID[i] = 0;
        }


/*        //fill arrivaltime array
        for (FactCameraPixel p : mapping.pixelArray) {

            if (cleaning[p.id] != 0) {
                double arrivaltime = arrivalTime[p.id];
                double[] brightnessSlices = p.getPixelData(data_array, roi);
                double temp = 0;
               for(int i=10; i<250; i++){
                   if (temp < brightnessSlices[i] - brightnessSlices[i-1]){
                       arrivaltime = i;
                   }
               }
                arrivalTime[p.id] = arrivaltime;
            }

        }

        //fill brightness array
        int k = 0;
        for (FactCameraPixel p : mapping.pixelArray) {

            double[] brightnessSlices = p.getPixelData(data_array, roi);
            double b = 0;
            for(int i=50; i<120; i++) {
                b = b + brightnessSlices[i]/(70.0);
            }
            brightness[k] = b;
            k++;

        }*/
        //System.out.println("Brightness berechnet");
        //threshold: brightness < threshold -> -2
        //int[] cleaning = removeUnderground(0.1);  //<---------------------------------------hardcode?

        int startPath = NextStartPixel(clusterID);
        //int startPath = 988;

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
                //FactCameraPixel[] usableNeighbours;
                ArrayList<FactCameraPixel> usableNeighbours = new ArrayList<>();

                for(FactCameraPixel n: allNeighbours){
                    if(clusterID[n.id] != -2){
                        usableNeighbours.add(n);
                    }
                }

                //brightestNeighbourID = findBrightestNeighbour(usableNeighbours, currentPixel);
                brightestNeighbourID = findMaxChargeNeighbour(usableNeighbours, currentPixel, photoncharge);

                aktuellerPfad.add(brightestNeighbourID);

                if (brightestNeighbourID == currentPixel) {
                    //int brightestNeighbourIDLarge = findBrightestLargeNeighbour(currentPixel);
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

        clusterID = removeClusterCleaning(clusterID, cleaning, numCluster);

        clusterID = renameClusterID(clusterID);

        int numClusterPixel = countClusterPixel(clusterID);

        numCluster = getNumberOfClusters(clusterID);

        int [] clusterSize = getClusterSize(clusterID, numCluster);


        clusterID = sortClusterBySize(clusterSize, clusterID);

        int[] maxima = findMaxIdInCluster(clusterID, photoncharge, numCluster);

        double[] clusterBrightnessSum = clusterBrightnessSum(clusterID, photoncharge, numCluster);
        double chargeMaxCluster = chargeInBiggestCluster(photoncharge, clusterID);
        double fullCharge = clusterCharge(clusterID, photoncharge, numCluster);
        double chargeMaxClusterRatio = chargeMaxCluster/fullCharge;

        clusterSize = getClusterSize(clusterID, numCluster);

        double isolatedCluster = isolatedCluster(clusterID, numCluster);

        //int[] clusterCog = clusterCogID(numCluster, clusterID, photoncharge);

        int cogID = mapping.getPixelBelowCoordinatesInMM(cogX, cogY).id;
        clusterID[cogID] = -10;



        double errRegressionCog = regressionClusterCog(numCluster, clusterID, photoncharge)[2];

        double angle = clusterAngle(numCluster, clusterID, photoncharge, cogX, cogY);

        double arrTimeStd = stdArrivaltimesClusterCog(maxima, arrivalTime);

        double size = sizeStd(clusterID, photoncharge, numCluster);





        /*int[] clusterArrivaltimeMaxima = clusterArrivaltimeMaxima(clusterID, brightness, arrivalTime, maxima, numCluster);*/
        //double[] clusterArrivaltimeMean = clusterArrivaltimeMean(clusterID, clusterSize, arrivalTime, numCluster);

        //double[] clusterBrightnessMean = clusterBrightnessMean(clusterID, clusterSize, photoncharge, numCluster);
        /*double[] distanceMaxima = distanceMaxima(maxima, numCluster);
        double[] brightnessMaxima = brightnessMaxima(maxima, brightness, numCluster);*/
        //double[] sizeX = sizeAxisX(clusterID, brightness, numCluster);
        //double[] sizeY = sizeAxisY(clusterID, brightness, numCluster);

        /*double[] absSize = new double[numCluster+1];
        for(int c=1; c<=numCluster; c++){
            absSize[c] = Math.sqrt(sizeX[c]*sizeX[c] + sizeY[c]*sizeY[c]);
        }*/

        //double distanceSize = distanceSize(clusterSize,numCluster,maxima);
        //double distanceArrivaltime = distanceArrivaltime(arrivalTime, numCluster,maxima);
        double distance = distanceBrightness(numCluster, maxima);

        data.put("ClusterID", clusterID);
        data.put("NumCluster", numCluster);
        data.put("ChargeMax", chargeMaxClusterRatio);
        data.put("SizeCluster1", clusterSize[1]);
        data.put("NumClusterPixel", numClusterPixel);
        data.put("Distance", distance);
        data.put("IsolatedCluster", isolatedCluster);
        data.put("ErrRegCog", errRegressionCog);
        data.put("Angle", angle);
        data.put("ArrTimeClusterStd", arrTimeStd);
       //data.put("ClusterArrivaltimeMaxima", clusterArrivaltimeMaxima);
        //data.put("ClusterArrivaltimeMean", clusterArrivaltimeMean);
        data.put("ClusterBrightnessSum", clusterBrightnessSum);
        //data.put("ClusterBrightnessMean", clusterBrightnessMean);
        //data.put("DistanceMaxima", distanceMaxima);
        //data.put("BrightnessMaxima", brightnessMaxima);
        //data.put("MaximaID", maxima);
        /*data.put("SizeX", sizeX);
        data.put("SizeY", sizeY);*/
        data.put("Size", size);
        //data.put("Waste", waste);
/*      data.put("DistanceSize", distanceSize);
        data.put("DistanceArrivaltime", distanceArrivaltime);*/
/*        data.put("DistanceBrightness", distanceBrightness);*/

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

/*    public int[] removeUnderground(double threshold){
        //find max/min brightness
        double max = -10.0;
        double min = 100.0;
        int ID_max = -1;
        int ID_min = -1;

        int[] cleaning = new int[1440];
        for(int i=0; i<1440; i++){
            if(brightness[i] > max){
                ID_max = i;
                max = brightness[i];
            }
            if(brightness[i] < min){
                ID_min = i;
                min = brightness[i];
            }
        }
        //define threshold value
        double abs = brightness[ID_max] - brightness[ID_min];
        double abs_threshold = brightness[ID_min] + threshold*abs;
        //System.out.println(brightness[ID_max] + "  " + brightness[ID_min] + "     " + abs_threshold);

        //set all pixel with brightness under threshold to -2
        for(int i=0; i<1440; i++){
            if(brightness[i] < abs_threshold) {
                clusterID[i] = -2;
            }
        }
        removeIsolatedPixel();
        for(int i=0; i<1440; i++){
            cleaning[i] = clusterID[i];
        }
        return cleaning;
    }*/

/*    public void removeIsolatedPixel() {
        for (int i = 0; i < 1440; i++) {
            if (clusterID[i] == 0) {
                int numberNeighbours = 0;
                FactCameraPixel[] neighbours = mapping.getNeighboursFromID(i);
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

    }*/

    //find brightest neighbour, return the currentPixel if there is no brighter neighbour!!
    public int findBrightestNeighbour(ArrayList<FactCameraPixel> usableNeighbours, int currentPixel){

        double maxBrightness = brightness[currentPixel];
        int maxBrightnessID = currentPixel;

        for (FactCameraPixel n : usableNeighbours) {
            if (brightness[n.id] > maxBrightness) {
                maxBrightness = brightness[n.id];
                maxBrightnessID = n.id;
            }
        }
        return maxBrightnessID;
    }

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
    public int findBrightestLargeNeighbour(int currentPixel){
        FactCameraPixel[] largeNeighbours = mapping.getSecondOrderNeighboursFromID(currentPixel);

        double maxBrightness = brightness[currentPixel];
        int maxBrightnessID = currentPixel;

        for (FactCameraPixel n : largeNeighbours) {
            if (brightness[n.id] > maxBrightness) {
                maxBrightness = brightness[n.id];
                maxBrightnessID = n.id;
            }
        }
        return maxBrightnessID;
    }


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
    public int[] removeSmallCluster(int[] clusterID, int[] clusterSize, int minClusterSize){
        for(int c=1; c<clusterSize.length; c++){
            if(clusterSize[c] <= minClusterSize){
                for(int i=0; i<1440; i++){
                    if(clusterID[i] == c){clusterID[i] = -2;}
                }
            }
        }
        return clusterID;
    }


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
        //System.out.println(ClusterList.size());
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

    public int findMaxEntry(int[] clusterSize, int startindex){
        int max = clusterSize[startindex];
        int maxID = startindex;
        for(int c=startindex+1; c<clusterSize.length; c++){
            if(clusterSize[c] > max){
                max = clusterSize[c];
                maxID = c;
            }
        }
        return maxID;
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

    public static int getMaxID(int[] clusterMax, double[] brightness){
        double max = brightness[clusterMax[0]];
        int maxID = 0;
        for(int i=1; i<clusterMax.length; i++){
            if(brightness[clusterMax[i]] > max){
                max = brightness[clusterMax[i]];
                maxID = i;
            }
        }
        return maxID;
    }

    public static int getMinID(int[] clusterMax, double[] brightness){
        double min = brightness[clusterMax[0]];
        int minID = 0;
        for(int i=1; i<clusterMax.length; i++){
            if(brightness[clusterMax[i]] < min){
                min = brightness[clusterMax[i]];
                minID = i;
            }
        }
        return minID;
    }

    public static double meanArrivaltime(int[] arrivaltimeMax){
     double arrival = 0;
     for(int i=1; i<arrivaltimeMax.length; i++){
         arrival = arrival + arrivaltimeMax[i]/arrivaltimeMax.length;
     }
     return arrival;
    }

    public static double stdDevArrivaltime(double mean, int[]arrivaltimeMax){
        double sum = 0;
        for(int i=1; i<arrivaltimeMax.length; i++){
            sum = sum + Math.pow((mean - arrivaltimeMax[i]),2)/arrivaltimeMax.length;
        }
        return Math.sqrt(sum);
    }



    /*build mean and standard deviation of arrivaltimes from all clustermaxima.
    * first idea: remove cluster with arrivaltime out of std dev. -> removes the shower cluster
    * => keep cluster with arrivaltimes out of std dev, remove cluster with arrivaltime close to mean
     */
    public static int[] removeClusterArrivaltime(int[]clusterID, double[] brightness, int arrivalTime[], int numCluster){
        int[] maxima = findMaxIdInCluster(clusterID, brightness, numCluster);
        System.out.println(numCluster + "  " + maxima.length);
        int[] arrivaltimeMax = new int[numCluster+1];
        arrivaltimeMax[0] = 0;

        for(int i=1; i<=numCluster; i++){
                arrivaltimeMax[i] = arrivalTime[maxima[i]];
        }

        double meanArrivaltime = meanArrivaltime(arrivaltimeMax);
        double stdDevArrivaltime = stdDevArrivaltime(meanArrivaltime,arrivaltimeMax);

        for(int c = 1; c<maxima.length; c++){
            if(Math.abs(arrivaltimeMax[c] - meanArrivaltime) < Math.abs(meanArrivaltime - stdDevArrivaltime)){
                clusterID = setClusterToId(clusterID,c,-2);
            }
        }

        return clusterID;
    }

    public int[] removeClusterCleaning(int[] clusterID, int[] cleaning, int numCluster){
        for(int c = 1; c<=numCluster; c++){
            int count = 0;
            for(int i=0; i<1440; i++){
                if(clusterID[i] == c && cleaning[i] == 0){
                    count++;
                }
            }
            if(count <= 2){    //<----------------------------------------------------threshold cleaning-pixel in cluster
                clusterID = setClusterToId(clusterID,c,-2);
            }
        }
        return clusterID;
    }





    //features
    public static int[] clusterArrivaltimeMaxima(int[]clusterID, double[] brightness, int arrivalTime[], int[]maxima, int numCluster){
        //System.out.println(numCluster + "  " + maxima.length);
        int[] arrivaltimeMax = new int[numCluster+1];
        //arrivaltimeMax[0] = 0;

        for(int i=1; i<=numCluster; i++){
            arrivaltimeMax[i] = arrivalTime[maxima[i]];
        }

        double meanArrivaltime = meanArrivaltime(arrivaltimeMax);
        double stdDevArrivaltime = stdDevArrivaltime(meanArrivaltime,arrivaltimeMax);

        arrivaltimeMax[0] = 0;

        return arrivaltimeMax;
    }

    public static double[] clusterArrivaltimeMean(int[] clusterID, int[] clusterSize, int arrivalTime[], int numCluster){
        double [] arrivaltimeMean = new double[numCluster+1];
        arrivaltimeMean[0] = 0;
        for(int c=1;c<=numCluster;c++){
            double arrival = 0;
            for(int i=0;i<1440; i++){
                if(clusterID[i] == c){
                    arrival=arrival + arrivalTime[i]/clusterSize[c];
                }
            }
            arrivaltimeMean[c] = arrival;
        }
        return arrivaltimeMean;
    }

    public double stdArrivaltimesClusterCog(int[] cogID, double[] arrivaltimes){
        double arrtimeMean = 0;
        double arrtimeStd = 0;

        for(int c=1; c< cogID.length; c++){
            arrtimeMean = arrtimeMean + (1.0/(cogID.length))*arrivaltimes[cogID[c]];
        }

        for(int c=1; c< cogID.length; c++){
            arrtimeStd = arrtimeStd + (1.0/(cogID.length))*Math.pow(arrtimeMean- arrivaltimes[cogID[c]],2);
        }
         arrtimeStd = Math.sqrt(arrtimeStd);

        return arrtimeStd;

    }

    public static double[] clusterBrightnessSum(int[] clusterID, double brightness[], int numCluster){
        double [] brightnessSum = new double[numCluster+1];

            for(int i=0;i<1440; i++){
                if(clusterID[i] == -2){
                    brightnessSum[0] =  brightnessSum[0] + brightness[i];
                }
                else{
                    brightnessSum[clusterID[i]]+= brightness[i];
                }
            }

        return brightnessSum;

    }

    public static double clusterCharge(int[] clusterID, double brightness[], int numCluster){
        double charge = 0;
        for(int i=0; i<1440; i++){
            if(clusterID[i] != -2){
                charge = charge + brightness[i];
            }
        }
        return charge;
    }

    public static double[] clusterBrightnessMean(int[] clusterID, int[] clusterSize, double brightness[], int numCluster){
        double [] brightnessMean = new double[1440];
        for(int c=1;c<=numCluster;c++){
            double b = 0;
            for(int i=0;i<1440; i++){
                if(clusterID[i] == c){
                    b=b + brightness[i]/clusterSize[c];
                }
            }
            for(int i=0; i<1440; i++){
                if(clusterID[i] == -2){brightnessMean[i] = 0;}

                else if(clusterID[i] == c){
                    brightnessMean[i] = b;
                }
            }
        }
        return brightnessMean;

    }

    public double[] distanceMaxima(int[] maxima, int numCluster){
        double[] dist = new double[numCluster+1];
        dist[0] = 0;
        if(numCluster == 1){
            dist[1] = 0;
        }
        else if(numCluster == 0){
            dist[0] = -1;
        }
        else {
            for (int c = 1; c <= numCluster; c++) {
                FactCameraPixel p = mapping.getPixelFromId(maxima[c]);
                double temp = 0;
                for (int cu = 1; cu <= numCluster; cu++) {

                    if (c != cu) {
                        FactCameraPixel q = mapping.getPixelFromId(maxima[cu]);
                        double distance = Math.sqrt(Math.pow(p.posX - q.posX, 2) + Math.pow(p.posY - q.posY, 2));
                        if (distance > temp) {
                            temp = distance;
                        }
                    }
                }
                dist[c] = temp;

            }
        }
        return dist;
    }


    public static double [] brightnessMaxima(int[] maxima, double[] brightness, int numCluster) {

        double[] brightnessMaxima = new double[numCluster + 1];
        for (int i = 1; i <= numCluster; i++) {
            brightnessMaxima[i] = brightness[maxima[i]];
        }
        return brightnessMaxima;
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
/*            System.out.println(sum2);*/
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

    public double sizeAbs(int[] clusterID, double[] brightness, int numCluster){
        double [] sizeX = sizeAxisX(clusterID, brightness, numCluster);
        double [] sizeY = sizeAxisX(clusterID, brightness, numCluster);

        double abs = 0;
        for(int c=1; c<sizeX.length; c++){
            abs = abs + Math.sqrt(sizeX[c]*sizeX[c] + sizeY[c]*sizeY[c])/sizeX.length;
        }

        return abs;
    }

    public double sizeStd(int[] clusterID, double[] brightness, int numCluster){
        double [] sizeX = sizeAxisX(clusterID, brightness, numCluster);
        double [] sizeY = sizeAxisX(clusterID, brightness, numCluster);

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



    //geometric distance between the brightest pixel of the biggest and the smallest cluster (clusterSize)
    public double distanceSize(int[] clusterSize, int numCluster, int[] maxima){
        if(numCluster < 2){return 0;}
        else{
            int min = clusterSize[1];
            int max = clusterSize[1];
            int minCluster = 1;
            int maxCluster = 1;
            for(int c=2; c<=numCluster; c++){
                if(clusterSize[c] < min){
                    min = clusterSize[c];
                    minCluster = c;
                }
                if(clusterSize[c] > max){
                    max = clusterSize[c];
                    maxCluster = c;
                }
            }


            double xMin = (mapping.getPixelFromId(maxima[minCluster])).posX;
            double yMin = (mapping.getPixelFromId(maxima[minCluster])).posY;
            double xMax = (mapping.getPixelFromId(maxima[maxCluster])).posX;
            double yMax = (mapping.getPixelFromId(maxima[maxCluster])).posY;

            double distance = Math.sqrt(Math.pow((xMin - xMax), 2) + Math.pow((yMin-yMax),2));
            return distance;

        }


    }

    //time-distance between the first and the last arriving cluster
    public double distanceArrivaltime(int[] arrivalTime, int numCluster, int[] maxima){
        if(numCluster < 2){return 0;}
        else{
            int min = arrivalTime[maxima[1]];
            int max = arrivalTime[maxima[1]];
            int minID = maxima[1];
            int maxID = maxima[1];
            for(int c=2; c<=numCluster; c++){
                if(arrivalTime[maxima[c]] < min){
                    min = arrivalTime[maxima[c]];
                    minID = maxima[c];
                }
                if(arrivalTime[maxima[c]] > max){
                    max = arrivalTime[maxima[c]];
                    maxID = maxima[c];
                }
            }

            double xMin = mapping.getPixelFromId(minID).posX;
            double yMin = mapping.getPixelFromId(minID).posY;
            double xMax = mapping.getPixelFromId(maxID).posX;
            double yMax = mapping.getPixelFromId(maxID).posY;

            double distance = Math.sqrt(Math.pow((xMin - xMax), 2) + Math.pow((yMin - yMax), 2));
            return distance;
        }
    }

    //distance between biggest and smallest cluster (pixel with max. photoncharge in cluster)
    public double distanceBrightness(int numCluster, int[] maxima){
        int minID = maxima[numCluster];
        int maxID = maxima[1];

            double xMin = mapping.getPixelFromId(minID).posX;
            double yMin = mapping.getPixelFromId(minID).posY;
            double xMax = mapping.getPixelFromId(maxID).posX;
            double yMax = mapping.getPixelFromId(maxID).posY;

            double distance = Math.sqrt(Math.pow((xMin - xMax), 2) + Math.pow((yMin - yMax), 2));
            return distance;
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

    public double isolatedCluster(int [] clusterID, int numCluster){
        int[] isolated  = new int[numCluster + 1];
        double countIsolatedCluster = 0;
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
        return countIsolatedCluster;///((int)numCluster);
    }

    public double[][] calculateClusterCogInMM(int numCluster, int[] clusterID, double[] photoncharge){
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
    }


    public int[] clusterCogID(int numCluster, int[] clusterID, double[] photoncharge){
        double[][] cogInMM = calculateClusterCogInMM(numCluster, clusterID, photoncharge);
        int[] clusterCogID = new int[numCluster+1];
        for(int c=1; c<=numCluster; c++){
            clusterCogID[c] = mapping.getPixelBelowCoordinatesInMM(cogInMM[c][0], cogInMM[c][1]).id;
            //System.out.println(clusterCogID[c]+ "\t" + cogInMM[c][0] + "\t" + cogInMM[c][1]);
        }

        return clusterCogID;
    }

    public double[] regressionClusterCog(int numCluster, int[] clusterID, double[] photoncharge){
        double [] parameter = new double[3];
        if(numCluster == 1) {
            parameter[0] = 0;
            parameter[1] = 0;
            parameter[2] = 0;

        }
        double[][] cogInMM = calculateClusterCogInMM(numCluster, clusterID, photoncharge);
        double[] xInMM = new double[numCluster];
        double[] yInMM = new double[numCluster];

        for(int c=0; c<numCluster; c++){
            xInMM[c] = cogInMM[c][0];
            yInMM[c] = cogInMM[c][1];
        }

        parameter[0] = linRegression(xInMM, yInMM)[0];
        parameter[1] = linRegression(xInMM, yInMM)[1];
        parameter[2] = linRegression(xInMM, yInMM)[3];

        return parameter;

    }

    public double clusterAngle(int numCluster, int[] clusterID, double[] photoncharge, double cogX, double cogY){
        double[][] clusterCog = calculateClusterCogInMM(numCluster,clusterID, photoncharge);
        double [] angle = new double[numCluster+1];
        double meanAngle = 0;
        double std = 0;

        for (int c=1; c<=numCluster; c++){
            angle[c] = Math.atan2((clusterCog[c][1] - cogY), (clusterCog[c][0] - cogX));
            meanAngle = meanAngle + angle[c]/numCluster;
        }

        for (int c=1; c<=numCluster; c++){
            std = std  + (1.0/numCluster)*Math.pow((meanAngle - angle[c]),2);
        }


        std = Math.sqrt(std);



        return std;
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
    }





    public static int[] setClusterToId(int[] clusterID, int oldClusterID, int newClusterID){
        for(int i=0; i<1440; i++){
            if(clusterID[i] == oldClusterID){
                clusterID[i] = newClusterID;
            }
        }

        return clusterID;
    }



}
