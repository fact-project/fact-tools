package fact.mapping.ui.components.cameradisplay;

import fact.mapping.CameraPixel;
import fact.mapping.FactCameraPixel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;


/**
 * @author kai
 *
 */
public class FactHexTile extends Tile {

	/** The unique class ID */
	private static final long serialVersionUID = 9128615424218016999L;

    static Logger log = LoggerFactory.getLogger( FactHexTile.class );

	private Double height;
	private Double width;
    private double radius;
    private FactCameraPixel pixel;
	public FactHexTile(FactCameraPixel p, Double radius){
        this.pixel = p;
        this.radius = radius;
        this.width =radius* Math.sqrt(3);
        this.height = 2.0d * radius + 3;
        this.position = this.getPosition();
        this.polygon = this.getPolygon();
    }

    @Override
    public CameraPixel getCameraPixel() {
        return this.pixel;
    }

    @Override
	public Point getPosition(){
        if(this.position == null) {
            int posX = this.pixel.geometricX;
            int posY = this.pixel.geometricY;

            //intentional precision loss
            int s = (int)  (3 * (radius / 2.0d)  + 0.5);
            int cy = (int) (posY * width);

            int cx =  posX * s;
            this.position = new Point( cx, cy );

        }
        return this.position;
	}

    @Override
    public Polygon getPolygon() {
        if(this.polygon == null) {
            Double xOff = 0.0d;
            Double yOff = 0.0d;

            if (this.pixel.geometricX % 2 == 0) {
                yOff = -0.5 * this.height;
            }

            Polygon polygon = new Polygon();
            for (int i = 0; i < 6; i++) {
                Double alpha = (Math.PI / 3.0d) * i; /// (new Double( i ));
                //if you get switch cos and sin in the following statements you will get the edge of the hexagon
                //on the bottom. In this display we want the pointy thing on the bottom
                Double x = this.radius * Math.cos(alpha);
                Double y = this.radius * Math.sin(alpha);

                int px =  (this.position.x + x.intValue() + xOff.intValue());
                int py =  (this.position.y + y.intValue() + yOff.intValue());

                polygon.addPoint(px, py);
            }
            this.polygon = polygon;
        }
        return this.polygon;
    }


	public boolean equals( Object o ){
		if( o instanceof FactHexTile){
			FactHexTile other = (FactHexTile) o;
			if( this.pixel.equals(other.getCameraPixel())){
				return true;
			}
		}
		return false;
	}
	
}