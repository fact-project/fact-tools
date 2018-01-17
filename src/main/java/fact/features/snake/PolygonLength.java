package fact.features.snake;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PolygonLength implements Processor {
    @Parameter(required = true)
    String outkey;

    @Parameter(required = true)
    String polygonX = null;

    @Parameter(required = true)
    String polygonY = null;

    @Override
    public Data process(Data item) {
        if (outkey == null) throw new RuntimeException("Key \"outkey\" not set");

        Utils.mapContainsKeys(item, polygonX, polygonY);


        double[] x = (double[]) item.get(polygonX);
        double[] y = (double[]) item.get(polygonY);

        if (x.length < 3) item.put(outkey, 0);

        double erg = 0;
        for (int i = 1; i < x.length; i++) {
            double a = (double) (x[i] - x[i - 1]);
            a *= a;
            double b = (double) (y[i] - y[i - 1]);
            b *= b;

            erg += Math.sqrt(a + b);
        }

        double a = (double) (x[0] - x[x.length - 1]);
        a *= a;
        double b = (double) (y[0] - y[y.length - 1]);
        b *= b;
        erg += Math.sqrt(a + b);

        item.put(outkey, Math.abs(erg));

        return item;
    }
}
