package fact.statistics;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PatchAverage implements Processor {
    static Logger log = LoggerFactory.getLogger(PatchAverage.class);

    @Parameter(description = "Key pointing to the input array with length npix or npix * roi", required = true)
    String key=null;
    @Parameter(description = "Key for the output array, if not given, '<key>:patchAverage' is used", required = false)
    String outputKey=null;
    @Parameter(required = false, description="If true, ignore the 9th channel of each patch, which contains the timemarker", defaultValue="true")
    boolean ignoreTimeMarker = true;

    int usablePixelsPerPatch = 9;


    private int npix;

    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys( item, key);
        Utils.isKeyValid(item, "NPIX", Integer.class);

        if (outputKey == null){
            outputKey = key + ":patchAverage";
        }

        npix = (Integer) item.get("NPIX");

        double[] data = (double[]) item.get(key);
        double[] result = new double[data.length];

        if (ignoreTimeMarker){
            usablePixelsPerPatch = 8;
        }

        int numberOfPatches = npix / 9;
        int currentRoi = data.length / npix;

        for (int patch = 0; patch < numberOfPatches; patch++){
            for (int sl = 0; sl < currentRoi; sl++){

                for (int px = 0 ; px < usablePixelsPerPatch ; px++){
                    int slice = (patch * 9 + px) * currentRoi + sl;
                    result[patch * 9 * currentRoi + sl] += data[slice];
                }
                result[patch * 9 * currentRoi + sl] /= 9;

                for (int px = 0 ; px < 9 ; px++){
                    int slice = (patch * 9 + px) * currentRoi + sl;
                    result[slice] = result[patch*9*currentRoi+sl];
                }
            }
        }
        item.put(outputKey, result);

        return item;
    }
}
