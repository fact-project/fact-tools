package fact.features.watershed;

import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import fact.features.watershed.FactCluster;
import fact.hexmap.FactPixelMapping;
import org.apache.commons.math3.ml.clustering.Cluster;
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


    @Parameter(required = false, description = "Threshold to decide whether a pixel belongs to a cluster or not", defaultValue = "3")
    protected double threshold = 3;

    @Parameter(required = true, description = "Input key for pixel set (aka shower pixel)")
    protected String pixelSetKey = null;

    @Parameter(required = false, description = "Input key for arrivaltime positions", defaultValue = "arrivalTimePos")
    protected String arrivaltimePosKey = "arrivalTimePos";

    @Parameter(required = false, description = "Input key for calculated photon charge", defaultValue = "photoncharge")
    protected String photonchargeKey = "photoncharge";



    @Override
    public Data process(Data data) {
        System.out.println(data.get("EventNum"));
        PixelSet pixelSet = (PixelSet) data.get(pixelSetKey);
        double[] arrivalTime = ((double[]) data.get(arrivaltimePosKey));
        double[] photoncharge = ((double[]) data.get(photonchargeKey));
        double cogX = (double) data.get("COGx");
        double cogY = (double) data.get("COGy");

        //get 'shower' as int array with pixel id's from 'pixelSet' (HashSet)
        int[] shower = pixelSet.toIntArray();
        int[] showerArray = new int[1440];

        for(int p : shower){
            showerArray[p] = 1;
        }

        int[] clusterID = new int[1440];
        int[] showerClusterID = new int[1440];


        for (int i = 0; i < 1440; i++) {
            clusterID[i] = 0;
            //showerClusterID[i] = -2;
        }

        // set hard coded (random) threshold!!!!!!!!!!!!!!!!!!!!!!!!!!! First approx, should be replaced later...
        //double threshold = 1;


        boolean finishCluster = false;
        boolean pathend = false;

        int cluster = 1;

        int cog = mapping.getPixelBelowCoordinatesInMM(cogX, cogY).id;

        int seed = shower[0];
        System.out.println(seed);

        clusterID[seed] = cluster;

        int [] current_cluster  = findNearestBlankNeighbor(seed, arrivalTime, clusterID, shower,cluster, showerArray);
        int current = current_cluster[0];

        cluster = current_cluster[1];


        //Region Growing Algorithm

        while(finishCluster == false){


//            System.out.println(current);

            //get ids from neighbor pixel which have already a cluster id

            ArrayList<Integer> flaggedNeighborPixel = findNeighborCluster(current, clusterID);

            if(flaggedNeighborPixel.size() == 0){
                cluster++;
                clusterID[current] = cluster;
                current_cluster = findNextSeed(shower, clusterID, cluster, arrivalTime, showerArray);
                current = current_cluster[0];
                cluster = current_cluster[1];
            }
            else {
                double minDiff = Math.abs(arrivalTime[current] - arrivalTime[flaggedNeighborPixel.get(0)]);
                int minClusterID = clusterID[flaggedNeighborPixel.get(0)];
                int minPixelID = flaggedNeighborPixel.get(0);

                System.out.println(1);

                if (flaggedNeighborPixel.size() > 1) {


                    for (int c = 1; c < flaggedNeighborPixel.size(); c++) {
                        double diff = Math.abs(arrivalTime[current] - arrivalTime[flaggedNeighborPixel.get(c)]);
                        if (diff < minDiff) {
                            minDiff = diff;
                            //clusterID for pixel with the minimal difference in arrival time:
                            minClusterID = clusterID[flaggedNeighborPixel.get(c)];
                            minPixelID = flaggedNeighborPixel.get(c);
                        }
                    }
                }

                System.out.println(2);

                if (minDiff < threshold) {
                    clusterID[current] = minClusterID;
                    System.out.println("Main 2 calls");
                    int[] temp = findNearestBlankNeighbor(current, arrivalTime, clusterID, shower, cluster, showerArray);
                    current = temp[0];
                    cluster = temp[1];
                    if (current == -1) {
                        System.out.println("finish1");
                        finishCluster = true;
                    }
                } else {
                    System.out.println(3);
                    if (showerArray[current] == 1) {
                        System.out.println(4);
                        System.out.println(cluster);
                        cluster++;
                        System.out.println(cluster);
                        clusterID[current] = cluster;
                        System.out.println("Main 4 calls");
                        int[] temp = findNearestBlankNeighbor(current, arrivalTime, clusterID, shower, cluster, showerArray);
                        System.out.println(cluster);
                        current = temp[0];
                        cluster = temp[1];
                        System.out.println(cluster);
                        if (current == -1) {
                            System.out.println("finish2");
                            finishCluster = true;
                        }
                    } else {
                        System.out.println(5);
                        int[] temp = findNextSeed(shower, clusterID, cluster, arrivalTime, showerArray);
                        current = temp[0];
                        cluster = temp[1];
                        //cluster++;
                        if (current == -1) {
                            System.out.println("finish3");
                            finishCluster = true;
                        }
                    }
                }
            }

            System.out.print("cluster: ");
            System.out.println(cluster);

        }

        //Clustering done
        System.out.print("cluster fertig: ");
        System.out.println(cluster);


        FactCluster[]  clusterSet = new FactCluster[cluster+1];
        for (int i=0; i<=cluster; i++){
            clusterSet[i] = new FactCluster();
            clusterSet[i].setClusterID(i);
        }

        for(int i=0;i<1440;i++){
            //System.out.println(i + "\t" + clusterID[i]);
            clusterSet[clusterID[i]].addContentPixel(i);
            clusterSet[clusterID[i]].addContentPixelPhotoncharge(photoncharge[i]);
            clusterSet[clusterID[i]].addContentPixelArrivaltime(arrivalTime[i]);

        }
//
//        //add showerpixel in a cluster to list
//        for(int i=0; i<shower.length;i++){
//            clusterSet[clusterID[shower[i]]].addCleaningPixel(shower[i]);
//        }
//
//        // remove one pixel cluster???????????
//
//        double ratioAT = ClusterFellwalker.boundContentRatio(clusterSet);
//        //double idealBoundDiffAT = ClusterFellwalker.idealBoundDiff(clusterSet);
//        //double boundAngleSumAT = ClusterFellwalker.boundAngleSum(clusterSet);
//        //double distanceCenterAT = ClusterFellwalker.distanceCenter(clusterSet);
//        //double chargeMaxClusterRatioAT = ClusterFellwalker.getChargeMaxCluster(clusterSet);
//        //double stdNumpixel = ClusterFellwalker.stdNumPixel(clusterSet);
//
//
//
//
//
//
//        //System.out.println(cluster);
//
//        data.put("boundRatioAT", ratioAT);
////        data.put("idealBoundDiffAT", idealBoundDiffAT);
////        data.put("boundAngleAT", boundAngleSumAT);
////        data.put("distanceCenterAT", distanceCenterAT);
////        data.put("chargeMaxAT", chargeMaxClusterRatioAT);
////        data.put("stdNumpixel", stdNumpixel);
        data.put("ArrrialTimeClusterID", clusterID);
//        data.put("numClusterAT", cluster);


        return data;

    }

    private int[] findNextSeed(int[] shower, int [] clusterID, int cluster, double[] arrivalTime, int[] showerArray) {
        int seed = -1;
        boolean foundSeed = false;
        int p = 0;
        while(foundSeed == false && p<shower.length){
            int showerPixelID = shower[p];
            if (clusterID[showerPixelID] == 0) {
                FactCameraPixel[] neighbors = mapping.getNeighboursFromID(showerPixelID);
                boolean foundFlaggedNeighbor = false;
                int n = 0;
                while(foundFlaggedNeighbor == false && n < neighbors.length){
                    if(clusterID[neighbors[n].id] != 0){
                        foundFlaggedNeighbor = true;
                        seed = showerPixelID;
                        foundSeed = true;
                    }
                    n++;
                }

            }
            p++;
        }

        if(seed == -1){
            // foundSeed = false;
            int k = 0;
            while(foundSeed == false && k<shower.length){
                int pixel = shower[k];
                if (clusterID[pixel] == 0) {
                    foundSeed = true;
                    clusterID[pixel] = cluster+1;
                    cluster = cluster+1;
                    System.out.println("findNextSeed calls");
                    int [] current_cluster = findNearestBlankNeighbor(p, arrivalTime, clusterID, shower, cluster, showerArray);
                    seed = current_cluster[0];
                    cluster = current_cluster[1];

                    System.out.println("findNextSeed: " + "  " +cluster);

                }
                k++;
            }

        }

        return new int[] {seed, cluster};

    }

    private ArrayList findNeighborCluster(int id, int[] clusterID) {
        ArrayList<Integer> neighborCluster = new ArrayList<>();
        FactCameraPixel[] neighbors = mapping.getNeighboursFromID(id);

        for(FactCameraPixel p : neighbors){
            if(!neighborCluster.contains(clusterID[p.id]) && clusterID[p.id] != 0){
                neighborCluster.add(p.id);
            }
        }

        return neighborCluster;
    }



    private int [] findNearestBlankNeighbor(int current, double[] arrivalTime, int[] clusterID, int[] shower, int cluster, int[] showerArray) {
        FactCameraPixel[] neighbors = mapping.getNeighboursFromID(current);
        double minDiff = 1000;
        int minID = -1;

        for(int i=0; i<neighbors.length; i++) {
            if (clusterID[neighbors[i].id] == 0 && showerArray[neighbors[i].id] == 1) {

                double diff = Math.abs(arrivalTime[neighbors[i].id] - arrivalTime[current]);
                if (diff < minDiff) {
                    minDiff = diff;
                    minID = neighbors[i].id;
                }
            }
        }

        if(minID == -1){
           int [] temp = findNextSeed(shower,clusterID,cluster,arrivalTime, showerArray);

            minID = temp[0];
            cluster = temp[1];
            System.out.println("findNeartestBlankNeighbor: " + "  " + cluster);
        }
        //System.out.println(minID);
        return new int [] {minID, cluster};
    }



    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }
    public void setArrivaltimePosKey(String arrivaltimePosKey){this.arrivaltimePosKey = arrivaltimePosKey;}
    public void setPhotonchargeKey(String photonchargeKey){this.photonchargeKey = photonchargeKey;}

}