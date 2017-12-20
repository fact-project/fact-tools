package fact.features.watershed;

import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by lena on 12.11.15.
 */
public class FactCluster {

    FactPixelMapping mapping = FactPixelMapping.getInstance();

    private int clusterID;

    public ArrayList<Integer> contentPixel = new ArrayList<>();
    private ArrayList<Double> contentPixelPhotoncharge = new ArrayList<>();
    private ArrayList<Double> contentPixelArrivaltime = new ArrayList<>();
    private ArrayList<Double> contentPixelMorphology = new ArrayList<>(); //list all content values if morphology is not photoncharge
    public ArrayList<Integer> cleaningPixel = new ArrayList<>();           //contains all pixel in the cluster which are already in the shower-array (after cleaning)
    ArrayList<Integer> compactClusterID = new ArrayList<>();        //contains the ids from all clusters in the event which belongs to the same compact group of clusters, means that there are no air pixel on the line between this cluster an the clusters in this list
    ArrayList<Integer> airpixelCluster = new ArrayList<>();         //contains number of air pixel on the line from this cluster to every other cluster. If clusters are direct or indirect neighbors, the number of air pixels is 0. List should have (number of clusters - 1) entries.
    public ArrayList<Integer> naiveNeighborClusterID = new ArrayList<>();

    private boolean containsShowerPixel;
    public int numNeighbors;


    public void addContentPixel(int id) {
        contentPixel.add(id);
    }

    protected void addContentMorphology(double morph) {
        contentPixelMorphology.add(morph);
    }

    public void addCleaningPixel(int id) {
        cleaningPixel.add(id);
    }

    public void addContentPixelPhotoncharge(double photoncharge) {
        contentPixelPhotoncharge.add(photoncharge);
    }

    public void addContentPixelArrivaltime(double arrtime) {
        contentPixelArrivaltime.add(arrtime);
    }

    public double getPhotonchargeSum() {
        double sum = 0;
        for (double p : contentPixelPhotoncharge) {
            sum = sum + p;
        }
        return sum;
    }

    public double getMorphSum() {
        double sum = 0;
        for (double p : contentPixelMorphology) {
            sum = sum + p;
        }
        return sum;
    }


    //not used at the moment, could maybe be used as a kind of weight later... Useful for cleaning??
    public double getPhotonchargePerPixel() {
        double sum = 0;
        for (double p : contentPixelPhotoncharge) {
            sum = sum + p;
        }
        return sum / contentPixelPhotoncharge.size();
    }

    public double maxPhotoncharge() {
        return Collections.max(contentPixelPhotoncharge);
    }

    public int maxPhotonchargeId() {
        return contentPixel.get(contentPixelPhotoncharge.indexOf(maxPhotoncharge()));
    }

    // calculate center of gravity for the cluster (weighted by photon charge). If there is no pixel on the calculated position
    // or the calculated pixel is not part of the cluster, the 'brightest' pixel (max photon charge) is returned instead
    public int cogId() {
        double cogX = 0;
        double cogY = 0;
        double size = 0;

        for (int i = 0; i < contentPixel.size(); i++) {
            cogX += contentPixelPhotoncharge.get(i)
                    * mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM();
            cogY += contentPixelPhotoncharge.get(i)
                    * mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM();
            size += contentPixelPhotoncharge.get(i);

        }

        cogX /= size;
        cogY /= size;

        CameraPixel cog = mapping.getPixelBelowCoordinatesInMM(cogX, cogY);
        if (cog == null) {
            return maxPhotonchargeId();
        } else {
            if (contentPixel.contains(cog.id)) {
                return cog.id;
            } else {
                return maxPhotonchargeId();
            }
        }
    }


    /* Std: a parameter to describe some kind of 'size'. This is the standard deviation of each pixel center from the cluster cog (centroid), where each pixel is weighted by its photon charge or arrival time.
    * Values for photon charge must be positive. Therefore (arbitrary) 10 is added to every photon charge. Values for arrival time should always be positive.
    * Not used and evaluated at the moment. Maybe for further ideas...(CLEANING????) But maybe it's quatsch.
    */

