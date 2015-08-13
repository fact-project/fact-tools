package fact.hexmap.ui.overlays;

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


    public EllipseOverlay(double center_x, double center_y, double semi_axis_x, double semi_axis_y, double angle){
        this.center_y = center_x;
        this.center_x = center_y;
        //we mulitply by 4 since the width and height attribute refer to the semimajor axis or however you call that
        //and we want to have 2 sigma of all pixels in the ellipse we multiply by two again.
        this.ellipse_height = semi_axis_y*2;
        this.ellipse_width = semi_axis_x*2;
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

        double scalingX = 0.172*radius;
        double scalingY = 0.184*radius;

        Ellipse2D el = new Ellipse2D.Double( - 0.5 * ellipse_height * scalingX, - 0.5 * ellipse_width * scalingY,
                this.ellipse_height *scalingX, this.ellipse_width * scalingY);


//        Line2D width = new Line2D.Double(0, 0, 0, ellipse_width);

        double centerX = center_y *scalingX;
        double centerY = -center_x *scalingY ;


        Line2D height = new Line2D.Double(-ellipse_height*0.6, 0, ellipse_height*0.6, 0);
//        Line2D line= new Line2D.Double(0, 0, 0, -ellipse_height*0.5);
        float[] dash = {5.0f};



        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dash, 0.0f));
        g2.setPaint(Color.white);
        g2.translate(centerX, centerY);


        g2.rotate(-angle);
        g2.draw(height);
//        g2.translate(-ellipse_height, 0);
//        g2.setStroke(new BasicStroke(1));
//        g2.translate(ellipse_height, 0);
//        g2.draw(arrowHead);
//        g2.translate(-ellipse_height, 0);
//        g2.translate(ellipse_height, 0);

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
