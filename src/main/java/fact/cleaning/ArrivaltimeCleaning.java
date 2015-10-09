package fact.cleaning;

import fact.Utils;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Created by lena on 03.09.15.
 */
public class ArrivaltimeCleaning implements Processor {

    FactPixelMapping map = FactPixelMapping.getInstance();

    @Parameter(required = true)
    private String outputKeyXCoordinate = null;
    @Parameter(required = true)
    private String outputKeyYCoordinate = null;
    @Parameter(required = true)
    private String outputKeyArrtimePos = null;
    @Parameter(required = true)
    private String showerKey = null;

    @Override
    public Data process(Data data) {

        //1.: Transform all surviving pixel (after MotionCleaning) to ellipse coordinates
        //get COG in mm!!!!
        double cogX = (double) data.get("COGx");
        double cogY = (double) data.get("COGy");

        //Coordinates mm -> posX, posY
        cogX = map.getPixelBelowCoordinatesInMM(cogX, cogY).posX;
        cogY = map.getPixelBelowCoordinatesInMM(cogX, cogY).posY;

        double delta = (double) data.get("Delta");
        double[] arrivaltimePos = (double[]) data.get("ArrtimePos");
        int[] showerPixel = (int[]) data.get("shower");
        int[] cleaningIDM = (int[]) data.get("Cleaning");

        double[] trafoX = new double[showerPixel.length];
        double[] trafoY = new double[showerPixel.length];
        double[] arrivaltime =  new double[showerPixel.length];
        ArrayList<Integer> cleaningList = new ArrayList<>();
        int[] cleaningID = new int[1440];

        for(int i=0; i<1440; i++){
            cleaningID[i] = -2;
        }

        for(int i=0; i<showerPixel.length; i++){
            double posX = map.getPixelFromId(showerPixel[i]).posX;
            double posY = map.getPixelFromId(showerPixel[i]).posY;

            double[] trafoCoordinates = Utils.transformToEllipseCoordinates(posX, posY, cogX, cogY, delta);
            trafoX[i] = trafoCoordinates[0];
            trafoY[i] = trafoCoordinates[1];
            arrivaltime[i] = arrivaltimePos[showerPixel[i]];
        }

        //2.: Regression: transformed x-position / arrivaltime with all surviving pixel
        double intercept = linRegression(trafoX, arrivaltime)[0];
        double slope = linRegression(trafoX, arrivaltime)[1];
        double meanSquaredError = linRegression(trafoX, arrivaltime)[2];


        //3: Predict arrivaltime for each pixel. if deviation of real arrivaltime is smaller than threshold, pixel -> shower
        for(int i=0; i<1440; i++){
            double[] ellipseCoordinates = Utils.transformToEllipseCoordinates(map.getPixelFromId(i).posX, map.getPixelFromId(i).posY, cogX,cogY,delta);

            double prediction = slope * ellipseCoordinates[0] + intercept;
           /* System.out.println(Math.abs(prediction - arrivaltimePos[i]) + "     " + meanSquaredError);*/

            if(Math.abs(prediction - arrivaltimePos[i]) < 8 && Math.abs(ellipseCoordinates[1]) < 7){ /*<---------------------------------------hardcode threshold; deviation (prediction <-> real arrivaltime)*/
                cleaningList.add(i);
                cleaningID[i] = 0;            }
        }

        int[] shower = new int[cleaningList.size()];
        for(int i=0; i<cleaningList.size(); i++){
            shower[i] = cleaningList.get(i);
        }

        cleaningID = removeIsolatedPixel(cleaningID);
        int[] merge = mergeCleaning(cleaningIDM, cleaningID);


        data.put(outputKeyXCoordinate, trafoX);
        data.put(outputKeyYCoordinate, trafoY);
        data.put(outputKeyArrtimePos, arrivaltime);
        data.put("COGxPos", cogX);
        data.put("COGyPos", cogY);
        data.put(showerKey, shower);
        data.put("CleaningIDAT", cleaningID);
        data.put("CleaningMerge", merge);

        return data;
    }


    public void setoutputKeyXCoordinate(String XCoordinate) {
        this.outputKeyXCoordinate = XCoordinate;
    }
    public void setoutputKeyYCoordinate(String YCoordinate) {
        this.outputKeyYCoordinate = YCoordinate;
    }
    public void setoutputKeyArrtimePos(String ArrtimePos) {
        this.outputKeyArrtimePos = ArrtimePos;
    }
    public void setshowerKey(String shower) {
        this.showerKey = shower;
    }


    public double[] linRegression(double[] x, double[] arrTime){
        SimpleRegression regression = new SimpleRegression();
        double[] regParameter = new double[3];
        for(int i=0; i<x.length; i++) {
            regression.addData(x[i], arrTime[i]);
        }

        regParameter[0] = regression.getIntercept();
        regParameter[1] = regression.getSlope();
        regParameter[2] = regression.getInterceptStdErr();

        return regParameter;
    }

    public int[] removeIsolatedPixel(int[] cleaningID) {
        for (int i = 0; i < 1440; i++) {
            if (cleaningID[i] == 0) {
                int numberNeighbours = 0;
                FactCameraPixel[] neighbours = map.getNeighboursFromID(i);
                for (FactCameraPixel n:neighbours) {
                    if (cleaningID[n.id] == 0) {                             //while???
                        numberNeighbours++;
                    }
                }
                if (numberNeighbours == 0) {
                    cleaningID[i] = -2;
                }
            }
        }
        return cleaningID;
    }

    public int[] mergeCleaning(int[] cleaningIDM, int [] cleaningIDAT){
        int[] cleaningMerge = new int[1440];
        for(int i=0; i<1440; i++) {
            if (cleaningIDAT[i] == -2 && cleaningIDM[i] == -2) {
                cleaningMerge[i] = -2;
            } else if (cleaningIDAT[i] == 0 && cleaningIDM[i] == 0) {
                cleaningMerge[i] = 0;
            } else if (cleaningIDAT[i] == 0 && cleaningIDM[i] == -2){
                cleaningMerge[i] = -1;
            } else if(cleaningIDAT[i] == -2 && cleaningIDM[i] == 0){
                cleaningMerge[i] = 1;
            }
        }
        return cleaningMerge;
    }
}
