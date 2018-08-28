package fact.features.snake;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.awt.*;

public class PolygonIntegrate implements Processor {

    @Parameter(required = true)
    private String key = null;

    @Parameter(required = true)
    private String polygonX = null;

    @Parameter(required = true)
    private String polygonY = null;

    @Parameter(required = true)
    private String outkey = null;                     // Summe aller Pixel deren Mittelpunkt innerhalb der Snake liegt

    @Parameter(required = true)
    private String outkeyNumberOfPixel = null;        // Anzahl an Pixel die Innerhalb der Snake liegen

    @Parameter(required = true)
    private String outkeyPixelList = null;            // Liste an Pixeln die Innerhalb der Snake liegen

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, key, polygonX, polygonY);


        double[] data = (double[]) item.get(key);
        double[] polyX = (double[]) item.get(polygonX);
        double[] polyY = (double[]) item.get(polygonY);

        Polygon poly = new Polygon();    // Wandel die Snake in ein Polygon um

        for (int i = 0; i < polyX.length; i++) {
            poly.addPoint((int) polyX[i], (int) polyY[i]);
        }

        int numberOfPixel = 0;
        boolean[] chidInPoly = new boolean[Constants.N_PIXELS];

        double erg = 0;
        for (int i = 0; i < Constants.N_PIXELS; i++) {
            if (poly.contains(pixelMap.getPixelFromId(i).getXPositionInMM(), pixelMap.getPixelFromId(i).getYPositionInMM()))    // Prüfe ob Pixel im Poly/Snake liegt
            {
                erg += data[i];

                chidInPoly[i] = true;
                numberOfPixel++;
            }
        }

        int[] chids = new int[numberOfPixel];
        for (int i = 0, tmpCount = 0; i < Constants.N_PIXELS; i++) {
            if (chidInPoly[i]) chids[tmpCount++] = i;
        }

        item.put(outkey, erg);
        item.put(outkeyNumberOfPixel, numberOfPixel);
        item.put(outkeyPixelList, chids);

        return item;
    }
}
