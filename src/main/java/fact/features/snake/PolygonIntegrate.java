package fact.features.snake;

import fact.Utils;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;

import java.awt.*;

public class PolygonIntegrate implements Processor {
    private String key = null;
    private String polygonX = null;
    private String polygonY = null;

    private String outkey = null;                // Summe aller Pixel deren Mittelpunkt innerhalb der Snake liegt
    private String outkeyNumberOfPixel = null;        // Anzahl an Pixel die Innerhalb der Snake liegen
    private String outkeyPixelList = null;            // Liste an Pixeln die Innerhalb der Snake liegen

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    private int npix;


    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        if (outkey == null) throw new RuntimeException("Key \"outkey\" not set");
        if (outkeyNumberOfPixel == null) throw new RuntimeException("Key \"outkeyNumberOfPixel\" not set");
        if (outkeyPixelList == null) throw new RuntimeException("Key \"outkeyPixelList\" not set");

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPolygonX() {
        return polygonX;
    }

    public void setPolygonX(String polygonX) {
        this.polygonX = polygonX;
    }

    public String getPolygonY() {
        return polygonY;
    }

    public void setPolygonY(String polygonY) {
        this.polygonY = polygonY;
    }

    public String getOutkey() {
        return outkey;
    }

    public void setOutkey(String outkey) {
        this.outkey = outkey;
    }

    public String getOutkeyNumberOfPixel() {
        return outkeyNumberOfPixel;
    }

    public void setOutkeyNumberOfPixel(String outkeyNumberOfPixel) {
        this.outkeyNumberOfPixel = outkeyNumberOfPixel;
    }

    public String getOutkeyPixelList() {
        return outkeyPixelList;
    }

    public void setOutkeyPixelList(String outkeyPixelList) {
        this.outkeyPixelList = outkeyPixelList;
    }


}
