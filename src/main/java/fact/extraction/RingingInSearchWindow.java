package fact.extraction;

import fact.Constants;
import fact.Utils;
import java.lang.Math;
//import fact.statistics.FastFourierTrafo;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class RingingInSearchWindow implements Processor {

    @Parameter(required=true)
    private String key = null;
    @Parameter(required=true)
    private String outputKey = null;
    @Parameter(required=false, defaultValue="30")
    private int searchWindowLeft;
    @Parameter(required=false, defaultValue="50")
    private int searchWindowRight;
    
    private int npix = Constants.NUMBEROFPIXEL;
    private int roi = 1024;
    
    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        Utils.isKeyValid(input, "NROI", Integer.class);
        npix = (Integer) input.get("NPIX");
        roi  = (Integer) input.get("NROI");
        
        double[] ringing_value = new double[npix];
        
        if (searchWindowLeft >= searchWindowRight)
        {
            throw new RuntimeException("searchWindowLeft is equal or larger than searchWindowRight: "+searchWindowLeft+" >= "+searchWindowRight);
        }
        
        Utils.mapContainsKeys(input, key);
        
        double[] data = (double[]) input.get(key);
        
        for (int px = 0 ; px < npix ; px++)
        {

            for (int sl = searchWindowLeft ; sl < searchWindowRight ; sl++)
            {
                int slice = px * roi + sl;
                ringing_value[px] += data[slice];
            }
            ringing_value[px] = ringing_value[px]/0.003;
        }
        input.put(outputKey, ringing_value);
        
        return input;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public int getSearchWindowLeft() {
        return searchWindowLeft;
    }

    public void setSearchWindowLeft(int searchWindowLeft) {
        if (searchWindowLeft < 0 || searchWindowLeft >= roi)
        {
            throw new RuntimeException("searchWindowLeft is not in the ROI: "+ searchWindowLeft);
        }
        this.searchWindowLeft = searchWindowLeft;
    }

    public int getSearchWindowRight() {
        return searchWindowRight;
    }

    public void setSearchWindowRight(int searchWindowRight) {
        if (searchWindowRight < 0 || searchWindowRight >= roi)
        {
            throw new RuntimeException("searchWindowRight is not in the ROI: "+ searchWindowRight);
        }
        this.searchWindowRight = searchWindowRight;
    }

}
