package fact.hexmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by lena on 12.11.15.
 */
public class FactCluster {

    FactPixelMapping mapping = FactPixelMapping.getInstance();
    private int clusterID;

    ArrayList<Integer> contentPixel = new ArrayList<>();
    ArrayList<Integer> cleaningPixel = new ArrayList<>();               //contains all pixel in the cluster which are already in the shower-array (after cleaning)
    ArrayList<Integer> boundPixel = new ArrayList<>();
    ArrayList<Double> contentPixelPhotoncharge = new ArrayList<>();
    ArrayList<Double> contentPixelArrivaltime = new ArrayList<>();

/*    ArrayList<Double> boundPixelPhotoncharge = new ArrayList<>();
    ArrayList<Double> boundPixelArrivaltime = new ArrayList<>();*/


    private boolean containsShowerPixel;



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


    public void addBoundPixel(int id){
        boundPixel.add(id);
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


    //calculates the distance between the center of gravity (weighted by photoncharge) an the center of the camera              Testten!!!!!!!!!!!!!!!!
    public double distanceCamCenter(){
        int cog = cogId();
        double cogX = mapping.getPixelFromId(cog).getXPositionInMM();
        double cogY = mapping.getPixelFromId(cog).getYPositionInMM();

        return Math.sqrt(cogX*cogX + cogY*cogY);

    }

    public int[] boundaryIds(){
        findBoundary();
        int [] boundPixelArray = new int[boundPixel.size()];

        for(int i=0;i<boundPixel.size(); i++){
            boundPixelArray[i] = boundPixel.get(i);
        }

        return boundPixelArray;
    }

    private void findBoundary(){
        int startId = findStartPixelBoundary();
        if(isBoundPixel(startId) == false){

        }
        int currentId = startId;
        boolean boundEnd = false;

        while (boundEnd == false) {
            //case 1 (best case): minPhotonchargeId is a boundary-pixel
            ArrayList<FactCameraPixel> neighbors = getNeighborsInClusterFromId(currentId);

            ArrayList<Double> photonchargeNeighbors = new ArrayList<>();

            for (FactCameraPixel p : neighbors) {
                // short version:  photonchargeNeighbors.add(contentPixelPhotoncharge.get(contentPixel.indexOf(p.id)));
                if(boundPixel.contains(p.id) == false && isBoundPixel(p.id) == true) {
                    int indexOfId = contentPixel.indexOf(p.id);
                    double photonchargeNeighbor = contentPixelPhotoncharge.get(indexOfId);

                    photonchargeNeighbors.add(photonchargeNeighbor);
                }
            }

            if(photonchargeNeighbors.size() == 0){ boundEnd = true;}
            else{

            // short version:  boundPixel.add(neighbors.get(photonchargeNeighbors.indexOf(Collections.min(photonchargeNeighbors))).id);
            double min = Collections.min(photonchargeNeighbors);
            int indexMin = photonchargeNeighbors.indexOf(min);
            FactCameraPixel lowestNeighbor = neighbors.get(indexMin);
            int minId = lowestNeighbor.id;
                boundPixel.add(minId);
                currentId = minId;
                System.out.println(minId);
            }
           
        }
    }

    private int findStartPixelBoundary(){
        boolean boundPixel = false;
        int i = 0;
        int id = -1;
        while (boundPixel == false){
            boundPixel = isBoundPixel(contentPixel.get(i));
            id = contentPixel.get(i);
        }

        return id;
    }

    private ArrayList<FactCameraPixel> getNeighborsInClusterFromId(int id){
        FactCameraPixel[] allNeighbors = mapping.getNeighboursFromID(id);
        ArrayList<FactCameraPixel> neighborsInCluster = new ArrayList<>();
        for(FactCameraPixel p : allNeighbors){
            if(contentPixel.contains(p.id)){
                neighborsInCluster.add(p);
            }
        }
        return neighborsInCluster;
    }

    private boolean isBoundPixel(int id){
        FactCameraPixel [] allCamNeighbors = mapping.getNeighboursFromID(id);
        ArrayList<FactCameraPixel> clusterNeighbors = getNeighborsInClusterFromId(id);

        if(allCamNeighbors.length == 6 && clusterNeighbors.size() == 6){
            return false;
        }
        else if(allCamNeighbors.length < 6 && clusterNeighbors.size() == allCamNeighbors.length){
            return true;
        }
        else { //if(allCamNeighbors.length > clusterNeighbors.length){
            return true;
        }

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

    public int getNumShowerpixel(){ return cleaningPixel.size();}


}
