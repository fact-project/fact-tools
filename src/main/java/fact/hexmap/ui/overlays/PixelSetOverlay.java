package fact.hexmap.ui.overlays;

import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;
import fact.hexmap.ui.components.cameradisplay.Tile;

import java.awt.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This is overlay can draw borders around the pixels passed to it via constructor or the add methods.
 */
public class PixelSetOverlay implements CameraMapOverlay, Serializable {
    public Set<CameraPixel> set = new HashSet<>();
    Color c = Color.WHITE;

    public PixelSetOverlay(HashSet<Integer> set) {
        for (Integer pix : set){
            this.addById(pix);
        }
    }
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

    public int[] toIntArray(){
        int intSet[] = new int[this.set.size()];
        int i = 0;
        for (CameraPixel px : this.set){
            intSet[i] = px.id;
            i++;
        }
        return intSet;
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

	@Override
	public int getDrawRank() {		
		return 1;
	}
}
