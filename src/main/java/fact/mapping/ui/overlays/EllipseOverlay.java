package fact.mapping.ui.overlays;

import fact.mapping.FactPixelMapping;
import fact.mapping.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;

/**
 * Created by bruegge on 7/31/14.
 */
public class EllipseOverlay implements CameraMapOverlay, Serializable {

    private Color fillColor = Color.GRAY;
    private Color strokeColor = Color.red;
    private double angle = 0;
    private double ellipseCenterX = 0; //in screen pixels
    private double ellipseCenterY = 0; //in screen pixels
    private Shape el;
    FactPixelMapping mappping = FactPixelMapping.getInstance();

    public EllipseOverlay(double cogX, double cogY, double width, double length, double angle){
        //el = new Ellipse2D.Double(cogX - width*0.5, cogY - length*0.5, width, length);

        double scalingx = 1.21;
        double scalingy = 1.27;

        double offsetx = cogX*scalingx;
        double offsety = -cogY*scalingy;//(millimeter)
        double w = 3*width;
        double h = 3*length;


        this.ellipseCenterX = offsetx;
        this.ellipseCenterY = offsety;

        System.out.println("prinitng ellipse at pixels: " +ellipseCenterX + ", " + ellipseCenterY);

        el = new Ellipse2D.Double((0-w*0.5) + offsetx, (0-h*0.5) + offsety, w, h);
        this.angle = angle;
    }

    @Override
    public void setColor(Color c) {
        fillColor = c;
    }

    @Override
    public void paint(Graphics2D g2, FactHexMapDisplay map) {
        g2.setPaint(fillColor);
        g2.setStroke(new BasicStroke(2));
        AffineTransform old = g2.getTransform();
        g2.rotate(angle, ellipseCenterX, ellipseCenterY);
        g2.draw(el);
        g2.setTransform(old);
    }
}
