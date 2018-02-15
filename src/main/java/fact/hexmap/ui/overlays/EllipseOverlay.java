package fact.hexmap.ui.overlays;

import fact.Constants;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * Draws an ellipse on the cameraview. The cameraview is rotated by 90 degrees. So X values correspond
 * to the horizontal axis in the GUI
 * Created by bruegge on 7/31/14.
 */
public class EllipseOverlay implements CameraMapOverlay {

    private final double centerX;
    private final double centerY;
    private final double ellipseHeight;
    private final double ellipseWidth;
    private Color fillColor = Color.GRAY;
    private double angle = 0;

    public EllipseOverlay(double centerX, double centerY, double semiAxisX, double semiAxisY, double angle) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.ellipseHeight = semiAxisY * 2;
        this.ellipseWidth = semiAxisX * 2;
        this.angle = angle;
    }

    public EllipseOverlay(CameraCoordinate center, double semiAxisX, double semiAxisY, double angle) {
        this(center.xMM, center.yMM, semiAxisX, semiAxisY, angle);
    }

    @Override
    public void setColor(Color c) {
        fillColor = c;
    }

    @Override
    public void paint(Graphics2D g2, FactHexMapDisplay map) {
        AffineTransform oldTransform = g2.getTransform();
        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();

        Ellipse2D el = new Ellipse2D.Double(
                -0.5 * ellipseWidth, -0.5 * ellipseHeight,
                this.ellipseWidth, this.ellipseHeight
        );

        Point center = map.cameraCoordinateToPixels(this.centerX, this.centerY);

        Line2D width = new Line2D.Double(-ellipseWidth * 0.5, 0, ellipseWidth * 0.5, 0);
        float[] dash = {3.0f};

        g2.translate(center.x, center.y);
        g2.rotate(-angle);
        g2.scale(map.scalingX, map.scalingY);

        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dash, 0.0f));
        g2.setPaint(Color.white);
        g2.draw(width);

        g2.setStroke(new BasicStroke(2));
        g2.setPaint(fillColor);
        g2.draw(el);

        g2.setStroke(oldStroke);
        g2.setPaint(oldPaint);
        g2.setTransform(oldTransform);
    }

    @Override
    public int getDrawRank() {
        return 5;
    }
}
