package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Leakage implements Processor {
    static Logger log = LoggerFactory.getLogger(Leakage.class);

    @Parameter(required = true)
    public String pixelSetKey;

    @Parameter(required = true)
    public String weights;

    @Parameter(required = true)
    public String leakage1OutputKey;

    @Parameter(required = true)
    public String leakage2OutputKey;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, pixelSetKey, weights);

        PixelSet showerPixel = (PixelSet) item.get(pixelSetKey);
        double[] photonCharge = (double[]) item.get(weights);


        double size = 0;

        double leakageBorder = 0;
        double leakageSecondBorder = 0;

        for (CameraPixel pix : showerPixel) {
            size += photonCharge[pix.id];
            if (isBorderPixel(pix)) {
                leakageBorder += photonCharge[pix.id];
                leakageSecondBorder += photonCharge[pix.id];
            } else if (isSecondBorderPixel(pix)) {
                leakageSecondBorder += photonCharge[pix.id];
            }
        }
        leakageBorder = leakageBorder / size;
        leakageSecondBorder = leakageSecondBorder / size;


        item.put(leakage1OutputKey, leakageBorder);
        item.put(leakage2OutputKey, leakageSecondBorder);
        return item;


    }

    // this is of course not the most efficient solution
    boolean isSecondBorderPixel(CameraPixel pixel) {
        for (CameraPixel neighbor : pixelMap.getNeighborsForPixel(pixel)) {
            if (isBorderPixel(neighbor)) {
                return true;
            }
        }
        return false;
    }

    boolean isBorderPixel(CameraPixel pix) {
        return pixelMap.getNeighborsForPixel(pix).length < 6;
    }

}
