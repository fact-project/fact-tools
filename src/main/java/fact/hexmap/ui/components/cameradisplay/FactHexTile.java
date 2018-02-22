package fact.hexmap.ui.components.cameradisplay;

import fact.hexmap.CameraPixel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;


/**
 * @author kai
 */
public class FactHexTile extends Component {

    /**
     * The unique class ID
     */
    private static final long serialVersionUID = 9128615424218016999L;

    static Logger log = LoggerFactory.getLogger(FactHexTile.class);

    public final CameraPixel pixel;

    private final double height;
    private final double width;
    private final double radius;
    private final Point center;
    private final Polygon hexagon;

    public Color borderColor = Color.WHITE;
    public Color fillColor = Color.BLUE;

    public FactHexTile(Point center, CameraPixel p, double radius) {
        this.pixel = p;
        this.radius = radius;
        this.width = radius * 2;
        this.height = radius * Math.sqrt(3);
        this.center = center;
        this.hexagon = createHexagon(center, radius);
    }

    /**
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Color c = g.getColor();

        g.setColor(fillColor);
        g.fillPolygon(hexagon);

        g.setColor(this.borderColor);
        g.drawPolygon(hexagon);

        g.setColor(c);
    }


    public Polygon createHexagon(Point center, double radius) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < 6; i++) {
            double alpha = i * Math.PI / 3;
            //if you get switch cos and sin in the following statements you will rotate the hexagon by 90 degrees
            //In this case we want the pointy top variant.
            double x = radius * 0.8 * Math.sin(alpha);
            double y = radius * 0.8 * Math.cos(alpha);

            int px = (int) Math.round(center.x + x);
            int py = (int) Math.round(center.y + y);
            polygon.addPoint(px, py);
        }

        return polygon;
    }


    public boolean equals(Object o) {
        if (o instanceof FactHexTile) {
            FactHexTile other = (FactHexTile) o;
            if (this.pixel.equals(other.pixel)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(Point p) {
        return hexagon.contains(p);
    }
}
