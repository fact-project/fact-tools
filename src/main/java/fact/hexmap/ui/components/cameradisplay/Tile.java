/**
 *
 */
package fact.hexmap.ui.components.cameradisplay;

import fact.hexmap.CameraPixel;

import java.awt.*;

/**
 * @author kai
 */
public abstract class Tile extends Component {

    /**
     * The unique class ID
     */
    private static final long serialVersionUID = 8849968124603265083L;

    /**
     * The polygon which defines the shape of the tile
     */
    protected Polygon polygon;

    /**
     * Center position of this tile
     */
    protected Point position;


    private Color borderColor = Color.WHITE;
    private Color fillColor = Color.BLUE;


    public abstract CameraPixel getCameraPixel();

    Double value = 0.0d;


    //Calculates the center position from the abstract pixel coordinates according to the geometry of the tile
    //private abstract Point getPosition();

    public abstract Polygon getPolygon();

    @Override
    public boolean contains(Point p) {
        return getPolygon().contains(p);
    }


    /**
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintBackground(g);
        paintBorder(g);
    }

    /**
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paintBackground(Graphics g) {
        Polygon p = getPolygon();
        Color c = g.getColor();
        g.setColor(fillColor);

        CameraPixel pixel = (CameraPixel) this.getCameraPixel();
        /*
        if(pixel.softid == 0){
            g.setColor(Color.RED);
        }
        if(pixel.softid == 1){
            g.setColor(Color.GREEN);
        }
        if(pixel.softid == 1410){
            g.setColor(Color.YELLOW);
        }
        */

        g.fillPolygon(p);
        g.setColor(c);
    }

    public void paintBorder(Graphics g) {
        Polygon p = getPolygon();
        Color c = g.getColor();
        g.setColor(this.borderColor);
        g.drawPolygon(p);
        g.setColor(c);
    }

    /**
     * @return the value
     */
    public Double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Double value) {
        this.value = value;
    }


    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
}
