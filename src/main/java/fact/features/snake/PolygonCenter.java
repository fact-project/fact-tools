package fact.features.snake;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PolygonCenter implements Processor {
    @Parameter(required = true)
    private String polygonX = null;

    @Parameter(required = true)
    private String polygonY = null;

    @Parameter(required = true)
    private String outkeyX = null;

    @Parameter(required = true)
    private String outkeyY = null;

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, polygonX, polygonY);


        double[] x = (double[]) input.get(polygonX);
        double[] y = (double[]) input.get(polygonY);

        double centerX = 0;
        double centerY = 0;

        for (int i = 0; i < x.length; i++) {
            centerX += x[i];
            centerY += y[i];
        }
        centerX /= x.length;
        centerY /= y.length;

        input.put(outkeyX, centerX);
        input.put(outkeyY, centerY);

        return input;
    }
}
