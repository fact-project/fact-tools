package fact.utils;

import stream.Data;
import stream.Processor;
import fact.Utils;

import java.util.Random;

public class CreateFakeStartCells implements Processor {

    String outputKey = null;
    
    long seed = 0;
    
    Random random = new Random(seed);
    
    private int npix;
    
    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        
        short[] fakeStartCells = new short[npix];
        
        for (int px=0 ; px < npix ; px++)
        {
            fakeStartCells[px] = (short) random.nextInt(1023);
        }
        
        input.put(outputKey, fakeStartCells);
        
        // TODO Auto-generated method stub
        return input;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

}
