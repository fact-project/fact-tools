package fact.cleaning;

import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;
import java.util.Arrays;

public class MuonRingClean implements Processor {

    @Parameter(required = true)
    public String ringPixelKey;

    @Parameter(required = true)
    public String photonChargeKey;

    @Parameter(required = true)
    public String arrivalTimeKey;

    @Parameter(required = true)
    public String outputKey;

    @Parameter(required = true)
    public double photonChargeThreshold;

    @Parameter(required = true)
    public double timeThreshold;

    @Override
    public Data process(Data item) {
        int[] ringPixel = (int[]) item.get(ringPixelKey);
        double[] photonCharge = (double[]) item.get(photonChargeKey);
        double[] arrivalTime = (double[]) item.get(arrivalTimeKey);

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

            item.put(outputKey, timeCleanRing);
            item.put(outputKey + "_pixelset", cleanPixelSet);
            return item;
        }


        int[] cleanRing = new int[cleanRingList.size()];
        for (int i = 0; i < cleanRingList.size(); i++) {
            cleanRing[i] = cleanRingList.get(i);
        }


        item.put(outputKey, cleanRing);
        item.put(outputKey + "_pixelset", cleanPixelSet);


        return item;
    }
}
