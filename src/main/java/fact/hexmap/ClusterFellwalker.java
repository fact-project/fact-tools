package fact.hexmap;

/**
 * Created by lena on 16.11.15.
 */

import org.jfree.ui.about.SystemPropertiesTableModel;
import stream.Data;
import stream.Processor;

import java.util.ArrayList;

//package fact.hexmap;

import org.apache.commons.lang3.ArrayUtils;
import stream.Data;
import stream.Processor;

import java.util.ArrayList;

public class ClusterFellwalker implements Processor {

    FactPixelMapping mapping = FactPixelMapping.getInstance();

    ArrayList<Integer> aktuellerPfad = new ArrayList<>();



    @Override
    public Data process(Data data) {
        int[] shower = ((int[]) data.get("shower"));
        double[] arrivalTime = ((double[]) data.get("ArrtimePos"));
        double[] photoncharge = ((double[]) data.get("photoncharge"));
/*        double cogX = (double) data.get("COGx");
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

        for(int i=0; i<shower.length;i++){
            clusterSet[clusterID[shower[i]]].addCleaningPixel(shower[i]);
        }

        //set containsShowerPixel to false if cluster comtains less the minShowerpixel showerpixel and count all "surviving" cluster
        int numCluster = removeCluster(clusterSet,minShowerpixel);



        //build showerpixelArray that contains only cluster with showerpixel (just to have a quick look at it in the event viewer)
        for(FactCluster c : clusterSet){
            if(c.getShowerLabel() == true){
                for(int i : c.contentPixel){
                    showerClusterID[i] = c.getClusterID();
                }
            }
        }

        for(FactCluster c : clusterSet){
            //System.out.println(c.cogId());
            if(c.getShowerLabel() == true) {
                showerClusterID[c.cogId()] = -5;
            }
        }

        markBoundaryPixel(clusterSet, showerClusterID);



        data.put("AllClusterID", clusterID);
        data.put("ShowerClusterID", showerClusterID);
        data.put("ClusterNoCleaning", cluster);
        data.put("NumCluster", numCluster);

/*            data.put("ChargeMax", chargeMaxClusterRatio);
            data.put("SizeCluster1", clusterSize[1]);
            data.put("NumClusterPixel", numClusterPixel);
            data.put("IsolatedCluster", isolatedCluster);
            data.put("StdArrivaltimeMaxima", stdArrivaltimeMaxima);
            data.put("ClusterSizeStd", clusterSizeStd);*/


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
        //System.out.println("Next" + next);
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

    //give all pixel from path the same new clusterID
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

    public int removeCluster(FactCluster clusterSet[], int minShowerpixel){
        int numShowerCluster = 0;
        int newID = 1;
        for(int c=1; c<clusterSet.length;c++){
            if(clusterSet[c].cleaningPixel.size()>minShowerpixel){
                clusterSet[c].setShowerLabel(true);
                clusterSet[c].setClusterID(newID);
                newID++;
                numShowerCluster++;
            }
            else{
                clusterSet[c].setShowerLabel(false);
            }
        }



        return numShowerCluster;

    }

    public void markBoundaryPixel(FactCluster[] clusterSet, int[] showerClusterID){
        for(FactCluster c : clusterSet){
            if(c.getShowerLabel() == true){
            int[] boundPixel = c.boundaryIds();
                for (int i:boundPixel){
                   showerClusterID[i] = -7;

                }
            }
        }
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