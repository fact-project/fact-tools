package fact.hexmap.ui.components.cameradisplay;

import fact.hexmap.ui.colormapping.ColorMapping;

public interface PixelMapDisplay {

    public void setColorMap(ColorMapping m);

    /**
     * A PixelMap has a number of tiles it can display. In case of the PixelMap for the fact camera this would be 1440 pixel
     *
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

}
