package fact.mapping.ui.components.cameradisplay;

import fact.mapping.PixelMapping;
import fact.mapping.ui.components.cameradisplay.colormapping.ColorMapping;

public interface PixelMapDisplay {

    public void setColorMap(ColorMapping m);

    /**
     * A PixelMap has a number of tiles it can display. In case of the PixelMap for the fact camera this would be 1440 pixel
     * @return the number of tiles displayed
     */
    public int getNumberOfTiles();

    /**
     *
     */
    public int getWidth();

    /**
     *
     */
    public int getHeight();

    /**
     *
     */
    public Tile[] getTiles();

    /**
     * 
     */
    public PixelMapping getPixelMapping();


}
