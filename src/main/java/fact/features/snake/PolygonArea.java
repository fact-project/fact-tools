package fact.features.snake;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PolygonArea implements Processor {
    @Parameter(required = true)
    private String outkey = null;

    @Parameter(required = true)
    private String polygonX = null;

    @Parameter(required = true)
    private String polygonY = null;

    @Override
    public Data process(Data input) {
        if (outkey == null) throw new RuntimeException("Key \"outkey\" not set");

        Utils.mapContainsKeys(input, polygonX, polygonY);


        double[] x = (double[]) input.get(polygonX);
        double[] y = (double[]) input.get(polygonY);

        final int N = x.length;

        float erg = 0;

        for (int i = 0; i < N; i++) {
            erg += (y[i] + y[(i + 1) % N]) * (x[i] - x[(i + 1) % N]);
        }

        input.put(outkey, Math.abs(0.5 * erg));

        return input;
    }
}
