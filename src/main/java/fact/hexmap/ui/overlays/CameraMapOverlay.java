package fact.hexmap.ui.overlays;

import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by kaibrugge on 28.05.14.
 * modified by dbaack 
 */
public interface CameraMapOverlay extends Serializable {
    public void setColor(Color c);
    public void paint(Graphics2D g2, FactHexMapDisplay map, int slice);
    public int getDrawRank();	// Wie hoeher die Nummer je spaeter wird es gezeichnet -> hoehere Prioritaet
}
