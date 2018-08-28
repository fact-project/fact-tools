package fact.features.watershed;

/**
 * Watershed algorithm to cluster the camera image.
 * The image is interpreted as "landscape" with hills and valleys, where the photoncharge is used as height of a pixel.
 * FellWalker algorithm clusters the pixels by grouping all pixels which belongs to a hill. The algorithm starts at a pixel
 * and searches in the neighborhood for the highest pixel. From this neighbor it searches for the next higher pixel in the
 * neighborhood and so on, until there is no higher pixel and the top of the hill is reached. Every pixel, which is used
 * during a path to the top is added to a list. If the path ends at the top of a hill, every pixel on this list is marked
 * with the same cluster ID. Every path to a top gets another cluster ID. If a path reaches a pixel which has already a clusterID
 * the path up to this pixel is marked with the same cluster ID, because it would lead to the top of the same hill.
 * After all pixels are used for a path, the whole image is clustered. In the last step all clusters are removed that contains
 * less than 'minShowerpixel' cleaning pixels.
 * <p>
 * edit:
 * morphologyKey:
 * FellWalker works on the morphology you choose. Photoncharge would be the most intuitive one, but other pixel values like
 * arrival times or mean correlation are possible. 'photoncharge' is default.
 * areaKey:
 * Define the area which should be segmented. By default the whole camera image (all 1440 pixels) are clustered (areaKey = null),
 * but one can choose another pixel set that should be used for clustering (e.g. cleaning set).
 * showerKey:
 * For areaKey = null (whole image clustered) only clusters are kept, if they contain 'minShowerpixel' cleaning pixel, which
 * are collected in a pixelSet named showerKey (e.g. output "shower" from TwoLevelTimeNeighbor or other pixel sets created by other cleaning algorithms).
 * <p>
 * Created by lena on 16.11.15.
 */

import fact.Constants;
import fact.Utils;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

//package fact.hexmap;


public class ClusterFellwalker implements Processor {

    FactPixelMapping mapping = FactPixelMapping.getInstance();

    ArrayList<Integer> aktuellerPfad = new ArrayList<>();


    @Parameter(required = false, description = "Minimal number of pixels a cluster must contain to be labeled as 'showerCluster'", defaultValue = "2")
    public int minShowerpixel = 2;

    @Parameter(required = true, description = "Input key for pixel set (aka shower pixel). Used to keep/remove cluster if areaKey=null.")
    public String showerKey = null;

    @Parameter(required = false, description = "Input key for arrivaltime positions", defaultValue = "arrivalTimePos")
    public String arrivaltimePosKey = "arrivalTimePos";

    @Parameter(required = false, description = "Input key for calculated photon charge", defaultValue = "photoncharge")
    public String photonchargeKey = "photoncharge";

    @Parameter(required = false, description = "Value chosen for clustering. Could be photoncharge, arrival times or mean correlation.", defaultValue = "photoncharge")
    public String morphologyKey = photonchargeKey;

    @Parameter(required = false, description = "Input key for soure position", defaultValue = "sourcePositionKey")
    public String sourcePositionKey = "sourcePositionKey";

    @Parameter(description = "Key to CameraCoordinate of the cog as calculated bei HillasParameters", defaultValue = "cog")
    public String cogKey = "cog";

    @Parameter(required = false, description = "Pixel set to cluster. If null, cluster all camera pixel; in that case decide which clusters should be kept via pixelSetKey", defaultValue = "null")
    public String areaKey = null;


