package fact.mapping.ui.overlays;

import fact.mapping.CameraPixel;
import fact.mapping.FactPixelMapping;
import fact.mapping.ui.components.cameradisplay.Tile;
import fact.mapping.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This is overlay can draw borders around the pixels passed to it via constructor or the add methods.
 */
public class PixelSetOverlay implements CameraMapOverlay, Serializable {
    Set<CameraPixel> set = new HashSet<>();
    Color c = Color.WHITE;

    public PixelSetOverlay(Set<CameraPixel> set){
        this.set = set;
    }

    public PixelSetOverlay(){
    }
    public void add(CameraPixel p){
        set.add(p);
    }
    public void addById(int id){
        set.add(FactPixelMapping.getInstance().getPixelFromId(id));
    }

    @Override
    public void setColor(Color c) {
        this.c = c;
    }

    @Override
    public void paint(Graphics2D g2, FactHexMapDisplay map) {
        for (Tile t : map.getTiles()){
            if(set.contains(t.getCameraPixel())){
                if (t.getBorderColor() != Color.BLACK){
                    t.setBorderColor(Color.YELLOW);
                } else {
                    t.setBorderColor(this.c);
                }
                t.paint(g2);
            }
        }
    }

    public void clear() {
        set.clear();
    }
}
