package fact.container;

import com.google.common.collect.ForwardingSet;
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
 * This class implements a set like container for IACT Camera Pixels.
 * It can also draw itself in the Viewer.
 */
public class PixelSet extends ForwardingSet<CameraPixel> implements CameraMapOverlay, Serializable, Iterable<CameraPixel> {
    public Set<CameraPixel> set = new HashSet<>();
    Color c = Color.WHITE;

    public PixelSet() {
    }

    public PixelSet(HashSet<Integer> set) {
        for (Integer pix : set) {
            this.addById(pix);
        }
    }

    public static PixelSet fromIDs(int[] chidArray) {
        PixelSet pixelSet = new PixelSet();
        for (int chid : chidArray) {
            pixelSet.addById(chid);
        }
        return pixelSet;
    }

    @Override
    protected Set<CameraPixel> delegate() {
        return set;
    }

    public void addById(int id) {
        set.add(FactPixelMapping.getInstance().getPixelFromId(id));
    }

    public boolean containsID(int id) {
        return set.contains(FactPixelMapping.getInstance().getPixelFromId(id));
    }

    public boolean containsAllIDs(int[] ids) {
        return set.containsAll(PixelSet.fromIDs(ids));
    }


    public int[] toIntArray() {
        return set.stream().mapToInt(p -> p.id).toArray();
    }

    public ArrayList<Integer> toArrayList() {
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
