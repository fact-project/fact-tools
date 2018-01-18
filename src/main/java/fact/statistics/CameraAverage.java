package fact.statistics;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class CameraAverage implements Processor {

    static Logger log = LoggerFactory.getLogger(CameraAverage.class);

    @Parameter(required = true)
    public String key;
    public String outputKey;

    private int npix;

    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, key);
        Utils.isKeyValid(item, "NPIX", Integer.class);
        npix = (Integer) item.get("NPIX");

        double[] data = (double[]) item.get(key);
        int currentRoi = data.length / npix;

        double[] result = new double[currentRoi];
        for (int sl = 0; sl < currentRoi; sl++) {
            for (int px = 0; px < npix; px++) {
                result[sl] += data[px * currentRoi + sl];
            }
            result[sl] /= npix;
        }

        item.put(outputKey, result);
        return item;
    }

}
