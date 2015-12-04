package fact.hexmap;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by lena on 12.11.15.
 */
public class FactCluster {

    FactPixelMapping mapping = FactPixelMapping.getInstance();

    private int clusterID;

    ArrayList<Integer> contentPixel = new ArrayList<>();
    ArrayList<Integer> cleaningPixel = new ArrayList<>();           //contains all pixel in the cluster which are already in the shower-array (after cleaning)
    ArrayList<Double> contentPixelPhotoncharge = new ArrayList<>();
    ArrayList<Double> contentPixelArrivaltime = new ArrayList<>();
    ArrayList<Integer> neighborClusterID = new ArrayList<>();
    ArrayList<Integer> airpixelNeighborCluster = new ArrayList<>();
    ArrayList<Integer> naiveNeighborClusterID = new ArrayList<>();

    private boolean containsShowerPixel;
    int numNeighbors;



    public void addContentPixel(int id){
        contentPixel.add(id);
    }

    public void addCleaningPixel(int id){
        cleaningPixel.add(id);
    }

    public void addContentPixelPhotoncharge(double photoncharge){
        contentPixelPhotoncharge.add(photoncharge);
    }

    public void addContentPixelArrivaltime(double arrtime){
        contentPixelArrivaltime.add(arrtime);
    }

    public double getPhotonchargeSum(){
        double sum = 0;
        for(double p : contentPixelPhotoncharge){
            sum = sum +p;
        }
        return sum;
    }

    public double getPhotonchargePerPixel(){
        double sum = 0;
        for(double p : contentPixelPhotoncharge){
            sum = sum +p;
        }
        return sum/contentPixelPhotoncharge.size();
    }





    public double maxPhotoncharge(){
        return Collections.max(contentPixelPhotoncharge);
    }

    public int maxPhotonchargeId(){
        return contentPixel.get(contentPixelPhotoncharge.indexOf(maxPhotoncharge()));
    }

    // calculate center of gravity for the cluster (weighted by photoncharge). If there is no pixel on the calculated position
    // or the calculated pixel is not part of the cluster, the 'brightest' pixel (max photoncharge) is returned instead
        public int cogId(){
            double cogX = 0;
            double cogY = 0;
            double size = 0;

            for (int i=0; i<contentPixel.size(); i++) {
                cogX += contentPixelPhotoncharge.get(i)
                        * mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM();
                cogY += contentPixelPhotoncharge.get(i)
                        * mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM();
                size += contentPixelPhotoncharge.get(i);

            }

            cogX /= size;
            cogY /= size;

            FactCameraPixel cog = mapping.getPixelBelowCoordinatesInMM(cogX,cogY);
            if(cog == null){
                return maxPhotonchargeId();
            }
            else {
                if (contentPixel.contains(cog.id)) {
                    return cog.id;
                } else {
                    return maxPhotonchargeId();
                }
            }
    }


    // a parameter to describe some kind of 'size'. This is the standard deviation of each pixel center from the cluster cog (centroid), where each pixel is weighted by its photoncharge or arrivaltime.
    // values for photoncharge must be positive. Therefore (arbitrary) 10 is added to every photoncharge. Values for arrivaltime should always be positive.

    public double stdPhotonchargeX(){
        double sumX2 = 0;    //d_i*x_i^2
        double sumX = 0;    //d_i*x_i
        double sumD = 0;    //d_i

        for(int i=0; i<getNumPixel();i++){
            sumX2 = sumX2 + (contentPixelPhotoncharge.get(i) + 10)*
                    mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM()*mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM();
            sumX = sumX + (contentPixelPhotoncharge.get(i) + 10)* mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM();
            sumD = sumD + (contentPixelPhotoncharge.get(i) + 10);
        }

        return Math.sqrt(sumX2 / sumD - Math.pow((sumX/ sumD),2));
    }

    public double stdPhotonchargeY(){
        double sumD = 0;    //d_i
        double sumY2 = 0;   //d_i*y_i^2
        double sumY = 0;    //d_i*y_i

        for(int i=0; i<getNumPixel();i++){
            sumD = sumD + (contentPixelPhotoncharge.get(i) + 10);
            sumY2 = sumY2 + (contentPixelPhotoncharge.get(i) + 10)* mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM()*mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM();
            sumY = sumY + (contentPixelPhotoncharge.get(i) + 10)* mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM();

        }

        return Math.sqrt(sumY2 / sumD - Math.pow((sumY/ sumD),2));
    }

