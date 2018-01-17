package fact.features.watershed;

import fact.Constants;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * This class clusters shower pixels(!) by their arrival times. In this case a region growing algorithm is used, which
 * works like
 * 1. Find seed (= an unflagged shower pixel). For the first iteration the first shower pixel is used as seed (shower[0]).
 * This is kind of a random seed and could cause different cluster results for the same event for different runs!
 * 2. Find all neighbor pixel which have the smallest difference in arrival time. This pixel is the 'current' pixel.
 * <p>
 * 3. Find all flagged neighbor pixel for the 'current' pixel.
 * 4. Search for the flagged pixel which has the smallest difference in arrival time.
 * 5. Check if the smallest difference is under a threshold (hardcode at the moment, could be variable, maybe std of all arrival times in cluster so far?)
 * 6. difference < threshold: current pixel gets the same cluster id as the neighbor pixel with this difference
 * difference > threshold: current pixel gets a new cluster ID
 * 7. Search for the next current or seed pixel to cluster. Keep in mind that only shower pixels should be clustered.
 * --> iterate step 3. - 7. until all shower pixels have a cluster ID
 * <p>
 * <p>
 * Created by lena on 09.02.16.
 */


public class ClusterArrivalTimes implements Processor {

    FactPixelMapping mapping = FactPixelMapping.getInstance();

    @Parameter(required = false, description = "Threshold to decide whether a pixel belongs to a cluster or not", defaultValue = "3")
    public double threshold = 3;

    @Parameter(required = true, description = "Input key for pixel set (aka shower pixel)")
    public String showerKey = null;

    @Parameter(required = false, description = "Input key for arrivaltime positions", defaultValue = "arrivalTimePos")
    public String arrivaltimePosKey = "arrivalTimePos";

    @Parameter(required = false, description = "Input key for calculated photon charge", defaultValue = "photoncharge")
    public String photonchargeKey = "photoncharge";


    @Override
    public Data process(Data data) {
        PixelSet pixelSet = (PixelSet) data.get(showerKey);
        double[] arrivalTime = ((double[]) data.get(arrivaltimePosKey));
        double[] photoncharge = ((double[]) data.get(photonchargeKey));


        //get 'shower' as int array with pixel id's from 'pixelSet' (HashSet)
        int[] shower = pixelSet.toIntArray();
        int[] showerArray = new int[Constants.N_PIXELS];

        for (int p : shower) {
            showerArray[p] = 1;
        }

        int[] clusterID = new int[Constants.N_PIXELS];
        for (int i = 0; i < Constants.N_PIXELS; i++) {
            clusterID[i] = 0;
        }

        // set hard coded (random) threshold!!!!!!!!!!!!!!!!!!!!!!!!!!! First approx, should be replaced later...

        //ArrayList<FactCluster> clusterList = new ArrayList<>();

        //clusterList.add(0,new FactCluster());
        //clusterList.get(0).setClusterID(1);


        boolean finishCluster = false;
        int cluster = 1;


        // Random, shower is not sorted in any way. Causes different clustering depending from the first seed.
        int seed = shower[0];

        clusterID[seed] = cluster;
        //clusterList.get(0).addContentPixel(seed);

        int[] current_cluster = findNearestBlankNeighbor(seed, arrivalTime, clusterID, shower, cluster, showerArray);
        int current = current_cluster[0];

        cluster = current_cluster[1];


        //Region Growing Algorithm

        while (!finishCluster) {

            //get ids from neighbor pixel which have already a cluster id

            ArrayList<Integer> flaggedNeighborPixel = findNeighborCluster(current, clusterID);

            if (flaggedNeighborPixel.size() == 0) {
                cluster++; //???????

                clusterID[current] = cluster;
            } else {
                double minDiff = Math.abs(arrivalTime[current] - arrivalTime[flaggedNeighborPixel.get(0)]);
                int minClusterID = clusterID[flaggedNeighborPixel.get(0)];

                if (flaggedNeighborPixel.size() > 1) {

                    for (int c = 1; c < flaggedNeighborPixel.size(); c++) {
                        double diff = Math.abs(arrivalTime[current] - arrivalTime[flaggedNeighborPixel.get(c)]);
                        if (diff < minDiff) {
                            minDiff = diff;
                            //clusterID for pixel with the minimal difference in arrival time:
                            minClusterID = clusterID[flaggedNeighborPixel.get(c)];
                        }
                    }
                }


                if (minDiff < threshold) {
                    clusterID[current] = minClusterID;
                } else {
                    cluster++;
                    clusterID[current] = cluster;
                }
            }

            int[] temp = findNearestBlankNeighbor(current, arrivalTime, clusterID, shower, cluster, showerArray);
            current = temp[0];
            cluster = temp[1];
            if (current == -1) {
                finishCluster = true;
            }
        }


        //Clustering done

        FactCluster[] clusterSet = new FactCluster[cluster];
        for (int i = 0; i < cluster; i++) {
            clusterSet[i] = new FactCluster();
            clusterSet[i].setClusterID(i + 1);
        }

        for (int pixel : shower) {

            clusterSet[clusterID[pixel] - 1].addContentPixel(pixel);
            clusterSet[clusterID[pixel] - 1].addContentPixelPhotoncharge(photoncharge[pixel]);
            clusterSet[clusterID[pixel] - 1].addContentPixelArrivaltime(arrivalTime[pixel]);

        }

//        // remove one pixel cluster???????????
//
        double boundRatioAT = ClusterFellwalker.boundContentRatio(clusterSet);
        double idealBoundDiffAT = ClusterFellwalker.idealBoundDiff(clusterSet);
        double distanceCenterAT = ClusterFellwalker.distanceCenter(clusterSet);
        double chargeMaxClusterRatioAT = ClusterFellwalker.getChargeMaxCluster(clusterSet);
        double stdNumpixel = ClusterFellwalker.stdNumPixel(clusterSet);


        data.put("boundRatioAT", boundRatioAT);
        data.put("idealBoundDiffAT", idealBoundDiffAT);
        data.put("distanceCenterAT", distanceCenterAT);
        data.put("chargeMaxAT", chargeMaxClusterRatioAT);
        data.put("stdNumPixelAT", stdNumpixel);
        data.put("ArrrialTimeClusterID", clusterID);
        data.put("numClusterAT", cluster);


        return data;

    }

