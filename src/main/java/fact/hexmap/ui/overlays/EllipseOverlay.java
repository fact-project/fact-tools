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

    Polygon arrowHead = new Polygon();


    public EllipseOverlay(double centerX, double centerY, double semiAxisX, double semiAxisY, double angle) {
        this.centerY = centerX;
        this.centerX = centerY;
        this.ellipseHeight = semiAxisY * 2;
        this.ellipseWidth = semiAxisX * 2;
        this.angle = angle;

        arrowHead.addPoint(0, -5);
        arrowHead.addPoint(0, 5);
        arrowHead.addPoint(5, 0);

    }

    public EllipseOverlay(CameraCoordinate center, double semiAxisX, double semiAxisY, double angle) {
        this.centerY = center.xMM;
        this.centerX = center.yMM;
        this.ellipseHeight = semiAxisY * 2;
        this.ellipseWidth = semiAxisX * 2;
        this.angle = angle;

        arrowHead.addPoint(0, -5);
        arrowHead.addPoint(0, 5);
        arrowHead.addPoint(5, 0);

    }

    @Override
    public void setColor(Color c) {
        fillColor = c;
    }

    @Override
    public void paint(Graphics2D g2, FactHexMapDisplay map) {
        double radius = map.getTileRadiusInPixels();
        AffineTransform oldTransform = g2.getTransform();
        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();

        double scaling = radius / (Constants.PIXEL_SIZE_MM / Math.sqrt(3));

        Ellipse2D el = new Ellipse2D.Double(
                -0.5 * ellipseHeight, -0.5 * ellipseWidth,
                this.ellipseHeight, this.ellipseWidth
        );


        double centerX = centerY * scaling;
        double centerY = -this.centerX * scaling;


        Line2D height = new Line2D.Double(-ellipseHeight * 0.6, 0, ellipseHeight * 0.6, 0);
        float[] dash = {5.0f};

        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dash, 0.0f));
        g2.setPaint(Color.white);
        g2.translate(centerX, centerY);
        g2.rotate(-angle);
        g2.scale(scaling, scaling);
        g2.draw(height);
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