    public double stdPhotonchargeX() {
        double sumX2 = 0;    //d_i*x_i^2
        double sumX = 0;    //d_i*x_i
        double sumD = 0;    //d_i

        for (int i = 0; i < getNumPixel(); i++) {
            sumX2 = sumX2 + (contentPixelPhotoncharge.get(i) + 10) *
                    mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM() * mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM();
            sumX = sumX + (contentPixelPhotoncharge.get(i) + 10) * mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM();
            sumD = sumD + (contentPixelPhotoncharge.get(i) + 10);
        }

        return Math.sqrt(sumX2 / sumD - Math.pow((sumX / sumD), 2));
    }

    public double stdPhotonchargeY() {
        double sumD = 0;    //d_i
        double sumY2 = 0;   //d_i*y_i^2
        double sumY = 0;    //d_i*y_i

        for (int i = 0; i < getNumPixel(); i++) {
            sumD = sumD + (contentPixelPhotoncharge.get(i) + 10);
            sumY2 = sumY2 + (contentPixelPhotoncharge.get(i) + 10) * mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM() * mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM();
            sumY = sumY + (contentPixelPhotoncharge.get(i) + 10) * mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM();

        }

        return Math.sqrt(sumY2 / sumD - Math.pow((sumY / sumD), 2));
    }

    public double stdArrivaltimeX() {
        double sumX2 = 0;    //d_i*x_i^2
        double sumX = 0;    //d_i*x_i
        double sumD = 0;    //d_i

        for (int i = 0; i < getNumPixel(); i++) {
            sumX2 = sumX2 + (contentPixelArrivaltime.get(i)) *
                    mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM() * mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM();
            sumX = sumX + (contentPixelArrivaltime.get(i)) * mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM();
            sumD = sumD + (contentPixelArrivaltime.get(i));
        }

        return Math.sqrt(sumX2 / sumD - Math.pow((sumX / sumD), 2));
    }

    public double stdArrivaltimeY() {
        double sumD = 0;    //d_i
        double sumY2 = 0;   //d_i*y_i^2
        double sumY = 0;    //d_i*y_i

        for (int i = 0; i < getNumPixel(); i++) {
            sumD = sumD + (contentPixelArrivaltime.get(i));
            sumY2 = sumY2 + (contentPixelArrivaltime.get(i)) * mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM() * mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM();
            sumY = sumY + (contentPixelArrivaltime.get(i)) * mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM();

        }

        return Math.sqrt(sumY2 / sumD - Math.pow((sumY / sumD), 2));
    }

    public double meanArrivaltime() {
        double mean = 0;
        for (double at : contentPixelArrivaltime) {
            mean = mean + at / contentPixelArrivaltime.size();
        }

        return mean;
    }

    public double stdArrivaltime() {
        double mean = meanArrivaltime();
        double std = 0;

        for (double at : contentPixelArrivaltime) {
            std = std + Math.pow((mean - at), 2) / contentPixelArrivaltime.size();
        }

        return Math.sqrt(std);
    }


    //calculates the distance between the center of gravity (weighted by photon charge) an the center of the camera
    public double distanceCamCenter() {
        int cog = cogId();
        double cogX = mapping.getPixelFromId(cog).getXPositionInMM();
        double cogY = mapping.getPixelFromId(cog).getYPositionInMM();

        return Math.sqrt(cogX * cogX + cogY * cogY);

    }

    public double distanceCog(double showerCogX, double showerCogY) {
        int cog = cogId();
        double clusterCogX = mapping.getPixelFromId(cog).getXPositionInMM();
        double clusterCogY = mapping.getPixelFromId(cog).getYPositionInMM();

        double absX = Math.abs(clusterCogX - showerCogX);
        double absY = Math.abs(clusterCogY - showerCogY);

        return Math.sqrt(absX * absX + absY * absY);

    }

    public double distanceSource(double sourceX, double sourceY) {
        int cog = cogId();
        double clusterCogX = mapping.getPixelFromId(cog).getXPositionInMM();
        double clusterCogY = mapping.getPixelFromId(cog).getYPositionInMM();

        double absX = Math.abs(clusterCogX - sourceX);
        double absY = Math.abs(clusterCogY - sourceY);

        return Math.sqrt(absX * absX + absY * absY);

    }


