package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Calculate the center pixel set that would e.g. be illuminated by the moon when tracking it
 *
 * Created by jbuss on 10.02.16.
 */
public class calculateCenterPixelSet implements Processor{
    static Logger log = LoggerFactory.getLogger(SimpleThreshold.class);

    @Parameter(required = false, defaultValue = "centerset")
    private String outputKey="centerset";

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data item) {

        PixelSet centerPixelSet = calculateCenterPixelSet(log);
        item.put(outputKey+":", centerPixelSet);

        return item;
    }

    public PixelSet calculateCenterPixelSet(Logger log) {

        PixelSet starSet = new PixelSet();

        for (int i = 252; i < 260 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 270; i < 287 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 364; i < 395 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 405; i < 431 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 981; i < 1007 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 1089; i < 1115 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 1125; i < 1151 + 1; i++) {
            starSet.addById(i);
        }

        return starSet;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
