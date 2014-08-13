package fact.hexmap.ui.overlays;

import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;

/**
 * Created by kaibrugge on 28.05.14.
 */
public interface CameraMapOverlay{
    public void setColor(Color c);
    public void paint(Graphics2D g2, FactHexMapDisplay map);
}