    /*
     * This nice little method is used for the calculation of 'boundAngleSum'. It returns a list containing all boundary
     * as they appear if one would walk over the boundary. So the pixel list can be seen as path around the cluster. This
     * is needed to calculate the change of direction from step to step (pixel to pixel).
     */
    private ArrayList<CameraPixel> findSortedBoundary() {


        int startId = findStartPixelBoundary();

        int currentId = startId;
        boolean boundEnd = false;

        ArrayList<CameraPixel> boundNeighbors = new ArrayList<>();
        ArrayList<CameraPixel> sortedBound = new ArrayList<>();
        sortedBound.add(mapping.getPixelFromId(startId));

        while (!boundEnd) {
            CameraPixel[] neighbors = getNeighborsInClusterFromId(currentId);

            int i = 0;
            for (CameraPixel p : neighbors) {
                // short version:  photonchargeNeighbors.add(contentPixelPhotoncharge.get(contentPixel.indexOf(p.id)));
                if (sortedBound.contains(p) || !isBoundPixel(p.id)) {
                    neighbors[i] = null;
                }
                i++;
            }

            for (int k = 0; k < neighbors.length; k++) {
                if (neighbors[k] != null) {
                    boundNeighbors.add(neighbors[k]);
                }
            }

            if (boundNeighbors.size() > 0) {

                // short version:  boundPixel.add(neighbors.get(photonchargeNeighbors.indexOf(Collections.min(photonchargeNeighbors))).id);

                if (boundNeighbors.size() == 1) {
                    int minId = boundNeighbors.get(0).id;
                    sortedBound.add(boundNeighbors.get(0));
                    currentId = minId;
                } else {
                    int min = 100;
                    CameraPixel minP = boundNeighbors.get(0);
                    for (CameraPixel p : boundNeighbors) {
                        int numClusterNeighbors = getNeighborsInClusterFromId(p.id).length;
                        if (numClusterNeighbors < min) {
                            min = numClusterNeighbors;
                            minP = p;
                        }
                    }
                    int minId = minP.id;
                    sortedBound.add(minP);
                    currentId = minId;

                }
            } else {
                CameraPixel[] neighborsNew = getNeighborsInClusterFromId(currentId);

                if (neighborsNew.length == 1) {
                    sortedBound.add(mapping.getPixelFromId(currentId));
                    currentId = neighborsNew[0].id;
                } else if (neighborsNew.length == 2 && sortedBound.size() < findBoundaryNaive().size()) {
                    sortedBound.add(mapping.getPixelFromId(currentId));
                    if (sortedBound.get(sortedBound.size() - 1).id == neighborsNew[0].id) {
                        currentId = neighborsNew[0].id;
                    } else {
                        currentId = neighborsNew[1].id;
                    }
                } else {
                    boundEnd = true;
                }
            }

            boundNeighbors.clear();

        }

        return sortedBound;
    }


    public ArrayList<Integer> findBoundaryNaive() {
        ArrayList<Integer> boundPixel = new ArrayList<>();
        for (int id : contentPixel) {
            if (isBoundPixel(id)) {
                boundPixel.add(id);
            }
        }

        return boundPixel;
    }

    private int findStartPixelBoundary() {
        boolean boundPixel = false;
        int i = 0;
        int id = -1;
        while (!boundPixel) {
            boundPixel = isBoundPixel(contentPixel.get(i));
            id = contentPixel.get(i);
            i++;
        }

        return id;
    }

    //Gets a pixel id and returns an array of all pixels that are neighbors of this pixel AND belong to the same cluster.
    private CameraPixel[] getNeighborsInClusterFromId(int id) {
        CameraPixel[] allNeighbors = mapping.getNeighborsFromID(id);
        ArrayList<CameraPixel> neighborsInCluster = new ArrayList<>();
        for (CameraPixel p : allNeighbors) {
            if (contentPixel.contains(p.id)) {
                neighborsInCluster.add(p);
            }
        }

        CameraPixel[] neighborsArray = new CameraPixel[neighborsInCluster.size()];
        for (int i = 0; i < neighborsInCluster.size(); i++) {
            neighborsArray[i] = neighborsInCluster.get(i);
        }
        return neighborsArray;
    }

    //Gets a pixel id, returns true if this pixel is a boundary pixel of the cluster ans false if it's not.
    private boolean isBoundPixel(int id) {
        CameraPixel[] allCamNeighbors = mapping.getNeighborsFromID(id);
        CameraPixel[] clusterNeighbors = getNeighborsInClusterFromId(id);

        if (allCamNeighbors.length == 6 && clusterNeighbors.length == 6) {
            return false;
        } else if (allCamNeighbors.length < 6 && clusterNeighbors.length == allCamNeighbors.length) {
            return true;
        } else {
            return true;
        }

    }

