package fact.cleaning;

import fact.container.PixelSet;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

/**
 * Calculate the center pixel set that would e.g. be illuminated by the moon when tracking it
 *
 * Created by jbuss on 10.02.16.
 */
public class calculateCenterPixelSet implements StatefulProcessor{
    static Logger log = LoggerFactory.getLogger(SimpleThreshold.class);

    @Parameter(required = false, defaultValue = "centerset")
    private String outputKey="centerset";
    @Parameter(required = false, defaultValue = "false")
    private Boolean outerRing=false;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data item) {

        PixelSet pixelSet = new PixelSet();
        addCentralPixel(pixelSet);
        if (outerRing){
            addOuterRingPixel(pixelSet);
        }
        item.put(outputKey, pixelSet);

        return item;
    }

    private void addCentralPixel(PixelSet starSet) {


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
        return ;
    }

    private void addOuterRingPixel(PixelSet starSet ) {
        for (int i = 189; i < 206 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 247; i < 251 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 261; i < 269 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 315; i < 323 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 351; i < 363 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 396; i < 404 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 499; i < 512 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 513; i < 521 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 531; i < 539 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 909; i < 926 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 963; i < 966 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 972; i < 980 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 1026; i < 1029 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 1035; i < 1043 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 1080; i < 1088 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 1110; i < 1124 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 1179; i < 1182 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 1215; i < 1218 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 1224; i < 1241 + 1; i++) {
            starSet.addById(i);
        }

        for (int i = 1251; i < 1259 + 1; i++) {
            starSet.addById(i);
        }
        return ;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setOuterRing(Boolean outerRing) {
        this.outerRing = outerRing;
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


}