    private int[] findNextSeed(int[] shower, int[] clusterID, int cluster, double[] arrivalTime, int[] showerArray) {
        int seed = -1;
        boolean foundSeed = false;
        int p = 0;
        while (foundSeed == false && p < shower.length) {
            int showerPixelID = shower[p];
            if (clusterID[showerPixelID] == 0) {
                CameraPixel[] neighbors = mapping.getNeighborsFromID(showerPixelID);
                boolean foundFlaggedNeighbor = false;
                int n = 0;
                while (foundFlaggedNeighbor == false && n < neighbors.length) {
                    if (clusterID[neighbors[n].id] != 0) {
                        foundFlaggedNeighbor = true;
                        seed = showerPixelID;
                        foundSeed = true;
                    }
                    n++;
                }

            }
            p++;
        }

        if (seed == -1) {
            // foundSeed = false;
            int k = 0;
            while (foundSeed == false && k < shower.length) {
                int pixel = shower[k];
                if (clusterID[pixel] == 0) {
                    foundSeed = true;
                    clusterID[pixel] = cluster + 1;
                    cluster = cluster + 1;

                    int[] current_cluster = findNearestBlankNeighbor(p, arrivalTime, clusterID, shower, cluster, showerArray);
                    seed = current_cluster[0];
                    cluster = current_cluster[1];


                }
                k++;
            }

        }

        return new int[]{seed, cluster};

    }

    private ArrayList<Integer> findNeighborCluster(int id, int[] clusterID) {
        ArrayList<Integer> neighborCluster = new ArrayList<>();
        CameraPixel[] neighbors = mapping.getNeighborsFromID(id);

        for (CameraPixel p : neighbors) {
            if (!neighborCluster.contains(clusterID[p.id]) && clusterID[p.id] != 0) {
                neighborCluster.add(p.id);
            }
        }

        return neighborCluster;
    }


    private int[] findNearestBlankNeighbor(int current, double[] arrivalTime, int[] clusterID, int[] shower, int cluster, int[] showerArray) {
        CameraPixel[] neighbors = mapping.getNeighborsFromID(current);
        double minDiff = 1000;
        int minID = -1;

        for (CameraPixel n : neighbors) {
            if (clusterID[n.id] == 0 && showerArray[n.id] == 1) {

                double diff = Math.abs(arrivalTime[n.id] - arrivalTime[current]);
                if (diff < minDiff) {
                    minDiff = diff;
                    minID = n.id;
                }
            }
        }

        if (minID == -1) {
            int[] temp = findNextSeed(shower, clusterID, cluster, arrivalTime, showerArray);

            minID = temp[0];
            cluster = temp[1];

        }

        return new int[]{minID, cluster};
    }
}
