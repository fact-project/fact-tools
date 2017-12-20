package fact.utils;

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

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        short[] fakeStartCells = new short[npix];

        for (int px = 0; px < npix; px++) {
            fakeStartCells[px] = (short) random.nextInt(1023);
        }

        input.put(outputKey, fakeStartCells);

        return input;
    }
}