    @Override
    public Data process(Data data) {
        int[] shower = Utils.getValidPixelSetAsIntArr(data, Constants.N_PIXELS, showerKey);
        int[] area = Utils.getValidPixelSetAsIntArr(data, Constants.N_PIXELS, areaKey);


        double[] arrivalTime = ((double[]) data.get(arrivaltimePosKey));
        double[] photoncharge = ((double[]) data.get(photonchargeKey));
        double[] morphology = ((double[]) data.get(morphologyKey));


        //source position not needed for example-xml, need to be calculated in "sourceParameter_mc.xml" for the feature "distanceSource"
        //same for COGxy, needed for distanceCog, but not in the example xml

        CameraCoordinate sourcePosition = (CameraCoordinate) data.get(sourcePositionKey);
        CameraCoordinate cog = (CameraCoordinate) data.get(cogKey);

        //get 'shower' as int array with pixel id's3System.out.println();


        int[] clusterID = new int[Constants.N_PIXELS];
        int[] showerClusterID = new int[Constants.N_PIXELS];

        int[] areaArray = new int[Constants.N_PIXELS];
        if (areaKey != null) {
            for (int i = 0; i < area.length; i++) {
                areaArray[area[i]] = 1;
            }
        } else {

            for (int i = 0; i < Constants.N_PIXELS; i++) {
                areaArray[i] = 1;
            }
        }

        for (int i = 0; i < Constants.N_PIXELS; i++) {
            clusterID[i] = 0;
            showerClusterID[i] = -2;
        }


        int startPath = NextStartPixel(clusterID, area);

        int cluster = 1;


        //FellWalker
        while (startPath != -1) {

            int highestNeighbourID;
            int currentPixel = startPath;
            boolean pathend = false;

            aktuellerPfad.add(startPath);

            while (!pathend) {
                //find neighbours and brightest neighbour
                CameraPixel[] allNeighbours = mapping.getNeighborsFromID(currentPixel);

                //find usable neighbours (pixel marked with clusterID = 0 after cleaning)
                ArrayList<CameraPixel> usableNeighbours = new ArrayList<>();

                for (CameraPixel n : allNeighbours) {
                    if (clusterID[n.id] != -2 && areaArray[n.id] == 1) {
                        usableNeighbours.add(n);
                    }
                }


                highestNeighbourID = findMaxChargeNeighbour(usableNeighbours, currentPixel, morphology);

                aktuellerPfad.add(highestNeighbourID);

                if (highestNeighbourID == currentPixel) {
//                    int brightestNeighbourIDLarge = findMaxChargeLargeNeighbour(currentPixel, photoncharge);
//
//                    if (brightestNeighbourIDLarge != currentPixel) {
//
//                      if (clusterID[brightestNeighbourIDLarge] != 0) {
//                          pathToExistingCluster(clusterID, aktuellerPfad, clusterID[brightestNeighbourIDLarge]);
//                          pathend = true;
//                        } else {
//                            currentPixel = brightestNeighbourIDLarge;
//                        }
//                    } else {
                    pathToNewCluster(clusterID, aktuellerPfad, cluster);
                    cluster++;
                    pathend = true;
                    //    }

                } else {
                    if (clusterID[highestNeighbourID] != 0) {

                        pathToExistingCluster(clusterID, aktuellerPfad, clusterID[highestNeighbourID]);
                        pathend = true;

                    } else {
                        currentPixel = highestNeighbourID;
                    }
                }
            }
            startPath = NextStartPixel(clusterID, area);
        }
        //end FellWalker

        /* create a FactCluster array which contains all cluster as objects. The array-index of a cluster is equal to the
         clusterID set in the fellwalker-algorithm. Keep in mind, that there is no clusterID 0, so the first cluster-object
         has to be treated separately!
          */


        FactCluster[] clusterSet = new FactCluster[cluster];
        for (int i = 0; i < cluster; i++) {
            clusterSet[i] = new FactCluster();
            clusterSet[i].setClusterID(i);
        }

        for (int i = 0; i < Constants.N_PIXELS; i++) {
            clusterSet[clusterID[i]].addContentPixel(i);
            clusterSet[clusterID[i]].addContentPixelPhotoncharge(photoncharge[i]);
            clusterSet[clusterID[i]].addContentPixelArrivaltime(arrivalTime[i]);
        }


        // fill another list with morphology values if morphology is not photoncharge
        if (morphologyKey != photonchargeKey) {
            for (int i = 0; i < Constants.N_PIXELS; i++) {
                clusterSet[clusterID[i]].addContentMorphology(morphology[i]);
            }
        }

        //add showerpixel/areapixel in a cluster to its cleaningPixelList:
        //if the whole camera pixels are clustered, (areaKey = null) use shower[] as 'cleaningPixel'.
        //If another pixel set (area) should be clustered, use area[] as 'cleaningPixel'. In this case all clustershould be kept, exept clusters with less than 'minShowerpixel' content.
        //Naming is quite confusing.
        if (areaKey == null) {
            for (int i = 0; i < shower.length; i++) {
                clusterSet[clusterID[shower[i]]].addCleaningPixel(shower[i]);
            }
        } else {
            for (int i = 0; i < area.length; i++) {
                clusterSet[clusterID[area[i]]].addCleaningPixel(area[i]);
            }
        }


        /* Here the parameter minShowerpixel is used. Label all clusters with true (containsShowerPixel) if they contain at least 'minShowerpixel' showerpixel
         * (pixel that survive the cleaning) and false if they contain less showerpixel. The clusters that are labeled 'true' get a new successive id.
         * All of these clusters are put to a new FactCluster array 'showerCluster'.
        */


        FactCluster[] showerCluster = removeCluster(clusterSet, minShowerpixel);


        int numCluster = showerCluster.length;
        //System.out.println(numCluster);


        /* build 1440-int array that contains the id's of the cluster that survive 'removeCluster' for every pixel.
         * (Just to have a quick look at it in the event viewer, not relevant for the algorithm itself or the resulting event parameters.)
        */
        for (FactCluster c : clusterSet) {
            if (c.getShowerLabel()) {
                for (int i : c.contentPixel) {
                    showerClusterID[i] = c.getClusterID();
                }
            }
        }


        // mark shower cog for the viewer
/*        for(FactCluster c : clusterSet){
            //System.out.println(c.cogId());
            if(c.getShowerLabel() == true) {
                showerClusterID[c.cogId()] = numCluster+1;
            }
        }

        markBoundaryPixel(clusterSet, showerClusterID);*/

        //build features (if there is any cluster left after removeCluster) and put them to data set
        if (numCluster != 0) {
            double ratio = boundContentRatio(showerCluster);
            double idealBoundDiff = idealBoundDiff(showerCluster);
            double boundAngleSum = boundAngleSum(showerCluster);
            double distanceCenterSum = distanceCenter(showerCluster);


            /*
            Source dependent parameter! Not needed for fellwalker_example. If this parameter shall be calculated,
            "sourceParameter_mc.xml" has to be included in the xml-file, cause source position has to be known
             */
            double distanceSource;
            if (sourcePosition == null) {
                distanceSource = Double.NaN;
            } else {
                distanceSource = distanceSource(showerCluster, sourcePosition);
            }
            /*
            cog must be calculated in HillasParameters, before distanceCog can be calculated
             */
            double distanceCog = distanceCog(showerCluster, cog.xMM, cog.yMM);


            int convexity = searchForCompactGroups(showerCluster, showerClusterID);


            findNeighbors(showerCluster, showerClusterID);

            double numNeighborCluster = neighborClusterMean(showerCluster);
            double chargeMaxClusterRatio;
            if (morphologyKey == photonchargeKey) {
                chargeMaxClusterRatio = getChargeMaxCluster(showerCluster);
            } else {
                chargeMaxClusterRatio = getMorphMaxCluster(showerCluster);
            }

            int numPixelMaxCluster = maxCluster(showerCluster).getNumPixel();

            int numClusterPixel = numClusterPixel(showerCluster);


            double stdNumpixel = stdNumPixel(showerCluster);


            data.put("boundRatio", ratio);
            data.put("idealBoundDiff", idealBoundDiff);
            data.put("boundAngle", boundAngleSum);
            data.put("distanceCenter", distanceCenterSum);

            data.put("distanceCog", distanceCog);
            data.put("distanceSource", distanceSource);

            data.put("neighborCluster", numNeighborCluster);
            data.put("chargeMax", chargeMaxClusterRatio);
            data.put("maxClusterNumPixel", numPixelMaxCluster);
            data.put("numClusterPixel", numClusterPixel);
            data.put("stdNumPixel", stdNumpixel);
            data.put("convexity", convexity);


        } else {
            data.put("boundRatio", null);
            data.put("idealBoundDiff", null);
            data.put("boundAngle", null);
            data.put("distanceCenter", null);
            data.put("distanceCog", null);
            data.put("distanceSource", null);
            data.put("neighborCluster", null);
            data.put("chargeMax", null);
            data.put("maxClusterNumPixel", null);
            data.put("numClusterPixel", null);
            data.put("stdNumPixel", null);
            data.put("convexity", null);


        }

        data.put("AllClusterID", clusterID);
        data.put(morphologyKey + "ClusterID", showerClusterID);
        data.put("clusterNoCleaning", cluster);
        data.put("numCluster", numCluster);


/*        PrintWriter writer = null;
        try {
            writer = new PrintWriter("/home/lena/Dokumente/Masterarbeit/Vortrag/DPG/protonShower7.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for(int i=0;i<Constants.N_PIXELS;i++){
            writer.println(i + "\t" + showerClusterID[i] + "\t" + photoncharge[i] + "\t" + cleaning[i]);
        }

        writer.close();


        System.out.println(data.get("EventNum"));*/


        return data;

    }

