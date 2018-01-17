package fact.utils;

import fact.Constants;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Random;

public class CreateFakeStartCells implements Processor {

    @Parameter(required = true)
    String outputKey = null;

    long seed = 0;

    Random random = new Random(seed);

    @Override
    public Data process(Data item) {
        short[] fakeStartCells = new short[Constants.N_PIXELS];

        for (int px = 0; px < Constants.N_PIXELS; px++) {
            fakeStartCells[px] = (short) random.nextInt(1023);
        }

        item.put(outputKey, fakeStartCells);

        return item;
    }
}
