/**
 * 
 */
package fact.viewer.ui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author chris
 *
 */
public class HexTile extends Tile {

	/** The unique class ID */
	private static final long serialVersionUID = 9128615424218016999L;
	static Logger log = LoggerFactory.getLogger( HexTile.class );
	
	Double radius;
	Double height;
	Double width;
	Double s;
	Color color = Color.WHITE;
	Integer id;
	int geoX = 0, geoY = 0;
	Point location = null;
	Map<String,String> info = new HashMap<String,String>();
	
	private HexTile( Double radius ){
		super();
		
		this.radius = radius;
		
		this.height = 2.0d * radius * Math.sin( Math.PI / 3.0d );
		this.s = 3.0d * radius / 2.0d;
		this.width = 2.0d * radius;
	}
	
	public HexTile( int x, int y, Double radius ){
		this( radius );
		this.setPosition( x, y );
	}
	
	public void setPosition( Point p ){
		super.setPosition( p );
		geoX = p.x;
		geoY = p.y;
		location = new Point( geoX, geoY );
		polygon = new Polygon();
		
		Double xOff = 0.0d;
		Double yOff = 0.0d;
		if( p.x % 2 == 0 ){
			//yOff = - height * 0.5;
			//xOff = s * 0.5 - 2.5;
			//xOff = width * 0.5;
		}
		
		log.debug( "Setting center for hexagon at {},{}", p.x, p.y );
		int cx = p.x * s.intValue() + (int) (1.5 * width.intValue());
		int cy = width.intValue() + p.y * height.intValue() + (int) (0.5 * height);
		
		if( p.x % 2 == 0 ){
			yOff = - 0.5 * height;
			//xOff = - ( width - s );
		}
		
		Point center = new Point( cx, cy );
		log.debug( "    center is: {}, {}", cx, cy );
		
		for( int i = 0; i < 6; i++ ){
			Double alpha = ( Math.PI / 3.0d ) * i; /// (new Double( i ));
			Double x = radius * Math.cos( alpha );
			Double y = radius * Math.sin( alpha );

			int px = center.x + x.intValue() + xOff.intValue();
			int py = center.y + y.intValue() + yOff.intValue();
			
			polygon.addPoint( px, py );
			log.debug( "Adding point ( {}, {} )", px, py );
		}
	}


	/**
	 * @see fact.viewer.ui.Tile#getColor()
	 */
	@Override
	public Color getColor() {
		return color;
	}

	
	public void setColor( Color c ){
		this.color = c;
	}

	/**
	 * @see fact.viewer.ui.Tile#getShape()
	 */
	@Override
	public Polygon getShape() {
		return polygon;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	
	public int getGeoX(){
		return geoX;
	}
	
	public int getGeoY(){
		return geoY;
	}
	
	public int hashCode(){
		return location.hashCode();
	}
	
	
	public boolean equals( Object o ){
		if( o instanceof HexTile ){
			HexTile other = (HexTile) o;
			if( geoX == other.geoX && geoY == other.geoY ){
				return true;
			}
		}
		return false;
	}
	
	public void setInfo( Map<String,String> info ){
		this.info = info;
	}
	
	public Map<String,String> getInfo(){
		return info;
	}
}