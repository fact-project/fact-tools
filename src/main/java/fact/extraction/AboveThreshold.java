package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Finds pixels above a certain threshold limit. If only a single slice of the
 * pixel is above the threshold, this pixel is marked.
 * Also the ratio of pixels above the threshold is calculated.
 */
public class AboveThreshold implements Processor {

    @Parameter(required = true, description = "A double array with length NROI times NPIX")
    public String dataKey = null;

    @Parameter(
            required = true,
            description = "A PixelSet with the pixels above the threshold. " +
                    "The fraction of pixels above the threshold is put into outputKey + PixelRatio" +
                    "The number of pixels above the threshold is put into outputKey + PixelCount" +
                    "The fraction of slices above the threshold is put into outputKey + SliceRatio" +
                    "The number of slices above the threshold is put into outputKey + SliceCount"
    )
    public String outputKey = null;

    @Parameter(required = true, description = "Threshold value to be exceeded"    )
    public double threshold = 0;

    @Override
    public Data process(Data item) {

        final int roi = (Integer) item.get("NROI");
        final double[] timeSeries = Utils.toDoubleArray(item.get(dataKey));
        final short thresholdShort = (short) threshold;

        int numSlicesAboveThreshold = 0;
        PixelSet pixelsAboveThreshold = new PixelSet();
        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            for (int slice = 0; slice < roi; slice++) {
                final int pos = pix * roi + slice;
                if (timeSeries[pos] > thresholdShort) {
                    pixelsAboveThreshold.addById(pix);
                    numSlicesAboveThreshold += 1;
                }
            }
        }

        final double ratioOfPixels = (double) pixelsAboveThreshold.set.size() / (double) Constants.N_PIXELS;

        item.put(outputKey, pixelsAboveThreshold);
        item.put(outputKey + "PixelRatio", ratioOfPixels);
        item.put(outputKey + "SliceRatio", numSlicesAboveThreshold / ((double) roi * Constants.N_PIXELS));
        item.put(outputKey + "PixelCount", pixelsAboveThreshold.set.size());
        item.put(outputKey + "SliceCount", numSlicesAboveThreshold);

        return item;
    }
}
