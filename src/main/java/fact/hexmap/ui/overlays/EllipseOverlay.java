package fact.hexmap.ui.overlays;

import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.Serializable;

/**
 * Draws an ellipse on the cameraview. The cameraview is rotated by 90 degrees. So X values correspond
 * to the horizontal axis in the GUI
 * Created by bruegge on 7/31/14.
 */
public class EllipseOverlay implements CameraMapOverlay, Serializable {

    private final double cogY;
    private final double cogX;
    private final double ellipse_height;
    private final double ellipse_width;
    private Color fillColor = Color.GRAY;
    private double angle = 0;

    public EllipseOverlay(double cogX, double cogY, double width, double height, double angle){
        this.cogX = cogX;
        this.cogY = cogY;
        //we mulitply by 4 since the width and height attribute refer to the semimajor axis or however you call that
        //and we want to have 2 sigma of all pixels in the ellipse we multiply by two again.
        this.ellipse_height = height*4;
        this.ellipse_width = width*4;
        this.angle = angle;
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
        AffineTransform old = g2.getTransform();

        double scalingX = 0.172*radius;
        double scalingY = 0.184*radius;

        Ellipse2D el = new Ellipse2D.Double( - 0.5 * ellipse_height * scalingX, - 0.5 * ellipse_width * scalingY,
                this.ellipse_height *scalingX, this.ellipse_width * scalingY);


        double centerX = cogX*scalingX;
        double centerY = -cogY*scalingY ;
        g2.translate(centerX, centerY);
        g2.rotate(-angle);

        g2.draw(el);

        g2.setTransform(old);
    }
}