    public double stdArrivaltimeX(){
        double sumX2 = 0;    //d_i*x_i^2
        double sumX = 0;    //d_i*x_i
        double sumD = 0;    //d_i

        for(int i=0; i<getNumPixel();i++){
            sumX2 = sumX2 + (contentPixelArrivaltime.get(i))*
                    mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM()*mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM();
            sumX = sumX + (contentPixelArrivaltime.get(i))* mapping.getPixelFromId(contentPixel.get(i)).getXPositionInMM();
            sumD = sumD + (contentPixelArrivaltime.get(i));
        }

        return Math.sqrt(sumX2 / sumD - Math.pow((sumX/ sumD),2));
    }

    public double stdArrivaltimeY(){
        double sumD = 0;    //d_i
        double sumY2 = 0;   //d_i*y_i^2
        double sumY = 0;    //d_i*y_i

        for(int i=0; i<getNumPixel();i++){
            sumD = sumD + (contentPixelArrivaltime.get(i));
            sumY2 = sumY2 + (contentPixelArrivaltime.get(i))* mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM()*mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM();
            sumY = sumY + (contentPixelArrivaltime.get(i))* mapping.getPixelFromId(contentPixel.get(i)).getYPositionInMM();

        }

        return Math.sqrt(sumY2 / sumD - Math.pow((sumY/ sumD),2));
    }


    //calculates the distance between the center of gravity (weighted by photoncharge) an the center of the camera              Testen!!!!!!!!!!!!!!!!
    public double distanceCamCenter(){
        int cog = cogId();
        double cogX = mapping.getPixelFromId(cog).getXPositionInMM();
        double cogY = mapping.getPixelFromId(cog).getYPositionInMM();

        return Math.sqrt(cogX*cogX + cogY*cogY);

    }



    private ArrayList<FactCameraPixel> findSortedBoundary() {


        int startId = findStartPixelBoundary();

        int countPixel = 0;
        int currentId = startId;
        boolean boundEnd = false;

        ArrayList<FactCameraPixel> boundNeighbors = new ArrayList<>();
        ArrayList<FactCameraPixel> sortedBound = new ArrayList<>();
        sortedBound.add(mapping.getPixelFromId(startId));

        while (!boundEnd) {
            FactCameraPixel [] neighbors = getNeighborsInClusterFromId(currentId);

            int i =0;
            for (FactCameraPixel p : neighbors) {
                // short version:  photonchargeNeighbors.add(contentPixelPhotoncharge.get(contentPixel.indexOf(p.id)));
                if (sortedBound.contains(p) || !isBoundPixel(p.id)) {
                    neighbors[i] = null;
                }
                i++;
            }

            for (int k=0; k<neighbors.length; k++) {
                if(neighbors[k] != null){
                    boundNeighbors.add(neighbors[k]);
                }
            }

            if(boundNeighbors.size() > 0) {

                // short version:  boundPixel.add(neighbors.get(photonchargeNeighbors.indexOf(Collections.min(photonchargeNeighbors))).id);

                if(boundNeighbors.size() == 1){
                    int minId = boundNeighbors.get(0).id;
                    sortedBound.add(boundNeighbors.get(0));
                    currentId = minId;
                }
                else {
                    int min = 100;
                    FactCameraPixel minP = boundNeighbors.get(0);
                    for(FactCameraPixel p:boundNeighbors){
                        int numClusterNeighbors = getNeighborsInClusterFromId(p.id).length;
                        if(numClusterNeighbors < min){
                            min = numClusterNeighbors;
                            minP = p;
                        }
                    }
                    int minId = minP.id;
                    sortedBound.add(minP);
                    currentId = minId;

                }
            }
            else { FactCameraPixel [] neighborsNew = getNeighborsInClusterFromId(currentId);

                if (neighborsNew.length == 1) {
                    sortedBound.add(mapping.getPixelFromId(currentId));
                    currentId = neighborsNew[0].id;
                } else if (neighborsNew.length == 2 && sortedBound.size() < findBoundaryNaive().size() ){
                    sortedBound.add(mapping.getPixelFromId(currentId));
                    if(sortedBound.get(sortedBound.size()-1).id == neighborsNew[0].id){
                        currentId = neighborsNew[0].id;
                    }
                    else {
                        currentId = neighborsNew[1].id;
                    }
                }
                else{
                    boundEnd = true;
                }
            }

            boundNeighbors.clear();

        }

        return sortedBound;
    }


