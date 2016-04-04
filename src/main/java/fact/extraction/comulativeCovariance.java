package fact.extraction;

import fact.Utils;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.util.Arrays;

/**
 * Caclutlate the comulative covariance of neighboring pixels to determine similarities
 *
 * Created by jebuss on 04.04.16.
 */
public class comulativeCovariance implements StatefulProcessor {
    @Parameter(required = true)
    private String key = null;

    @Parameter(required = true)
    private String amplitudePositionsKey = null;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "50")
    private int skipFirst = 35;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "50")
    private int skipLast = 100;

    @Parameter(required = false)
    private String outputKey = "DataScaled";


    private int npix = 1440;

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        Utils.mapContainsKeys(input, key, amplitudePositionsKey);
        double[] data = (double[]) input.get(key);
        int[] amplitudePositions = (int[]) input.get(amplitudePositionsKey);

        int roi = data.length / npix;
        double[] scaledData = data.clone();

        for (int pix = 0; pix < npix; pix++) {
            double maxAmpl = amplitudePositions[pix];

            for (int slice = 0; slice < roi; slice++) {
                scaledData[pix*roi+slice] /= maxAmpl;
            }


//            double[] pixData = Arrays.copyOfRange(data,pix*roi + skipFirst, (pix+1)*roi - 1 - skipLast);


        }
        input.put(outputKey, scaledData);

        return input;
    }

    @Override
    public void init(ProcessContext context) throws Exception {

    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setAmplitudePositionsKey(String amplitudePositionsKey) {
        this.amplitudePositionsKey = amplitudePositionsKey;
    }

    public void setSkipFirst(int skipFirst) {
        this.skipFirst = skipFirst;
    }

    public void setSkipLast(int skipLast) {
        this.skipLast = skipLast;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
