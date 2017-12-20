package fact.hexmap.ui.components.cameradisplay;

import fact.hexmap.CameraPixel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;


/**
 * @author kai
 */
public class FactHexTile extends Tile {

    /**
     * The unique class ID
     */
    private static final long serialVersionUID = 9128615424218016999L;

    static Logger log = LoggerFactory.getLogger(FactHexTile.class);

    private double height;
    private double width;
    private double radius;
    private CameraPixel pixel;

    public FactHexTile(CameraPixel p, double radius) {
        this.pixel = p;
        this.radius = radius;
        this.width = radius * 2;
        this.height = radius * Math.sqrt(3);
        this.position = this.getPosition();
        this.polygon = this.getPolygon();
    }

    @Override
    public CameraPixel getCameraPixel() {
        return this.pixel;
    }

    //@Override
    private Point getPosition() {
        if (this.position == null) {
            int posX = this.pixel.geometricX;
            int posY = this.pixel.geometricY;

            //intentional precision loss
            int cx = posX * (int) (width * 0.75);
            int cy = posY * (int) height;

            this.position = new Point(cx, cy);
        }
        return this.position;
    }

    @Override
    public Polygon getPolygon() {
        double[] alphas = {0.0, 1.0471975511965976, 2.0943951023931953,
                3.141592653589793, 4.1887902047863905, 5.235987755982988};
        if (this.polygon == null) {
            double yOff = 0.0d;

            if (this.pixel.geometricX % 2 == 0) {
                yOff = -0.5 * this.height;
            }

            Polygon polygon = new Polygon();
            for (int i = 0; i < 6; i++) {
                double alpha = alphas[i]; /// (new Double( i ));
                //if you get switch cos and sin in the following statements you will rotate the hexagon by 90 degrees
                //In this case we want the flat edge on the bottom. Note that the whole thing is later
                //rotated by 90 degree in the viewer. So in the display the pointy thing will actually be on the
                //bottom
                double x = this.radius * 0.8 * Math.cos(alpha);
                double y = this.radius * 0.8 * Math.sin(alpha);

                int px = (int) Math.round(this.position.x + x);
                int py = (int) Math.round(this.position.y + y + yOff);
                polygon.addPoint(px, py);
            }
            this.polygon = polygon;
        }
        return this.polygon;
    }


    public boolean equals(Object o) {
        if (o instanceof FactHexTile) {
            FactHexTile other = (FactHexTile) o;
            if (this.pixel.equals(other.getCameraPixel())) {
                return true;
            }
        }
        return false;
    }

}
