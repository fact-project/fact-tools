package fact.datacorrection;

import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;

/**
 * Created by lena on 14.08.15.
 */
public class Threshold implements Processor {
    FactPixelMapping mapping = FactPixelMapping.getInstance();
    double[] brightness = new double[1440];
    int [] clusterID = new int[1440];



    @Override
    public Data process(Data data) {
        double[] data_array = (double[]) data.get("DataCalibrated");
        int roi = (Integer) data.get("NROI");
        int npix = (Integer) data.get("NPIX");
        int minClusterSize = 5;


        for(int i=0; i<1440;i++){
            clusterID[i] = 0;
        }


        int k = 0;
        for (FactCameraPixel p : mapping.pixelArray) {

            double[] brightnessSlices = p.getPixelData(data_array, roi);
            double b = 0;
            for (int i = 50; i < 120; i++) {
                b = b + brightnessSlices[i] / (70.0);
            }
            brightness[k] = b;
            k++;
        }

        removeUnderground(0.1);
        data.put("ClusterID", clusterID);
        //data.put("NumCluster", NumCluster);
        return data;
    }

    public void removeUnderground ( double threshold){
            //find max/min brightness
            double max = -10.0;
            double min = 100.0;
            int ID_max = -1;
            int ID_min = -1;

            for (int i = 0; i < 1440; i++) {
                if (brightness[i] > max) {
                    ID_max = i;
                    max = brightness[i];
                }
                if (brightness[i] < min) {
                    ID_min = i;
                    min = brightness[i];
                }
            }
            //define threshold value
            double abs = brightness[ID_max] - brightness[ID_min];
            double abs_threshold = brightness[ID_min] + threshold * abs;
            //System.out.println(brightness[ID_max] + "  " + brightness[ID_min] + "     " + abs_threshold);

            //set all pixel with brightness under threshold to -2
            for (int i = 0; i < 1440; i++) {
                if (brightness[i] < abs_threshold) {
                    clusterID[i] = -2;
                }
            }
        removeIsolatedPixel();

    }

    public void removeIsolatedPixel() {
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

    }
}
