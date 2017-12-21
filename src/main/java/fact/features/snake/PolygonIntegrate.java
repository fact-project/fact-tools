package fact.features.snake;

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

    private int npix;


    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        Utils.mapContainsKeys(input, key, polygonX, polygonY);


        double[] data = (double[]) input.get(key);
        double[] polyX = (double[]) input.get(polygonX);
        double[] polyY = (double[]) input.get(polygonY);

        Polygon poly = new Polygon();    // Wandel die Snake in ein Polygon um

        for (int i = 0; i < polyX.length; i++) {
            poly.addPoint((int) polyX[i], (int) polyY[i]);
        }

        int numberOfPixel = 0;
        boolean[] chidInPoly = new boolean[1440];

        double erg = 0;
        for (int i = 0; i < npix; i++) {
            if (poly.contains(pixelMap.getPixelFromId(i).getXPositionInMM(), pixelMap.getPixelFromId(i).getYPositionInMM()))    // Prüfe ob Pixel im Poly/Snake liegt
            {
                erg += data[i];

                chidInPoly[i] = true;
                numberOfPixel++;
            }
        }

        int[] chids = new int[numberOfPixel];
        for (int i = 0, tmpCount = 0; i < npix; i++) {
            if (chidInPoly[i]) chids[tmpCount++] = i;
        }

        input.put(outkey, erg);
        input.put(outkeyNumberOfPixel, numberOfPixel);
        input.put(outkeyPixelList, chids);

        return input;
    }
}
