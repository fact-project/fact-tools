package fact.features.snake;

import fact.Utils;
import stream.Data;
import stream.Processor;

public class PolygonCenter implements Processor {
    private String polygonX = null;
    private String polygonY = null;
    private String outkeyX = null;
    private String outkeyY = null;

    @Override
    public Data process(Data input) {
        if (outkeyX == null) throw new RuntimeException("Key \"outkeyX\" not set");
        if (outkeyY == null) throw new RuntimeException("Key \"outkeyY\" not set");

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