    //find next pixel without clusterID to start a new path, return ID
    public int NextStartPixel(int[] clusterID, int[] shower) {
        int next;
        int i = 0;
        while (clusterID[shower[i]] != 0) {
            i++;
            if (i == shower.length) {
                i = -1;
                break;
            }
        }
        if (i == -1) {
            return -1;
        } else {
            return shower[i];
        }
    }

    //find brightest neighbour, return the currentPixel if there is no brighter neighbour!!
    public int findMaxChargeNeighbour(ArrayList<CameraPixel> usableNeighbours, int currentPixel, double[] photoncharge) {

        double maxBrightness = photoncharge[currentPixel];
        int maxBrightnessID = currentPixel;

        for (CameraPixel n : usableNeighbours) {
            if (photoncharge[n.id] > maxBrightness) {
                maxBrightness = photoncharge[n.id];
                maxBrightnessID = n.id;
            }
        }
        return maxBrightnessID;
    }

    //find brightest neighbour in large neighbourhood, return the currentPixel if there is no brighter neighbour!!
    public int findMaxChargeLargeNeighbour(int currentPixel, double[] photoncharge) {
        CameraPixel[] largeNeighbours = mapping.getSecondOrderNeighboursFromID(currentPixel);

        double maxBrightness = photoncharge[currentPixel];
        int maxBrightnessID = currentPixel;

        for (CameraPixel n : largeNeighbours) {
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


    public FactCluster[] removeCluster(FactCluster clusterSet[], int minShowerpixel) {
        ArrayList<FactCluster> showerCluster = new ArrayList<>();
        int numShowerCluster = 0;
        int newID = 1;
        for (int c = 1; c < clusterSet.length; c++) {
            if (clusterSet[c].cleaningPixel.size() >= minShowerpixel) {
                clusterSet[c].setShowerLabel(true);
                clusterSet[c].setClusterID(newID);
                newID++;
                numShowerCluster++;
                showerCluster.add(clusterSet[c]);
            } else {
                clusterSet[c].setShowerLabel(false);
            }
        }

        FactCluster[] showerClusterArray = new FactCluster[numShowerCluster];
        for (int i = 0; i < numShowerCluster; i++) {
            showerClusterArray[i] = showerCluster.get(i);
        }
        return showerClusterArray;

    }

    // not needed for parameter calculation, just to have a quick look in the viewer
    public static void markBoundaryPixel(FactCluster[] clusterSet, int[] showerClusterID) {
        for (FactCluster c : clusterSet) {
            if (c.getShowerLabel()) {
                ArrayList<Integer> boundPixel = c.findBoundaryNaive();
                showerClusterID[boundPixel.get(0)] = -1;
                for (int i = 1; i < boundPixel.size(); i++) {

                    showerClusterID[boundPixel.get(i)] = 0;

                }
            }
        }
    }


    /* Calculates the ratio of number of bound pixel to number of content pixels and sums it up for all clusters.
     * So this feature is correlated to the number of clusters in the event ('NumCluster'). One could divide the returned
     * 'ratio' by 'showerCluster.length' to have a normed value.
     * Idea behind is to have a value for the shape of a cluster.
     */
    public static double boundContentRatio(FactCluster[] showerCluster) {
        double ratio = 0;

        for (FactCluster c : showerCluster) {
            ratio += (double) c.getBoundaryLength() / c.getNumPixel();
        }

        return ratio / showerCluster.length;
    }


    /*
     * Sums up the 'idealBoundDiff' of every cluster in the event. The 'ideal boundary' is the minimum number of boundary pixels,
     * which means the cluster would have the shape of a circle. 'idealBoundDiff' means therefore the difference between the real number
     * of boundary pixels and the ideal(minimal) number of boundary pixels a cluster with a certain number of pixel could have.
     * Keep in mind that this feature is again correlated to 'NumCluster', as long as the returned 'sum' is not divided by 'showerCluster.length'.
     */
    public static double idealBoundDiff(FactCluster[] showerCluster) {
        double sum = 0;
        for (FactCluster c : showerCluster) {
            sum += c.idealBoundDiff();
        }
        return sum / showerCluster.length;
    }

    /*
     * Sums up the 'boundAngleSum' for all clusters in the event. 'boundAngleSum' does something like count how often you
     * have to change the direction (on a hexagonal coordinate system) if you walk along the boundary of the cluster.
     * This is another feature that should describe the shape of a cluster. If a cluster has kind of a 'smooth' shape
     * (like circle or ellipse) the 'boundAngleSum' should be smaller than the value for a cluster with a irregular random
     * splashy shape...(Again: correlation to 'NumCluster' if not dividing by 'showerCluster.length'.)

     */
    public static double boundAngleSum(FactCluster[] showerCluster) {
        double sum = 0;
        for (FactCluster c : showerCluster) {
            sum += c.boundAngleSum();
        }
        return sum / showerCluster.length;
    }


    /*
     * Returns the mean over all distances from all cluster center of gravity to the camera center (the center position
     * in mm not the 'center pixel'). Gives an information about the geometrical distribution of the clusters in the camera image.
     */
    public static double distanceCenter(FactCluster[] showerCluster) {
        double sum = 0;
        for (FactCluster c : showerCluster) {
            sum += c.distanceCamCenter();
        }
        return sum / showerCluster.length;
    }

    public double distanceCog(FactCluster[] showerCluster, double cogX, double cogY) {
        double sum = 0;
        for (FactCluster c : showerCluster) {
            sum += c.distanceCog(cogX, cogY);
        }
        return sum / showerCluster.length;
    }

    public double distanceSource(FactCluster[] showerCluster, CameraCoordinate sourcePosition) {
        double sum = 0;
        for (FactCluster c : showerCluster) {
            sum += c.distanceSource(sourcePosition.xMM, sourcePosition.yMM);
        }
        return sum / showerCluster.length;
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
    public int searchForCompactGroups(FactCluster[] showerCluster, int[] showerClusterID) {
        //int[][] map = new int [showerCluster.length][showerCluster.length];
        //int [] viewer = showerClusterID.clone();
        int sumAirpixel = 0;
        for (int i = 0; i < showerCluster.length; i++) {
            for (int j = i + 1; j < showerCluster.length; j++) {
                int airPixel = countAirPixel(mapping.line(showerCluster[i].cogId(), showerCluster[j].cogId()), showerClusterID);
                showerCluster[i].addAirDistance(airPixel);
                showerCluster[j].addAirDistance(airPixel);
                if (airPixel == 0) {
                    showerCluster[i].addCompactCluster(showerCluster[j].getClusterID());
                    showerCluster[j].addCompactCluster(showerCluster[i].getClusterID());
                } else {
                    sumAirpixel += airPixel;
                    //System.out.println(sumAirpixel);
                }

            }
        }

        return sumAirpixel;
    }

    public int countAirPixel(ArrayList<Integer> gapPixel, int[] showerClusterID) {
        int countAirPixel = 0;
        for (int id : gapPixel) {
            if (showerClusterID[id] == -2) {
                countAirPixel++;
            }

        }
        return countAirPixel;
    }

    /*
     * Mean over the number of (naive) neighbors for all clusters in an event.
     */
    public double neighborClusterMean(FactCluster[] showerCluster) {
        double sum = 0;
        for (FactCluster c : showerCluster) {
            sum += c.getNumNeighbors();
        }
        return sum / showerCluster.length;
    }

    public void findNeighbors(FactCluster[] showerSet, int[] showerClusterID) {                  //------------------------------- Neighbors testen waere sinnvoll
        //int[] numNeighbors = new int [showerSet.length];
        for (FactCluster c : showerSet) {
            int clusterID = c.getClusterID();
            ArrayList<Integer> bound = c.findBoundaryNaive();
            for (int id : bound) {
                CameraPixel[] boundPixelNeighbors = mapping.getNeighborsFromID(id);
                for (CameraPixel p : boundPixelNeighbors) {
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


    public FactCluster maxCluster(FactCluster[] showerCluster) {
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


    public static double getMorphMaxCluster(FactCluster[] showerCluster) {
        if (showerCluster.length == 1) {
            return 1;
        } else {
            int maxClusterIndex = 0;
            int size = 0;
            double chargeSum = 0;
            int i = 0;
            for (FactCluster c : showerCluster) {
                chargeSum += c.getMorphSum();
                if (c.getNumPixel() > size) {
                    size = c.getNumPixel();
                    maxClusterIndex = i;
                }
                i++;
            }
            return showerCluster[maxClusterIndex].getMorphSum() / chargeSum;
        }

    }


    public static double getChargeMaxCluster(FactCluster[] showerCluster) {
        if (showerCluster.length == 1) {
            return 1;
        } else {
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

    int numClusterPixel(FactCluster[] showerCluster) {
        int sum = 0;
        for (FactCluster c : showerCluster) {
            sum += c.getNumPixel();
        }
        return sum;
    }


    //Possible, but not a good feature. Arrival times are therefore not use for feature creation so far.
    public double stdArrTime(FactCluster[] showerCluster, double[] arrivaltime) {
        double arrTimeMean = 0;
        double arrTimeStd = 0;
        for (FactCluster c : showerCluster) {
            int maxId = c.maxPhotonchargeId();
            arrTimeMean += arrivaltime[maxId] / showerCluster.length;
        }

        for (FactCluster c : showerCluster) {
            int maxId = c.maxPhotonchargeId();
            arrTimeStd += Math.pow((arrTimeMean - arrivaltime[maxId]), 2) / showerCluster.length;
        }

        return Math.sqrt(arrTimeStd);

    }

    // Standard deviation of the mean over the number of pixels in every cluster in the event.
    public static double stdNumPixel(FactCluster[] showerCluster) {
        int numCluster = showerCluster.length;
        double mean = 0;
        double std = 0;
        for (FactCluster c : showerCluster) {
            mean += c.getNumPixel() / numCluster;
        }

        for (FactCluster c : showerCluster) {
            std += Math.pow((mean - c.getNumPixel()), 2) / numCluster;
        }

        return Math.sqrt(std);
    }
}
