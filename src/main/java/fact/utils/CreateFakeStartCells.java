package fact.utils;

import fact.Constants;
import fact.Utils;
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
    public Data process(Data input) {
        short[] fakeStartCells = new short[Constants.NUMBEROFPIXEL];

        for (int px = 0; px < Constants.NUMBEROFPIXEL; px++) {
            fakeStartCells[px] = (short) random.nextInt(1023);
        }

        input.put(outputKey, fakeStartCells);

        return input;
    }
}
