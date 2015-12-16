package fact.hexmap;

/**
 * Created by lena on 16.11.15.
 */

import stream.Data;
import stream.Processor;

import java.util.ArrayList;

//package fact.hexmap;


public class ClusterFellwalker implements Processor {

    FactPixelMapping mapping = FactPixelMapping.getInstance();

    ArrayList<Integer> aktuellerPfad = new ArrayList<>();



    @Override
    public Data process(Data data) {
        int[] shower = ((int[]) data.get("shower"));
        double[] arrivalTime = ((double[]) data.get("ArrtimePos"));
        double[] photoncharge = ((double[]) data.get("photoncharge"));
/*      double cogX = (double) data.get("COGx");
        double cogY = (double) data.get("COGy");*/

        int[] clusterID = new int[1440];
        int[] showerClusterID = new int[1440];

        int minShowerpixel = 2;


        for (int i = 0; i < 1440; i++) {
            clusterID[i] = 0;
            showerClusterID[i] = -2;
        }


        int startPath = NextStartPixel(clusterID);

        int cluster = 1;


        //FellWalker
        while (startPath != -1) {

            int brightestNeighbourID;
            int currentPixel = startPath;
            boolean pathend = false;

            aktuellerPfad.add(startPath);

            while (!pathend) {
                //find neighbours and brightest neighbour
                FactCameraPixel[] allNeighbours = mapping.getNeighboursFromID(currentPixel);

                //find usable neighbours (pixel marked with clusterID = 0 after cleaning)
                ArrayList<FactCameraPixel> usableNeighbours = new ArrayList<>();

                for (FactCameraPixel n : allNeighbours) {
                    if (clusterID[n.id] != -2) {
                        usableNeighbours.add(n);
                    }
                }


                brightestNeighbourID = findMaxChargeNeighbour(usableNeighbours, currentPixel, photoncharge);

                aktuellerPfad.add(brightestNeighbourID);

                if (brightestNeighbourID == currentPixel) {
                    int brightestNeighbourIDLarge = findMaxChargeLargeNeighbour(currentPixel, photoncharge);

                    if (brightestNeighbourIDLarge != currentPixel) {

                        if (clusterID[brightestNeighbourIDLarge] != 0) {
                            pathToExistingCluster(clusterID, aktuellerPfad, clusterID[brightestNeighbourIDLarge]);
                            pathend = true;
                        } else {
                            currentPixel = brightestNeighbourIDLarge;
                        }
                    } else {
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

        //System.out.println(cluster + "   " + getNumberOfClusters(clusterID));
        /* create a FactCluster-array which contains all cluster as objects. The array-index of a cluster is equal to the
         clusterID set in the fellwalker-algorithm. Keep in mind, that there is no clusterID 0, so the first cluster-object
         has to be treated separately!
          */
        FactCluster[]  clusterSet = new FactCluster[cluster];
        for (int i=0; i<cluster; i++){
            clusterSet[i] = new FactCluster();
            clusterSet[i].setClusterID(i);
        }

        for(int i=0;i<1440;i++){
            clusterSet[clusterID[i]].addContentPixel(i);
            clusterSet[clusterID[i]].addContentPixelPhotoncharge(photoncharge[i]);
            clusterSet[clusterID[i]].addContentPixelArrivaltime(arrivalTime[i]);
        }

        //add showerpixel in a cluster to list
        for(int i=1; i<shower.length;i++){
            clusterSet[clusterID[shower[i]]].addCleaningPixel(shower[i]);
        }


        //set containsShowerPixel to false if cluster comtains less the minShowerpixel showerpixel and count all "surviving" cluster
        FactCluster[] showerCluster = removeCluster(clusterSet,minShowerpixel);

        int numCluster = showerCluster.length;

        //build showerpixelArray that contains only cluster with showerpixel (just to have a quick look at it in the event viewer)
        for(FactCluster c : clusterSet){
            if(c.getShowerLabel() == true){
                for(int i : c.contentPixel){
                    showerClusterID[i] = c.getClusterID();
                }
            }
        }


        // mark shower cog for the viewer
/*        for(FactCluster c : clusterSet){
            //System.out.println(c.cogId());
            if(c.getShowerLabel() == true) {
                showerClusterID[c.cogId()] = 0;
            }
        }*/

        //markBoundaryPixel(clusterSet, showerClusterID);

        //build features (if there is any cluster left after removeCluster) and put them to data set
        if(numCluster != 0) {
            int[] lengthBoundaries = countBoundaryPixel(showerCluster);
            double ratio = boundContentRatio(showerCluster);
            double idealBoundDiff = idealBoundDiff(showerCluster);
            double boundAngleSum = boundAngleSum(showerCluster);
            double distanceCenterSum = distanceCenter(showerCluster);

            //int [] viewer = showerClusterID.clone();

            //airPixelMap(showerCluster, showerClusterID, viewer);


            //double airpixel = airpixelMean(showerCluster);

            //System.out.println(numNeighborCluster + "   " + airpixel);

            findNeighbors(showerCluster, showerClusterID);

            double numNeighborCluster = neighborClusterMean(showerCluster);

            //int numIsolatedCluster = numIsolatedCluster(showerCluster);

            double chargeMaxClusterRatio = getChargeMaxCluster(showerCluster);

            int numPixelMaxCluster = maxCluster(showerCluster).getNumPixel();

            int numClusterPixel = numClusterPixel(showerCluster);

            //double stdArrTimeMaxima = stdArrTime(showerCluster, arrivalTime);

            double stdNumpixel = stdNumPixel(showerCluster);


            data.put("BoundLength", lengthBoundaries);
            data.put("BoundRatio", ratio);
            data.put("IdealBoundDiff", idealBoundDiff);
            data.put("BoundAngleSum", boundAngleSum);
            data.put("DistanceCenterSum", distanceCenterSum);
            data.put("NeighborCluster", numNeighborCluster);
            //data.put("IsolatedCluster", numIsolatedCluster);
            data.put("ChargeMax", chargeMaxClusterRatio);
            data.put("MaxClusterNumPixel", numPixelMaxCluster);
            data.put("NumClusterPixel", numClusterPixel);
            //data.put("StdArrivaltimeMaxima", stdArrTimeMaxima);
            data.put("StdNumPixel", stdNumpixel);

            //data.put("Airpixel", airpixel);

        }

        data.put("AllClusterID", clusterID);
        data.put("ShowerClusterID", showerClusterID);
        data.put("ClusterNoCleaning", cluster);
        data.put("NumCluster", numCluster);

        return data;

    }

    //find next pixel without clusterID to start a new path, return ID
    public int NextStartPixel(int[] clusterID) {
        int next;
        int i = 0;
        while (clusterID[i] != 0) {
            i++;
            if (i == 1440) {
                i = -1;
                break;
            }
        }
        next = i;
        return next;
    }

    //find brightest neighbour, return the currentPixel if there is no brighter neighbour!!
    public int findMaxChargeNeighbour(ArrayList<FactCameraPixel> usableNeighbours, int currentPixel, double[] photoncharge) {

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
    public int findMaxChargeLargeNeighbour(int currentPixel, double[] photoncharge) {
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

    //give all pixel on a path the same new clusterID
    public static void pathToNewCluster(int[] clusterID, ArrayList<Integer> aktuellerPfad, int clusterNum) {
        for (int p : aktuellerPfad) {

            clusterID[p] = clusterNum;
        }

        aktuellerPfad.clear();
    }

    //add path to existing cluster
    public static void pathToExistingCluster(int[] clusterID, ArrayList<Integer> aktuellerPfad, int clusterNum) {
        for (int p : aktuellerPfad) {
            clusterID[p] = clusterNum;
        }
        aktuellerPfad.clear();
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

    public FactCluster [] removeCluster(FactCluster clusterSet[], int minShowerpixel){
        ArrayList<FactCluster> showerCluster = new ArrayList<>();
        int numShowerCluster = 0;
        int newID = 1;
        for(int c=1; c<clusterSet.length;c++){
            if(clusterSet[c].cleaningPixel.size()>minShowerpixel){
                clusterSet[c].setShowerLabel(true);
                clusterSet[c].setClusterID(newID);
                newID++;
                numShowerCluster++;
                showerCluster.add(clusterSet[c]);
            }
            else{
                clusterSet[c].setShowerLabel(false);
            }
        }

        FactCluster [] showerClusterArray = new FactCluster[numShowerCluster];
        for(int i=0; i<numShowerCluster; i++){
            showerClusterArray[i] = showerCluster.get(i);
        }
        return showerClusterArray;

    }

    public void markBoundaryPixel(FactCluster[] clusterSet, int[] showerClusterID){
        for(FactCluster c : clusterSet){
            if(c.getShowerLabel() == true){
            ArrayList<Integer> boundPixel = c.findBoundaryNaive();
                showerClusterID[boundPixel.get(0)] = -1;
                for (int i=1; i<boundPixel.size(); i++){

                    showerClusterID[boundPixel.get(i)] = 0;

                }
            }
        }
    }

    public int [] countBoundaryPixel(FactCluster [] showerCluster){
        int[] boundLength = new int[showerCluster.length];

        for(int i=0; i<showerCluster.length; i++){
            boundLength[i] = showerCluster[i].getBoundaryLength();
        }

        return boundLength;
    }

    public double  boundContentRatio(FactCluster [] showerCluster){
        double ratio = 0;

        int i = 0;
        for(FactCluster c : showerCluster){
            ratio += (double) c.getBoundaryLength()/c.getNumPixel();
            i++;
        }

        return ratio;
    }


    //returns sum of all idealBoundDiffs
    public double idealBoundDiff(FactCluster [] showerCluster){
        //int[] diffs = new int [showerCluster.length];
        double sum = 0;
        int i = 0;
        for (FactCluster c : showerCluster){
            sum += c.idealBoundDiff();
            i++;
        }

        return sum; ///showerCluster.length;
    }

    public double boundAngleSum(FactCluster[] showerCluster){
        double sum = 0;
        int i = 0;
        for (FactCluster c : showerCluster){
            sum += c.boundAngleSum();
            i++;
        }
        return sum;

    }

    public double distanceCenter(FactCluster[] showerCluster){
        double sum = 0;
        int i = 0;
        for (FactCluster c : showerCluster){
            sum += c.distanceCamCenter();
            i++;
        }
        return sum/showerCluster.length;
    }



    //Returns an 2 dimensional array containing the "air-pixel" between two clusters. "Air-pixel" are the pixel on the line
    //between two cluster cog's that don't belong to a shower-cluster. From the number of air-pixel one can conclude
    //whether the clusters are neighbors, and, if not, how large the distance is between them.
    //At the moment two clusters are marked as neighbors if there are no air-pixels between them.
    public void airPixelMap(FactCluster [] showerCluster, int [] showerClusterID, int[] viewer){
        //int[][] map = new int [showerCluster.length][showerCluster.length];
            //int [] viewer = showerClusterID.clone();
            for(int i=0; i<showerCluster.length; i++){
                for(int j=i+1; j<showerCluster.length; j++){
                    int airPixel = countAirPixel(gapPixel(showerCluster[i].cogId(), showerCluster[j].cogId(), viewer), showerClusterID);
                    showerCluster[i].addNeighborDistance(airPixel);
                    showerCluster[j].addNeighborDistance(airPixel);
                    if(airPixel == 0){
                        showerCluster[i].addNeighborCluster(showerCluster[j].getClusterID());
                        showerCluster[j].addNeighborCluster(showerCluster[i].getClusterID());
                    }
                    //else{System.out.println("airpixel: " + airPixel);}

                }
            }
    }






    public int countAirPixel(ArrayList<Integer> gapPixel, int[] showerClusterID){
        int countAirPixel = 0;
        for(int id : gapPixel){
            if (showerClusterID[id] == -2) {
                countAirPixel++;
            }

        }
        return countAirPixel;
    }

    // Returns an ArrayList containing the pixel-ids which build a line between two pixels. Needs the ids of the two pixel that should be connected.
    private ArrayList<Integer> gapPixel(int id1, int id2, int [] viewerArray){
        ArrayList<Integer> line = new ArrayList<>();

        int [] cube1 = mapping.getCubeCoordinatesFromId(id1);
        int [] cube2 = mapping.getCubeCoordinatesFromId(id2);

        int hexDistance = (Math.abs(cube2[0] - cube1[0]) + Math.abs(cube2[1] - cube1[1]) + Math.abs(cube2[2] - cube1[2]))/2;

        //System.out.println(hexDistance);
        double N = (double) hexDistance;

        for(int i=1; i<=hexDistance; i++){
            double [] point = linePoint(cube1, cube2, 1.0/N * i);
            long [] pixel = cube_round(point);
            FactCameraPixel linePixel = mapping.getPixelFromCubeCoordinates(pixel[0], pixel[2]);
            if (linePixel != null) {
                viewerArray[linePixel.id] = 0; // <--------------- show lines irgendwo anders!
                line.add(linePixel.id);
            }
        }

        return line;
    }



    //Returns the (double) cube coordinates for a point on the line between two pixels
    private double[] linePoint(int[] cube1, int [] cube2, double t){
        double [] linePoint = new double[3];
        linePoint[0] = cube1[0] + (cube2[0] - cube1[0])*t;
        linePoint[1] = cube1[1] + (cube2[1] - cube1[1])*t;
        linePoint[2] = cube1[2] + (cube2[2] - cube1[2])*t;

        return linePoint;
    }

    //Returns the (int) cube coordinates of the pixel which contains the (double) coordinates of some point in the coordinate system
    private long[] cube_round(double [] linePoint){
        long rx =  Math.round(linePoint[0]);
        long ry =  Math.round(linePoint[1]);
        long rz =  Math.round(linePoint[2]);

        double x_diff = Math.abs(rx - linePoint[0]);
        double y_diff = Math.abs(ry - linePoint[1]);
        double z_diff = Math.abs(rz - linePoint[2]);

        if(x_diff > y_diff && x_diff > z_diff){
            rx = -ry - rz;
        }
        else if(y_diff > z_diff){
            ry = -rx - rz;
        }
        else {
            rz = -rx - ry;
        }

        long [] linePixel = new long [3];
        linePixel[0] = rx;
        linePixel[1] = ry;
        linePixel[2] = rz;

        return linePixel;
    }

    public double neighborClusterMean(FactCluster [] showerCluster){
        double sum = 0;
        double i = 0;
        for (FactCluster c : showerCluster){
            sum += c.getNumNeighbors();
            i++;
            //System.out.println(c.getNumNeighbors());
        }
        return sum/i;
    }


    /* 1) Find the air-pixel for one cluster. Air-pixel are the pixels on a line between two clusters which don't belong to any cluster.
          (The more air-pixels between two clusters, the larger the distance between them.) Build the air-pixel-mean over all lines starting from this cluster. -> 'numAirpixel'
       2) Sum over all 'numAirpixel' (of every cluster) -> sum
          (the result should be a value for the spread/distribution of the clusters in the camera image)
      */
    public double airpixelMean(FactCluster [] showerCluster) {
        double sum = 0;
        if (showerCluster.length == 1) {
            return 0;
        } else {
            for (FactCluster c : showerCluster) {
                sum += c.getNumAirpixel() / (showerCluster.length - 1);
            }
            return sum/showerCluster.length;
        }
    }

    public void findNeighbors(FactCluster [] showerSet, int[] showerClusterID){                  //------------------------------- Neighbors testen waere sinnvoll
        //int[] numNeighbors = new int [showerSet.length];
        for(FactCluster c : showerSet){
            int clusterID = c.getClusterID();
            ArrayList<Integer> bound = c.findBoundaryNaive();
            for(int id : bound){
                FactCameraPixel [] boundPixelNeighbors = mapping.getNeighboursFromID(id);
                for(FactCameraPixel p : boundPixelNeighbors){
                    if (showerClusterID[p.id] != clusterID && showerClusterID[p.id] != -2 && !c.naiveNeighborClusterID.contains(showerClusterID[p.id])) {
                        c.naiveNeighborClusterID.add(showerClusterID[p.id]);
                    }
                }
            }
            c.numNeighbors = c.getNumNeighbors();
        }
    }

    public int numIsolatedCluster(FactCluster[] showerCluster){
        int isolatedCluster = 0;
        for(FactCluster c : showerCluster){
            if(c.numNeighbors == 0){
                isolatedCluster++;
            }
        }
        return  isolatedCluster;
    }


    public FactCluster maxCluster(FactCluster [] showerCluster){
        int maxClusterIndex = 0;
        int size = 0;
        int i = 0;
        for (FactCluster c : showerCluster) {
            if (c.getNumPixel() > size) {
                size = c.getNumPixel();
                maxClusterIndex = i;
            }
            i++;
        }

        return showerCluster[maxClusterIndex];
    }

    public double getChargeMaxCluster(FactCluster [] showerCluster){
        if(showerCluster.length == 1){
            return 1;
        }
        else {
            int maxClusterIndex = 0;
            int size = 0;
            double chargeSum = 0;
            int i = 0;
            for (FactCluster c : showerCluster) {
                chargeSum += c.getPhotonchargeSum();
                //System.out.println(c.getPhotonchargeSum());
                if (c.getNumPixel() > size) {
                    size = c.getNumPixel();
                    maxClusterIndex = i;
                }
                i++;
            }
            //System.out.println(maxClusterIndex);
            return showerCluster[maxClusterIndex].getPhotonchargeSum() / chargeSum;
        }

    }

    int numClusterPixel(FactCluster [] showerCluster){
        int sum = 0;
        for(FactCluster c : showerCluster){
            sum+= c.getNumPixel();
        }
        return sum;
    }

    public double stdArrTime(FactCluster [] showerCluster, double [] arrivaltime){
        double arrTimeMean = 0;
        double arrTimeStd = 0;
        for(FactCluster c : showerCluster){
            int maxId = c.maxPhotonchargeId();
            arrTimeMean += arrivaltime[maxId]/showerCluster.length;
        }

        for(FactCluster c : showerCluster){
            int maxId = c.maxPhotonchargeId();
            arrTimeStd += Math.pow((arrTimeMean - arrivaltime[maxId]), 2)/showerCluster.length;
        }

        return Math.sqrt(arrTimeStd);

    }

    public double stdNumPixel(FactCluster [] showerCluster){
        int numCluster = showerCluster.length;
        double mean = 0;
        double std = 0;
        for(FactCluster c : showerCluster){
            mean += c.getNumPixel()/numCluster;
        }

        for(FactCluster c : showerCluster){
            std += Math.pow((mean - c.getNumPixel()), 2)/numCluster;
        }

        return Math.sqrt(std);
    }


    /*public int[] removeClusterArray(int[] clusterID, int[] shower, int numCluster, int minShowerpixel){
        for(int c = 1; c<=numCluster; c++){
            int count = 0;
            for(int i=0; i<1440; i++){
                if(clusterID[i] == c && ArrayUtils.contains(shower, i)){
                    count++;
                }
            }
            if(count <= minShowerpixel){    //<----------------------------------------------------threshold cleaning-pixel in cluster
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
*/

}