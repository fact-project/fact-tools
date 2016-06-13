package fact.features;

import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Leakage implements Processor {
    static Logger log = LoggerFactory.getLogger(Leakage.class);

    @Parameter(required = true)
    private String pixelSetKey;
    @Parameter(required = true)
    private String weights;
    @Parameter(required = true)
    private String leakage1OutputKey;
    @Parameter(required = true)
    private String leakage2OutputKey;
    @Parameter(required = true)
    private String leakage3OutputKey;
    @Parameter(required = true)
    private String leakage4OutputKey;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys( input, pixelSetKey, weights);

        PixelSet showerPixel = (PixelSet) input.get(pixelSetKey);
        double[] photonCharge = (double[]) input.get(weights);


        double size = 0;
        double size2 = 0;

        double leakageBorder          = 0;
        double leakageSecondBorder    = 0;
        double leakageBorder1         = 0;
        double leakageSecondBorder1   = 0;
        for (CameraPixel pix: showerPixel.set){
            size += photonCharge[pix.id];
            size2+= 1;
            if (isBorderPixel(pix.id) ){
                leakageBorder          += photonCharge[pix.id];
                leakageSecondBorder    += photonCharge[pix.id];
                leakageBorder1         += 1;
                leakageSecondBorder1   += 1;
            }
            else if (isSecondBorderPixel(pix.id)){
                leakageSecondBorder    += photonCharge[pix.id];
                leakageSecondBorder1   += 1;
            }
        }
        leakageBorder          = leakageBorder        / size;
        leakageSecondBorder    = leakageSecondBorder  / size;
        leakageBorder1          = leakageBorder1        / size2;
        leakageSecondBorder1    = leakageSecondBorder1  / size2;

        input.put(leakage1OutputKey , leakageBorder);
        input.put(leakage2OutputKey , leakageSecondBorder);
        input.put(leakage3OutputKey , leakageBorder1);
        input.put(leakage4OutputKey , leakageSecondBorder1);
        return input;
    }

    //this is of course not the most efficient solution

    boolean isSecondBorderPixel(int pix){
        for(FactCameraPixel nPix: pixelMap.getNeighboursFromID(pix))
        {
            if(isBorderPixel(nPix.id)){
                return true;
            }
        }
        return false;
    }
    boolean isBorderPixel(int pix){
        return pixelMap.getNeighboursFromID(pix).length < 6;
    }

    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }

    public String getWeights() {
        return weights;
    }
    public void setWeights(String weights) {
        this.weights = weights;
    }

    public String getLeakage1OutputKey() {
        return leakage1OutputKey;
    }

    public void setLeakage1OutputKey(String leakage1OutputKey) {
        this.leakage1OutputKey = leakage1OutputKey;
    }

    public String getLeakage2OutputKey() {
        return leakage2OutputKey;
    }

    public void setLeakage2OutputKey(String leakage2OutputKey) {
        this.leakage2OutputKey = leakage2OutputKey;
    }

    public String getLeakage3OutputKey() {
        return leakage3OutputKey;
    }

    public void setLeakage3OutputKey(String leakage3OutputKey) {
        this.leakage3OutputKey = leakage3OutputKey;
    }

    public String getLeakage4OutputKey() {
        return leakage4OutputKey;
    }

    public void setLeakage4OutputKey(String leakage4OutputKey) {
        this.leakage4OutputKey = leakage4OutputKey;
    }

}
