package fact.datacorrection;

import fact.Constants;
import fact.Utils;
import fact.container.SpikeInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Tries to remove the artefact called 'Spike' from the data for different spike lengths. We know that there exist
 * spikes with length 4.
 *
 * @author Fabian Temme
 */
public class RemoveSpikes implements Processor {
    static Logger log = LoggerFactory.getLogger(RemoveSpikes.class);

    @Parameter(required = true)
    public String dataKey = null;

    @Parameter(required = true)
    public String startCellKey = null;

    @Parameter(required = true)
    public String outputKey = null;

    @Parameter(required = true)
    public double spikeLimit;

    @Parameter(required = true)
    public double topSlopeLimit;

    @Parameter(required = false, description = "useful for spike studies")
    public String outputSpikesKey = null;

    @Parameter(required = true, defaultValue = "2")
    public int maxSpikeLength = 2;

    @Parameter(required = false)
    public boolean addSpikeInfo = false;

    int roi;
    int leftBorder = 10;


    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, dataKey, double[].class);
        Utils.isKeyValid(item, startCellKey, short[].class);
        Utils.isKeyValid(item, "NROI", Integer.class);

        double[] data = (double[]) item.get(dataKey);
        double[] result = new double[data.length];
        System.arraycopy(data, 0, result, 0, data.length);
        roi = (Integer) item.get("NROI");
        short[] startCells = (short[]) item.get(startCellKey);

        for (int spikeLength = 1; spikeLength <= maxSpikeLength; spikeLength++) {
            SpikeInfos spikeInfos = null;
            if (addSpikeInfo == true) {
                spikeInfos = new SpikeInfos();
            }

            for (int px = 0; px < Constants.N_PIXELS; px++) {
                int rightBorder = roi - spikeLength;
                // we want to skip the timemarker signal (which only occur in files with roi == 300) in the spike removal
                if (px % 9 == 8 && roi == 300) {
                    rightBorder = 260;
                }
                for (int sl = leftBorder; sl < rightBorder; sl++) {
                    int slice = px * roi + sl;
                    boolean isSpike = true;
                    double averTopSlope = 0;
                    double averTopValues = 0;
                    // Check for a jump up:
                    if (result[slice] - result[slice - 1] > spikeLimit) {
                        averTopValues += result[slice];
                        for (int topSlice = 1; topSlice < spikeLength && isSpike == true; topSlice++) {
                            // Check for small steps (with a maximum slope of topSlope):
                            if (Math.abs(result[slice + topSlice] - result[slice + topSlice - 1]) >= topSlopeLimit) {
                                isSpike = false;
                            } else {
                                averTopSlope += result[slice + topSlice] - result[slice + topSlice - 1];
                                averTopValues += result[slice + topSlice];
                            }
                        }
                        if (isSpike == true) {
                            if (result[slice + spikeLength] - result[slice + spikeLength - 1] < -spikeLimit) {
                                if (spikeLength > 1) {
                                    averTopSlope /= (spikeLength - 1);
                                    averTopValues /= spikeLength;
                                }
                                double spikeHeight = CorrectSpike(slice, spikeLength, averTopValues, result);
                                if (addSpikeInfo == true) {
                                    spikeInfos.addSpike(px, sl, startCells[px], spikeHeight, averTopSlope);
                                }
                            }
                        }
                    }
                }
            }

            if (addSpikeInfo == true) {
                spikeInfos.addInfosToDataItem(item, spikeLength, outputSpikesKey);
            }

        }

        item.put(outputKey, result);

        return item;
    }

    private double CorrectSpike(int pos, int spikeLength, double averTopValues, double[] result) {
        double spikeHeight = 0;

        double averBaseValues = (result[pos - 1] + result[pos + spikeLength]) / 2.0;

        spikeHeight = averTopValues - averBaseValues;

        for (int sl = 0; sl < spikeLength; sl++) {
            result[pos + sl] -= spikeHeight;
        }

        return spikeHeight;
    }
}
