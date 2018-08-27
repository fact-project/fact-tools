package fact.features.evaluate;

import fact.Constants;
import fact.container.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by lena on 02.11.15.
 * <p>
 * Processor to evaluate a cleaning.
 * 1) Needs a PixelSet and the number of Cherenkov photons per pixel
 * 2) Returns a "performance matrix" containing 4 doubles for true positives, false negatives and so on. A pixel with more
 * than "mcPhotThreshold" mc-photons is classified as showerpixel. The default value is 3.
 * 3) Calculates and returns some parameters for evaluation like precision, recall, accuracy and true/false positive/negative rate
 */
public class CleaningPerformance implements Processor {

    @Parameter(required = true)
    public String pixelSetKey;

    @Parameter(required = false, description = "Minimal number of MC Cherenkov photons to classify a pixel as showerpixel", defaultValue = "3")
    public int mcPhotThreshold = 3;

    @Override
    public Data process(Data data) {


        float[] McPhotoncharge = (float[]) data.get("McCherPhotNumber");
        double[] performance;

        if (data.containsKey(pixelSetKey) && data.get(pixelSetKey) != null) {
            PixelSet pixelSet = (PixelSet) data.get(pixelSetKey);
            int[] shower = pixelSet.toIntArray();
            int numShowerpixel = shower.length;

            performance = getPerformanceMatrixShower(McPhotoncharge, shower, numShowerpixel);

        } else {
            performance = getPerformanceMatrixNoShower(McPhotoncharge);
            System.out.println(performance[0]);
        }

        double tp = performance[0]; //true positive
        double fn = performance[1]; //false negative
        double tn = performance[2]; //true negative
        double fp = performance[3]; //false positive

        double recall = tp / (tp + fn);                   //aka true positive rate or sensitivity
        double specificity = tn / (tn + fp);              //aka true negative rate

        double accuracy = (tp + tn) / (tp + tn + fp + fn);
        double fpr = fp / (fp + tn);                      // false positive rate
        double fnr = fn / (fn + tp);                      // false negative rate
        double precision;

        if (data.get(pixelSetKey) != null) {
            precision = tp / (tp + fp);                //aka positive predictive value
            //aka true positive rate or sensitivity
            // System.out.println(recall);

        } else {
            precision = 0;
            recall = 0;
        }


        data.put(pixelSetKey + "_confusionMatrix", performance);
        data.put(pixelSetKey + "_recall", recall);
        data.put(pixelSetKey + "_precision", precision);
        data.put(pixelSetKey + "_accuracy", accuracy);
        data.put(pixelSetKey + "_specificity", specificity);
        data.put(pixelSetKey + "_FPR", fpr);
        data.put(pixelSetKey + "_FNR", fnr);


        return data;
    }


    public double[] getPerformanceMatrixShower(float[] McPhotoncharge, int[] shower, int numShowerpixel) {
        double[] performance = new double[4];

        boolean[] cleaningPixel = new boolean[Constants.N_PIXELS];
        for (int i = 0; i < Constants.N_PIXELS; i++) {
            cleaningPixel[i] = false;
        }
        for (int i = 0; i < numShowerpixel; i++) {
            cleaningPixel[shower[i]] = true;
        }


        for (int i = 0; i < Constants.N_PIXELS; i++) {

            //true positive
            if (McPhotoncharge[i] > mcPhotThreshold && cleaningPixel[i] == true) {
                performance[0]++;
            }

            //false negative
            if (McPhotoncharge[i] > mcPhotThreshold && cleaningPixel[i] == false) {
                performance[1]++;
            }

            //true negative
            if (McPhotoncharge[i] < mcPhotThreshold && cleaningPixel[i] == false) {
                performance[2]++;
            }

            //false positive
            if (McPhotoncharge[i] < mcPhotThreshold && cleaningPixel[i] == true) {
                performance[3]++;
            }
        }
        return performance;
    }

    public double[] getPerformanceMatrixNoShower(float[] McPhotoncharge) {
        double[] performance = new double[4];

        //true positive
        performance[0] = 0;
        //false positive
        performance[3] = 0;

        for (int i = 0; i < Constants.N_PIXELS; i++) {
            //false negative
            if (McPhotoncharge[i] > mcPhotThreshold) {
                performance[1]++;
            }

            //true negative
            if (McPhotoncharge[i] < mcPhotThreshold) {
                performance[2]++;
            }
        }
        return performance;
    }
}
