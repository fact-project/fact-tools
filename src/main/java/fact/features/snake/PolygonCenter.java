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
    public Data process(Data item) {
        Utils.mapContainsKeys(item, polygonX, polygonY);


        double[] x = (double[]) item.get(polygonX);
        double[] y = (double[]) item.get(polygonY);

        double centerX = 0;
        double centerY = 0;

        for (int i = 0; i < x.length; i++) {
            centerX += x[i];
            centerY += y[i];
        }
        centerX /= x.length;
        centerY /= y.length;

        item.put(outkeyX, centerX);
        item.put(outkeyY, centerY);

        return item;
    }
}
