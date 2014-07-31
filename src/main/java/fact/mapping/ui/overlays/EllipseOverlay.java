package fact.mapping.ui.overlays;

import fact.mapping.FactPixelMapping;
import fact.mapping.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;

/**
 * Created by bruegge on 7/31/14.
 */
public class EllipseOverlay implements CameraMapOverlay, Serializable {

    private Color fillColor = Color.GRAY;
    private Color strokeColor = Color.red;
    private double angle = 0;
    private double cogX = 0;
    private double cogY = 0;
    private Ellipse2D el;
    FactPixelMapping mappping = FactPixelMapping.getInstance();

    public EllipseOverlay(double cogX, double cogY, double width, double length, double angle){
        System.out.println("prinitng ellipse at: " +cogX + ", " + cogY);
        //el = new Ellipse2D.Double(cogX - width*0.5, cogY - length*0.5, width, length);

        double scalingx = 14/9.5;

        double offsetx = 90.5*scalingx;
        double offsety = 0;
        el = new Ellipse2D.Double((0-20) + offsetx, (0-20) + offsety , 40, 40);
        this.cogX = cogX;
        this.cogY = cogY;
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
        //g2.rotate(angle, cogX, cogY );
        g2.draw(el);
        //g2.rotate(-angle,  cogX, cogY );
    }
}