    /* Returns (parameter) idealBoundDiff. This is the difference between the real number of boundary pixels and the minimal number
     * of boundary pixels this cluster could have.
     */
    public int idealBoundDiff() {
        return getBoundaryLength() - idealBound();
    }

    /*
     * Calculation of the minimal (ideal) number of boundary pixels this cluster could have. The ideal shape is of hexagon
     * (if you link the cog's of the boundary pixel).
     * Since there are just some discrete numbers of pixels that form a full hexagon, the ideal boundary also has to be
     * calculated for all numbers between this 'full hexagon numbers'.
     * The equation is only an approximation, but for the range of numbers in this case (up to 1440 pixels) it fits quite well.
     */
    private int idealBound() {
        if (getNumPixel() < 7) {
            return getNumPixel();
        } else {
            return (int) (2 * Math.sqrt(3 * getNumPixel()) - 3);
        }
    }

    /*
     * Returns a parameter for the cluster which should quantify the shape of the cluster. Imagine the boundary of the cluster
     * as path in a hexagonal cube coordinate system with three axis. To walk from one pixel to another you have six opportunities,
     * two on every hexagonal axis, makes three 'directions'. The first step defines the first direction (if you walk along the
     * x-, y- or z-axis). In the next step you could walk along the same axis in the same direction or change axis/direction.
     * This algorithm counts how often the direction is changed along the walk over the boundary. It doesn't matter which
     * direction is taken in the next step it just matters if is the same as in the step before or not.
     * Idea behind: If the cluster has a smooth round shape the boundAngleSum should be quite small. In case of zigzag it increases.
     */
    public int boundAngleSum() {
        ArrayList<CameraPixel> sortedBound = findSortedBoundary();
        if (sortedBound.size() > 1) {
            int cuDir = calcDirection(mapping.getCubeCoordinatesFromId(sortedBound.get(0).id), mapping.getCubeCoordinatesFromId(sortedBound.get(1).id));
            int countChangeDir = 0;

            for (int i = 1; i < sortedBound.size() - 1; i++) {
                int dir = calcDirection(mapping.getCubeCoordinatesFromId(sortedBound.get(i).id), mapping.getCubeCoordinatesFromId(sortedBound.get(i + 1).id));
                if (cuDir != dir) {
                    countChangeDir++;
                    cuDir = dir;
                }

            }

            int dir = calcDirection(mapping.getCubeCoordinatesFromId(sortedBound.get(sortedBound.size() - 1).id), mapping.getCubeCoordinatesFromId(sortedBound.get(0).id));
            if (cuDir != dir) {
                countChangeDir++;
            }

            return countChangeDir;
        } else {
            return 0;
        }

    }

    private int calcDirection(int[] pixel1, int[] pixel2) {

        int diffX = pixel2[0] - pixel1[0];
        int diffY = pixel2[1] - pixel1[1];
        int diffZ = pixel2[2] - pixel1[2];

        if (diffY == 0) {
            return 1;
        } else if (diffZ == 0) {
            return 2;
        } else if (diffX == 0) {
            return 3;
        } else {
            return 0;
        }
    }


    // some sets, gets and adds

    public void addCompactCluster(int id) {
        compactClusterID.add(id);
    }

    public int getNumNeighbors() {
        return naiveNeighborClusterID.size();
    }

    public int getCompactClusters() {
        return compactClusterID.size();
    }

    public void addAirDistance(int numAirPixel) {
        airpixelCluster.add(numAirPixel);
    }

    public int getNumAirpixel() {
        int sum = 0;
        for (int i : airpixelCluster) {
            sum += i;
        }
        return sum;
    }


    // get/set
    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }

    public int getClusterID() {
        return clusterID;
    }

    public int getNumPixel() {
        return contentPixel.size();
    }

    public void setShowerLabel(boolean containsShowerPixel) {
        this.containsShowerPixel = containsShowerPixel;
    }

    public boolean getShowerLabel() {
        return containsShowerPixel;
    }

    public int getBoundaryLength() {
        return findBoundaryNaive().size();
    }


    public int getNumShowerpixel() {
        return cleaningPixel.size();
    }


}
