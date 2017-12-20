package fact.hexmap.ui.overlays;

import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

/**
 * Draws an polygon on the cameraview.
 * <p>
 * Dominik Baack (dominik.baack@udo.edu)
 */
public class PolygonOverlay implements CameraMapOverlay, Serializable {
    private final double[] x;
    private final double[] y;
    private Color fillColor = Color.GRAY;

    public PolygonOverlay(double[] X, double[] Y) {
        x = X;
        y = Y;
    }

    @Override
    public void setColor(Color c) {
        fillColor = c;
    }

    @Override
    public void paint(Graphics2D g2, FactHexMapDisplay map) {
        double radius = map.getTileRadiusInPixels();

        Paint oldPaint = g2.getPaint();
        g2.setPaint(fillColor);

        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2));

        AffineTransform old = g2.getTransform();

        double scalingX = 0.172 * radius;
        double scalingY = 0.184 * radius;

        Polygon poly = new Polygon();
        for (int i = 0; i < x.length; i++) {
            poly.addPoint((int) (x[i] * scalingX), -(int) (y[i] * scalingY));
        }

        g2.translate(0, 0);

        g2.draw(poly);

        g2.setStroke(oldStroke);
        g2.setPaint(oldPaint);
        g2.setTransform(old);
    }

    public int getDrawRank() {
        return 4;
    }
}
