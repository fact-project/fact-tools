package fact.cleaning;

import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;

/**
 * Created by lena on 10.09.15.
 */
public class CleaningLerner implements Processor {
    FactPixelMapping mapping = FactPixelMapping.getInstance();
    int roi = 300;

    @Override
    public Data process(Data data) {
        double[] data_array = (double[]) data.get("DataCalibrated");
        //int[] acDifferences = ((int[]) data.get("AcDiff")).clone();
        double[] arrivalTime = ((double[]) data.get("ArrtimePos")).clone();
        double[] photoncharge = (double[]) data.get("photoncharge");
        //double[] clusterBrightnessSum = ((double[]) data.get("ClusterBrightnessSum")).clone();

        double[] devArrivaltime = new double[1440];
        double[] stdPixel = new double[1440];
        double[] posNeg = new double[1440];
        double[] diff_50_70 = new double[1440];
        double[] fivePoints = new double[1440];
        double[] posX = new double[1440];
        double[] posY = new double[1440];

        //deviation arrivaltime
        int coutBrightPixel = 0;
        double meanArrtime = 0;
        for(int i=0; i<1440; i++){
            if(photoncharge[i] > 5) {  //<---------------------------- hardcode, parameter to skip useless/small events?????
                meanArrtime = meanArrtime + arrivalTime[i];
                coutBrightPixel++;
            }
        }

        meanArrtime = meanArrtime/coutBrightPixel;
        //System.out.println(meanArrtime);

        for(int i=0; i<1440; i++){
            devArrivaltime[i] = Math.abs(meanArrtime - arrivalTime[i]);
        }

        //mean over all slices
        for(FactCameraPixel p : mapping.pixelArray) {
            double[] pixelData = p.getPixelData(data_array, roi);
            double mean = 0;
            for(int i=0; i<roi; i++){
                mean = mean + pixelData[i]/300.0;
            }

            double std = 0;
            for(int i=0; i<roi; i++){
                std= std + Math.pow((pixelData[i] - mean),2)/300.0;
            }
            stdPixel[p.id] = Math.sqrt(std);

        }


        for(FactCameraPixel p : mapping.pixelArray) {
            posX[p.id] = mapping.getPixelFromId(p.id).posX;
            posY[p.id] = mapping.getPixelFromId(p.id).posY;
            double[] pixelData = p.getPixelData(data_array, roi);
            double pos = 0;
            double neg = 0;

            for (int i=0; i<250; i++) {
                if (pixelData[i] > 0) {
                    pos = pos + pixelData[i];
                } else {
                    neg = neg + pixelData[i];
                }
            }

            posNeg[p.id] = pos+neg;

            diff_50_70[p.id] = pixelData[70] - pixelData[50];
            fivePoints[p.id] = (pixelData[60] - pixelData[57]) + (pixelData[65] - pixelData[60]) + (pixelData[83] - pixelData[65]) + (pixelData[112] - pixelData[83]);
        }

        data.put("DevArrivaltime",devArrivaltime);
        data.put("StdPixel", stdPixel);
        data.put("PosNeg", posNeg);
        data.put("Diff_5070", diff_50_70);
        data.put("posX", posX);
        data.put("posY", posY);
        data.put("5Points", fivePoints);

        return data;
    }
}
