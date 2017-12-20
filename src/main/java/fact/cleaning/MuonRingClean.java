package fact.cleaning;

import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;

import java.util.ArrayList;
import java.util.Arrays;

public class MuonRingClean implements Processor {

    private String ringPixelKey;
    private String photonChargeKey;
    private String arrivalTimeKey;
    private String outputKey;
    private double photonChargeThreshold;
    private double timeThreshold;

    @Override
    public Data process(Data input) {

        final Logger log = LoggerFactory.getLogger(MuonRingClean.class);

        int[] ringPixel = (int[]) input.get(ringPixelKey);
        double[] photonCharge = (double[]) input.get(photonChargeKey);
        double[] arrivalTime = (double[]) input.get(arrivalTimeKey);

        ArrayList<Integer> cleanRingList = new ArrayList<Integer>();
        PixelSet cleanPixelSet = new PixelSet();

        for (int i = 0; i < ringPixel.length; i++) {
            if (photonCharge[ringPixel[i]] >= photonChargeThreshold) {
                cleanRingList.add(ringPixel[i]);
                cleanPixelSet.addById(ringPixel[i]);
            }
        }


        if (timeThreshold > 0 && cleanRingList.size() > 14) {
            double median;

            double[] arrivalTimeForMedian = new double[cleanRingList.size()];

            for (int i = 0; i < cleanRingList.size(); i++) {
                arrivalTimeForMedian[i] = arrivalTime[cleanRingList.get(i)];
            }

            Arrays.sort(arrivalTimeForMedian);
            int length = arrivalTimeForMedian.length;
            if (length % 2 == 1) {
                median = arrivalTimeForMedian[(length - 1) / 2];
            } else {
                median = 0.5 * (arrivalTimeForMedian[(length) / 2] + arrivalTimeForMedian[(length) / 2 - 1]);
            }

            ArrayList<Integer> timeCleanRingList = new ArrayList<Integer>();
            cleanPixelSet.clear();

            for (int i = 0; i < cleanRingList.size(); i++) {
                if (Math.abs(median - arrivalTime[cleanRingList.get(i)]) <= timeThreshold) {
                    timeCleanRingList.add(cleanRingList.get(i));
                    cleanPixelSet.addById(cleanRingList.get(i));
                }
            }

            int[] timeCleanRing = new int[timeCleanRingList.size()];
            for (int i = 0; i < timeCleanRingList.size(); i++) {
                timeCleanRing[i] = timeCleanRingList.get(i);
            }

            input.put(outputKey, timeCleanRing);
            input.put(outputKey + "_pixelset", cleanPixelSet);
            return input;
        }


        int[] cleanRing = new int[cleanRingList.size()];
        for (int i = 0; i < cleanRingList.size(); i++) {
            cleanRing[i] = cleanRingList.get(i);
        }


        input.put(outputKey, cleanRing);
        input.put(outputKey + "_pixelset", cleanPixelSet);


        return input;
    }

    // ********************************
    // Getters and Setters for the Keys
    // ********************************

    public String getRingPixelKey() {
        return ringPixelKey;
    }


    public void setRingPixelKey(String ringPixelKey) {
        this.ringPixelKey = ringPixelKey;
    }


    public String getPhotonChargeKey() {
        return photonChargeKey;
    }


    public void setPhotonChargeKey(String photonChargeKey) {
        this.photonChargeKey = photonChargeKey;
    }


    public String getArrivalTimeKey() {
        return arrivalTimeKey;
    }


    public void setArrivalTimeKey(String arrivalTimeKey) {
        this.arrivalTimeKey = arrivalTimeKey;
    }


    public String getOutputKey() {
        return outputKey;
    }


    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public double getPhotonChargeThreshold() {
        return photonChargeThreshold;
    }

    public void setPhotonChargeThreshold(double photonChargeThreshold) {
        this.photonChargeThreshold = photonChargeThreshold;
    }

    public double getTimeThreshold() {
        return timeThreshold;
    }

    public void setTimeThreshold(double timeThreshold) {
        this.timeThreshold = timeThreshold;
    }

}