    public ArrayList<Integer> findBoundaryNaive(){
        ArrayList<Integer> boundPixel = new ArrayList<>();
        for(int id : contentPixel){
            if( isBoundPixel(id)){
                boundPixel.add(id);
            }
        }

        return boundPixel;
    }

    private int findStartPixelBoundary(){
        boolean boundPixel = false;
        int i = 0;
        int id = -1;
        while (!boundPixel){
            boundPixel = isBoundPixel(contentPixel.get(i));
            id = contentPixel.get(i);
            i++;
        }

        return id;
    }

    private FactCameraPixel [] getNeighborsInClusterFromId(int id){
        FactCameraPixel[] allNeighbors = mapping.getNeighboursFromID(id);
        ArrayList<FactCameraPixel> neighborsInCluster = new ArrayList<>();
        for(FactCameraPixel p : allNeighbors){
            if(contentPixel.contains(p.id)){
                neighborsInCluster.add(p);
            }
        }

        FactCameraPixel [] neighborsArray = new FactCameraPixel[neighborsInCluster.size()];
        for(int i=0; i<neighborsInCluster.size(); i++){
            neighborsArray[i] = neighborsInCluster.get(i);
        }
        return neighborsArray;
    }

    private boolean isBoundPixel(int id){
        FactCameraPixel [] allCamNeighbors = mapping.getNeighboursFromID(id);
        FactCameraPixel [] clusterNeighbors = getNeighborsInClusterFromId(id);

        if(allCamNeighbors.length == 6 && clusterNeighbors.length == 6){
            return false;
        }
        else if(allCamNeighbors.length < 6 && clusterNeighbors.length == allCamNeighbors.length){
            return true;
        }
        else { //if(allCamNeighbors.length > clusterNeighbors.length){
            return true;
        }

    }

    public int idealBoundDiff(){
        return getBoundaryLength() - idealBound();
    }

    private int idealBound(){
        return (int) (2*Math.sqrt(3*getNumPixel()) - 3);
    }

 public int boundAngleSum(){
     ArrayList<FactCameraPixel> sortedBound = findSortedBoundary();
     int cuDir = calcDirection(cubeCoordinates(sortedBound.get(0).id), cubeCoordinates(sortedBound.get(1).id));
     int countChangeDir = 0;

     for(int i=1; i<sortedBound.size()-1; i++){
         int dir = calcDirection(cubeCoordinates(sortedBound.get(i).id), cubeCoordinates(sortedBound.get(i+1).id));
         if(cuDir != dir){
             countChangeDir++;
             cuDir = dir;
         }

     }

     int dir = calcDirection(cubeCoordinates(sortedBound.get(sortedBound.size()-1).id), cubeCoordinates(sortedBound.get(0).id));
     if(cuDir != dir){
         countChangeDir++;
     }

     return countChangeDir;

 }

    private int calcDirection(int[] pixel1, int[] pixel2){

        int diffX = pixel2[0] - pixel1[0];
        int diffY = pixel2[1] - pixel1[1];
        int diffZ = pixel2[2] - pixel1[2];

        if(diffY == 0){
            return 1;
        }
        else if(diffZ == 0){
            return 2;
        }
        else if(diffX == 0){
            return 3;
        }
        else {return 0;}
    }



    private int [] cubeCoordinates(int id){
        int [] cube = new int[3];
        int col = mapping.getPixelFromId(id).geometricX;
        int row = mapping.getPixelFromId(id).geometricY;

        int x = col;
        int z = row - (col - (col%2)) / 2;
        int y = -x -z;

        cube[0] = x;
        cube[1] = y;
        cube[2] = z;
        return cube;
    }

    public void addNeighborCluster(int id){
        neighborClusterID.add(id);
    }

    public int getNumNeighbors(){
        return naiveNeighborClusterID.size();
    }

    public void addNeighborDistance(int numAirPixel){
        airpixelNeighborCluster.add(numAirPixel);
    }

    public int getNumAirpixel(){
        int sum = 0;
        for(int i : airpixelNeighborCluster){
            sum += i;
        }
        return sum;
    }


    // get/set
    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }
    public int getClusterID(){
        return clusterID;
    }

    public int getNumPixel(){
        return contentPixel.size();
    }

    public void setShowerLabel(boolean containsShowerPixel){
        this.containsShowerPixel = containsShowerPixel;
    }

    public boolean getShowerLabel(){
        return containsShowerPixel;
    }

    public int getBoundaryLength(){
        return findBoundaryNaive().size();
    }


    public int getNumShowerpixel(){ return cleaningPixel.size();}


}
