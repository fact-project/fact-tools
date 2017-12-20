package fact.statistics;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PatchAverage implements Processor {
    static Logger log = LoggerFactory.getLogger(PatchAverage.class);

    @Parameter(required = true)
    String key = null;

    @Parameter(required = true)
    String outputKey = null;

    @Parameter(required = true)
    String color = null;

    private int npix;

    @Override
    public Data process(Data input) {
        // TODO Auto-generated method stub
        Utils.mapContainsKeys(input, key);
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        double[] data = (double[]) input.get(key);
        double[] result = new double[data.length];

        int numberOfPatches = npix / 9;
        int currentRoi = data.length / npix;

        for (int patch = 0; patch < numberOfPatches; patch++) {
            for (int sl = 0; sl < currentRoi; sl++) {
                for (int px = 0; px < 8; px++) {
                    int slice = (patch * 9 + px) * currentRoi + sl;
                    result[patch * 9 * currentRoi + sl] += data[slice];
                }
                result[patch * 9 * currentRoi + sl] /= 8;
                for (int px = 0; px < 9; px++) {
                    int slice = (patch * 9 + px) * currentRoi + sl;
                    result[slice] = result[patch * 9 * currentRoi + sl];
                }
            }
        }
        input.put(outputKey, result);

        return input;
    }
}
