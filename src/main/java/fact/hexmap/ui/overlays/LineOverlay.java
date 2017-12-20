package fact.hexmap.ui.overlays;

import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.Serializable;

/**
 * Draws a Line the cameraview. The cameraview is rotated by 90 degrees. So X values correspond
 * to the horizontal axis in the GUI
 * Created by bruegge on 8/26/14.
 */
public class LineOverlay implements CameraMapOverlay, Serializable {

    private Color fillColor = Color.GRAY;
    double fromX = 0;
    double fromY = 0;

    double toX = 0;
    double toY = 0;

    public LineOverlay(double fromX, double fromY, double toX, double toY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    @Override
    public void setColor(Color c) {
        fillColor = c;
    }

    @Override
    public void paint(Graphics2D g2, FactHexMapDisplay map) {
        double radius = map.getTileRadiusInPixels();
        g2.setPaint(fillColor);
        g2.setStroke(new BasicStroke(2));

        double scalingX = 0.172 * radius;
        double scalingY = 0.184 * radius;
        Line2D line = new Line2D.Double(fromX * scalingX, -fromY * scalingY, toX * scalingX, -toY * scalingY);
        Ellipse2D el = new Ellipse2D.Double(fromX * scalingX - 3, -fromY * scalingY - 3, 6, 6);
        g2.draw(line);
        g2.draw(el);
        g2.setStroke(new BasicStroke(1));
    }

    @Override
    public int getDrawRank() {
        return 3;
    }
}
