package fact.hexmap.ui.overlays;

import fact.Constants;
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

    private final double center_x;
    private final double center_y;
    private final double ellipse_height;
    private final double ellipse_width;
    private Color fillColor = Color.GRAY;
    private double angle = 0;

    Polygon arrowHead = new Polygon();


    public EllipseOverlay(double center_x, double center_y, double semi_axis_x, double semi_axis_y, double angle) {
        this.center_y = center_x;
        this.center_x = center_y;
        this.ellipse_height = semi_axis_y * 2;
        this.ellipse_width = semi_axis_x * 2;
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
                -0.5 * ellipse_height, -0.5 * ellipse_width,
                this.ellipse_height, this.ellipse_width
        );


        double centerX = center_y * scaling;
        double centerY = -center_x * scaling;


        Line2D height = new Line2D.Double(-ellipse_height * 0.6, 0, ellipse_height * 0.6, 0);
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
