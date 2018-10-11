package fact.container;

import com.google.common.collect.ForwardingSet;
import fact.Constants;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;
import fact.hexmap.ui.components.cameradisplay.FactHexTile;
import fact.hexmap.ui.overlays.CameraMapOverlay;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements a set like container for FACT Camera Pixels.
 * It can also draw itself in the Viewer.
 */
public class PixelSet extends ForwardingSet<CameraPixel> implements CameraMapOverlay, Serializable, Iterable<CameraPixel> {
    public Set<CameraPixel> set = new HashSet<>();
    Color c = Color.WHITE;

    public PixelSet() {
    }

    public PixelSet(HashSet<Integer> set) {
        for (Integer pix : set) {
            this.addByCHID(pix);
        }
    }

    /**
     * Create a PixelSet from an integer array containing the chids of
     * the pixels to be added to the set.
     *
     * @param chidArray integer array with pixel chids
     * @return PixelSet containing all pixels given in chidArray
     */
    public static PixelSet fromCHIDs(int[] chidArray) {
        PixelSet pixelSet = new PixelSet();
        for (int chid : chidArray) {
            pixelSet.addByCHID(chid);
        }
        return pixelSet;
    }

    @Override
    protected Set<CameraPixel> delegate() {
        return set;
    }

    /**
     * Add a pixel by its chid
     *
     * @param chid of the pixel to be added
     */
    public void addByCHID(int chid) {
        set.add(FactPixelMapping.getInstance().getPixelFromId(chid));
    }

    /**
     * Test if the pixel with chid is in the set
     * @param chid
     * @return
     */
    public boolean containsCHID(int chid) {
        return set.contains(FactPixelMapping.getInstance().getPixelFromId(chid));
    }

    /**
     * Test if every pixel whose chid is given in chids is in the pixel set
     * @param chids
     * @return true iff all pixels are in the pixelset else false
     */
    public boolean containsAllCHIDs(int[] chids) {
        return set.containsAll(PixelSet.fromCHIDs(chids));
    }


    /**
     * Convert the set to an integer array of CHIDs
     * @return array containing all chids of the pixels in the set
     */
    public int[] toCHIDArray() {
        return set.stream().mapToInt(p -> p.id).toArray();
    }

    /**
     * Convert pixelset to a boolean array, array[chid] == True means, that
     * the pixel is in the set.
     * @return boolean mask of pixels in the set.
     */
    public boolean[] toBooleanArray() {
        boolean[] mask = new boolean[Constants.N_PIXELS];
        for (CameraPixel p: set) {
            mask[p.chid] = true;
        }
        return mask;
    }


    /**
     * Create a PixelSet from a boolean mask of length N_PIXELS
     * @param mask boolean array, array[chid] == True means the pixel will be added to the Set
     * @return PixelSet
     */
    public static PixelSet fromBooleanArray(boolean[] mask) {
        PixelSet pixelSet = new PixelSet();
        for (int chid = 0; chid < mask.length; chid++) {
            if (mask[chid]) {
                pixelSet.addByCHID(chid);
            }
        }
        return pixelSet;
    }

    /**
     * @return PixelSet converted to an ArrayList<Integer> of chids
     */
    public ArrayList<Integer> toCHIDArrayList() {
        ArrayList<Integer> chidArrayList = new ArrayList<Integer>();
        for (CameraPixel px : this.set) {
            chidArrayList.add(px.id);
        }
        return chidArrayList;
    }

    @Override
    public void setColor(Color c) {
        this.c = c;
    }

    @Override
    public void paint(Graphics2D g2, FactHexMapDisplay map) {
        for (FactHexTile t : map.getTiles()) {
            if (set.contains(t.pixel)) {
                if (t.borderColor != Color.BLACK) {
                    t.borderColor = Color.YELLOW;
                } else {
                    t.borderColor = this.c;
                }
                t.paint(g2);
            }
        }
    }

    public void clear() {
        set.clear();
    }

    @Override
    public int getDrawRank() {
        return 1;
    }

}
