package fact.extraction;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Finds pixels above a certain threshold limit. If only a single slice of the 
 * pixel is above the threshold, this pixel is marked.
 * Also the ratio of pixels above the threshold is calculated.
 */
public class AboveThreshold implements Processor {
    @Parameter(
        required = true, 
        description = "A double array with length NROI times NPIX"
    )
    private String dataKey = null;

    @Parameter(
        required = true,
        description = "A list of the pixels above the threshold. "+
                      "Also the fraction of pixels above the threshold" 
    )
    private String outputKey = null;

    @Parameter(
        required = true,
        description = "Threshold value to be exceeded"
    )
    protected short threshold = 1800;

    @Override
    public Data process(Data input) {

        final int npix = (Integer) input.get("NPIX");
        final int roi = (Integer) input.get("NROI");
        final short[] timeSerieses = (short[]) input.get(dataKey);

        ArrayList<Integer> pixelsAboveThresholdList = new ArrayList<>();
        for (int pix=0; pix<npix; pix++) {
            for(int slice=0; slice<roi; slice++) {
                final int pos = pix*roi + slice;
                if(timeSerieses[pos] > threshold) {
                    pixelsAboveThresholdList.add(pix);
                    break;
                }
            }
        }

        int[] pixelsAboveThreshold = Utils.arrayListToInt(pixelsAboveThresholdList);
        final double ratioOfPixels = (double)pixelsAboveThreshold.length/(double)npix;

        input.put(outputKey, pixelsAboveThreshold);
        input.put(outputKey+"Ratio", ratioOfPixels);
        input.put(outputKey+"Count", pixelsAboveThreshold.length);
        return input;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setThreshold(short threshold) {
        this.threshold = threshold;
    }
}