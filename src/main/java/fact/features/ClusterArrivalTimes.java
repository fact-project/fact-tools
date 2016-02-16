package fact.features;

import fact.container.PixelSet;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactCluster;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Created by lena on 09.02.16.
 */




public class ClusterArrivalTimes implements Processor {

    FactPixelMapping mapping = FactPixelMapping.getInstance();

    ArrayList<Integer> aktuellerPfad = new ArrayList<>();


    @Parameter(required = false, description="Minimal number of pixels a cluster must contain to be labeled as 'showerCluster'", defaultValue="2")
    protected int minShowerpixel = 2;

    @Parameter(required = true, description = "Input key for pixel set (aka shower pixel)")
    protected String pixelSetKey = null;

    @Parameter(required = false, description = "Input key for arrivaltime positions", defaultValue = "arrivalTimePos")
    protected String arrivaltimePosKey = "arrivalTimePos";

    @Parameter(required = false, description = "Input key for calculated photon charge", defaultValue = "photoncharge")
    protected String photonchargeKey = "photoncharge";

    @Parameter(required = false, description = "Label: 0 = Proton, 1 = Gamma", defaultValue = "-1")
    protected int label = -1;




    @Override
    public Data process(Data data) {
        PixelSet pixelSet = (PixelSet) data.get(pixelSetKey);
        double[] arrivalTime = ((double[]) data.get(arrivaltimePosKey));
        double[] photoncharge = ((double[]) data.get(photonchargeKey));
        double cogX = (double) data.get("COGx");
        double cogY = (double) data.get("COGy");

        //get 'shower' as int array with pixel id's from 'pixelSet' (HashSet)
        int [] shower = pixelSet.toIntArray();

        int[] clusterID = new int[1440];
        int[] showerClusterID = new int[1440];



        for (int i = 0; i < 1440; i++) {
            clusterID[i] = 0;
            showerClusterID[i] = -2;
        }


        int startPath = NextStartPixel(clusterID);

        int cluster = 1;

        //FellWalker
        while (startPath != -1) {
            int highestNeighbourID;
            int currentPixel = startPath;
            boolean pathend = false;

            aktuellerPfad.add(startPath);

            while (!pathend) {
                //find neighbours and highest neighbour
                FactCameraPixel[] neighbours = mapping.getNeighboursFromID(currentPixel);

                //find usable neighbours (pixel marked with clusterID = 0 after cleaning)
                ArrayList<FactCameraPixel> usableNeighbours = new ArrayList<>();


                highestNeighbourID = findMaxValueNeighbour(neighbours, currentPixel, arrivalTime);

                aktuellerPfad.add(highestNeighbourID);

                if (highestNeighbourID == currentPixel) {
                    int highestNeighbourIDLarge = findMaxValueLargeNeighbour(currentPixel, arrivalTime);

                    if (highestNeighbourIDLarge != currentPixel) {

                        if (clusterID[highestNeighbourIDLarge] != 0) {
                            pathToExistingCluster(clusterID, aktuellerPfad, clusterID[highestNeighbourIDLarge]);
                            pathend = true;
                        } else {
                            currentPixel = highestNeighbourIDLarge;
                        }
                    } else {
                        pathToNewCluster(clusterID, aktuellerPfad, cluster);
                        cluster++;
                        pathend = true;
                    }

                } else {
                    if (clusterID[highestNeighbourID] != 0) {

                        pathToExistingCluster(clusterID, aktuellerPfad, clusterID[highestNeighbourID]);
                        pathend = true;

                    } else {
                        currentPixel = highestNeighbourID;
                    }
                }
            }
            startPath = NextStartPixel(clusterID);
        }
        //end FellWalker

        /* create a FactCluster array which contains all cluster as objects. The array-index of a cluster is equal to the
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


        /* Here the parameter minShowerpixel is used. Label all clusters with true (containsShowerPixel) if they contain at least 'minShowerpixel' showerpixel
         * (pixel that survive the cleaning) and false if they contain less showerpixel. The clusters that are labeled 'true' get a new successive id.
         * All of these clusters are put to a new FactCluster array 'showerCluster'.
        */
        FactCluster[] showerCluster = removeCluster(clusterSet,minShowerpixel);

        int numCluster = showerCluster.length;


        /* build 1440-int array that contains the id's of the cluster that survive 'removeCluster' for every pixel.
         * (Just to have a quick look at it in the event viewer, not relevant for the algorithm itself or the resulting event parameters.)
        */
        for(FactCluster c : clusterSet){
            if(c.getShowerLabel()){
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
        /*if(numCluster != 0) {
            double ratio = boundContentRatio(showerCluster);
            double idealBoundDiff = idealBoundDiff(showerCluster);
            double boundAngleSum = boundAngleSum(showerCluster);
            double distanceCenterSum = distanceCenter(showerCluster);

            int convexity = searchForCompactGroups(showerCluster, showerClusterID);


            findNeighbors(showerCluster, showerClusterID);

            double numNeighborCluster = neighborClusterMean(showerCluster);

            double chargeMaxClusterRatio = getChargeMaxCluster(showerCluster);

            int numPixelMaxCluster = maxCluster(showerCluster).getNumPixel();

            int numClusterPixel = numClusterPixel(showerCluster);


            double stdNumpixel = stdNumPixel(showerCluster);


            data.put("boundRatio", ratio);
            data.put("idealBoundDiff", idealBoundDiff);
            data.put("boundAngleSum", boundAngleSum);
            data.put("distanceCenterSum", distanceCenterSum);
            data.put("neighborCluster", numNeighborCluster);
            data.put("chargeMax", chargeMaxClusterRatio);
            data.put("maxClusterNumPixel", numPixelMaxCluster);
            data.put("numClusterPixel", numClusterPixel);
            data.put("stdNumPixel", stdNumpixel);
            data.put("convexity", convexity);


        }
        else{
            data.put("boundRatio", null);
            data.put("idealBoundDiff", null);
            data.put("boundAngleSum", null);
            data.put("distanceCenterSum", null);
            data.put("neighborCluster", null);
            data.put("chargeMax", null);
            data.put("maxClusterNumPixel", null);
            data.put("numClusterPixel", null);
            data.put("stdNumPixel", null);
            data.put("convexity", null);


        }*/

        if(numCluster != 0 ){
            double stdArrivaltime = stdArrivaltime(showerCluster);
            double ratio = boundContentRatio(showerCluster);
            double idealBoundDiff = idealBoundDiff(showerCluster);
            //double boundAngleMean = boundAngleSum(showerCluster);
            double distanceCogMean = distanceCog(showerCluster, cogX, cogY);

            data.put("stdArrivaltime", stdArrivaltime);
            data.put("boundRatioAT", ratio);
            data.put("idealBoundDiffAT", idealBoundDiff);
            //data.put("boundAngleSumAT", boundAngleMean);
            data.put("distanceCogMeanAT", distanceCogMean);



        }

        else{
            data.put("stdArrivaltime", null);
            data.put("boundRatioAT", null);
            data.put("idealBoundDiffAT", null);
            //data.put("boundAngleSumAT", null);
            data.put("distanceCogMeanAT", null);

        }
        data.put("AllClusterIDAT", clusterID);
        data.put("ShowerClusterIDAT", showerClusterID);
        data.put("ClusterNoCleaningAT", cluster);
        data.put("numClusterAT", numCluster);





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

    //find highest neighbour, return the currentPixel if there is no higher neighbour!!
    public int findMaxValueNeighbour(FactCameraPixel [] neighbours, int currentPixel, double[] arrTimes) {

        double maxValue = arrTimes[currentPixel];
        int maxValueID = currentPixel;

        for (FactCameraPixel n : neighbours) {
            if (arrTimes[n.id] > maxValue) {
                maxValue = arrTimes[n.id];
                maxValueID = n.id;
            }
        }
        return maxValueID;
    }

    //find brightest neighbour in large neighbourhood, return the currentPixel if there is no brighter neighbour!!
    public int findMaxValueLargeNeighbour(int currentPixel, double[] arrTimes) {
        FactCameraPixel[] largeNeighbours = mapping.getSecondOrderNeighboursFromID(currentPixel);

        double maxValue = arrTimes[currentPixel];
        int maxValueID = currentPixel;

        for (FactCameraPixel n : largeNeighbours) {
            if (arrTimes[n.id] > maxValue) {
                maxValue = arrTimes[n.id];
                maxValueID = n.id;
            }
        }
        return maxValueID;
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


    public FactCluster [] removeCluster(FactCluster clusterSet[], int minShowerpixel){
        ArrayList<FactCluster> showerCluster = new ArrayList<>();
        int numShowerCluster = 0;
        int newID = 1;
        for(FactCluster c : clusterSet){
            if(c.cleaningPixel.size()>minShowerpixel){
                c.setShowerLabel(true);
                c.setClusterID(newID);
                newID++;
                numShowerCluster++;
                showerCluster.add(c);
            }
            else{
                c.setShowerLabel(false);
            }
        }

        FactCluster [] showerClusterArray = new FactCluster[numShowerCluster];
        for(int i=0; i<numShowerCluster; i++){
            showerClusterArray[i] = showerCluster.get(i);
        }
        return showerClusterArray;

    }


    public double stdArrivaltime(FactCluster [] showerCluster){
        double mean = 0;
        double std = 0;
        for(FactCluster c : showerCluster){
            mean = mean + c.meanArrivaltime()/showerCluster.length;
        }

        for(FactCluster c : showerCluster){
            std = std + Math.pow((mean - c.meanArrivaltime()), 2)/showerCluster.length;
        }

        return Math.sqrt(std);
    }





























    // not needed for parameter calculation, just to have a quick look in the viewer
/*    public void markBoundaryPixel(FactCluster[] clusterSet, int[] showerClusterID){
        for(FactCluster c : clusterSet){
            if(c.getShowerLabel()){
            ArrayList<Integer> boundPixel = c.findBoundaryNaive();
                showerClusterID[boundPixel.get(0)] = -1;
                for (int i=1; i<boundPixel.size(); i++){

                    showerClusterID[boundPixel.get(i)] = 0;

                }
            }
        }
    }*/


    /* Calculates the ratio of number of bound pixel to number of content pixels and sums it up for all clusters.
     * So this feature is correlated to the number of clusters in the event ('NumCluster'). One could divide the returned
     * 'ratio' by 'showerCluster.length' to have a normed value.
     * Idea behind is to have a value for the shape of a cluster.
     */
    public double  boundContentRatio(FactCluster [] showerCluster){
        double ratio = 0;

        for(FactCluster c : showerCluster){
            ratio += (double) c.getBoundaryLength()/c.getNumPixel();
        }

        return ratio/showerCluster.length;
    }


    /*
     * Sums up the 'idealBoundDiff' of every cluster in the event. The 'ideal boundary' is the minimum number of boundary pixels,
     * which means the cluster would have the shape of a circle. 'idealBoundDiff' means therefore the difference between the real number
     * of boundary pixels and the ideal(minimal) number of boundary pixels a cluster with a certain number of pixel could have.
     * Keep in mind that this feature is again correlated to 'NumCluster', as long as the returned 'sum' is not divided by 'showerCluster.length'.
     */
    public double idealBoundDiff(FactCluster [] showerCluster){
        double sum = 0;
        for (FactCluster c : showerCluster){
            sum += c.idealBoundDiff();
        }
        return sum;
    }

    /*
     * Sums up the 'boundAngleSum' for all clusters in the event. 'boundAngleSum' does something like count how often you
     * have to change the direction (on a hexagonal coordinate system) if you walk along the boundary of the cluster.
     * This is another feature that should describe the shape of a cluster. If a cluster has kind of a 'smooth' shape
     * (like circle or ellipse) the 'boundAngleSum' should be smaller than the value for a cluster with a irregular random
     * splashy shape...(Again: correlation to 'NumCluster' if not dividing by 'showerCluster.length'.)

     */
    public double boundAngleSum(FactCluster[] showerCluster){
        double sum = 0;
        for (FactCluster c : showerCluster){
            sum += c.boundAngleSum();
        }
        return sum/showerCluster.length;
    }


    /*
     * Returns the mean over all distances from all cluster center of gravity to the camera center (the center position
     * in mm not the 'center pixel'). Gives an information about the geometrical distribution of the clusters in the camera image.
     */
    public double distanceCog(FactCluster[] showerCluster, double cogX, double cogY){
        double sum = 0;
        for (FactCluster c : showerCluster){
            sum += c.distanceCog(cogX, cogY);
        }
        return sum/showerCluster.length;
    }



    /** Method to search for all neighbor clusters of all clusters in the camera image. Two clusters are neighbors if there are no air pixels on the line between their cog's.
     * "Air pixels" are the pixel on this line that don't belong to any shower-cluster. From the number of air-pixel one can conclude
     * whether the clusters are neighbors, and, if not, how large the distance is between them.
     * At the moment two clusters are marked as neighbors if there are no air-pixels between them.
     * Keep in mind that currently clusters are marked as neighbors even if they are "indirect neighbors" (means they have a third cluster between them). In this case there are also no air pixel on the line
     * between their cog's, because all pixel on this line belongs to a cluster.
     * But maybe this is an opportunity to define another parameter for the whole image, something like "convexity". If there are no air pixel in the image at all, the group of clusters could be defined as convex.
     * This makes sense probably only for images with more than two clusters.
     * Returns the sum over all found air pixels as a parameter for convexity.
     *
     * Maybe it's not necessary to fill the distances between the clusters in lists... the resulting 'number of neighbors' from this method isn't really a number of neighbors (as found in findNeighbors);
     * it's more like an estimation for the compactness of the clusters (how many clusters build a compact/connected group in the image). Therefore 'neighborDistance' and 'neighborClusters' could be misleading...
     */
    public int searchForCompactGroups(FactCluster [] showerCluster, int [] showerClusterID){
        //int[][] map = new int [showerCluster.length][showerCluster.length];
        //int [] viewer = showerClusterID.clone();
        int sumAirpixel = 0;
        for(int i=0; i<showerCluster.length; i++){
            for(int j=i+1; j<showerCluster.length; j++){
                int airPixel = countAirPixel(mapping.line(showerCluster[i].cogId(), showerCluster[j].cogId()), showerClusterID);
                showerCluster[i].addAirDistance(airPixel);
                showerCluster[j].addAirDistance(airPixel);
                if(airPixel == 0){
                    showerCluster[i].addCompactCluster(showerCluster[j].getClusterID());
                    showerCluster[j].addCompactCluster(showerCluster[i].getClusterID());
                }
                else{sumAirpixel += airPixel;}

            }
        }

        return sumAirpixel;
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

    /*
     * Mean over the number of (naive) neighbors for all clusters in an event.
     */
    public double neighborClusterMean(FactCluster [] showerCluster){
        double sum = 0;
        for (FactCluster c : showerCluster){
            sum += c.getNumNeighbors();
        }
        return sum/showerCluster.length;
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


    //Possible, but not a good feature. Maybe nice to have for further ideas...
/*    public int numIsolatedCluster(FactCluster[] showerCluster){
        int isolatedCluster = 0;
        for(FactCluster c : showerCluster){
            if(c.numNeighbors == 0){
                isolatedCluster++;
            }
        }
        return  isolatedCluster;
    }*/


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
                if (c.getNumPixel() > size) {
                    size = c.getNumPixel();
                    maxClusterIndex = i;
                }
                i++;
            }
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


    //Possible, but not a good feature. Arrival times are therefore not use for feature creation so far.
/*    public double stdArrTime(FactCluster [] showerCluster, double [] arrivaltime){
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

    }*/

    // Standard deviation of the mean over the number of pixels in every cluster in the event.
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


    public void setMinShowerpixel(int minShowerpixel) {
        this.minShowerpixel = minShowerpixel;
    }
    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }
    public void setArrivaltimePosKey(String arrivaltimePosKey){this.arrivaltimePosKey = arrivaltimePosKey;}
    public void setPhotonchargeKey(String photonchargeKey){this.photonchargeKey = photonchargeKey;}
    public void setLabel(int label){this.label = label;}

}