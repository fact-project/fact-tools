/**
 * 
 */
package fact.viewer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

/**
 * @author chris
 * 
 */
public abstract class Tile extends Component {

	/** The unique class ID */
	private static final long serialVersionUID = 8849968124603265083L;

	protected Point position;
	protected Polygon polygon;

	Color borderColor = Color.DARK_GRAY;
	Double value = 0.0d;

	public Tile() {
		polygon = new Polygon();
		position = new Point(0, 0);
	}

	public void setPosition(int x, int y) {
		setPosition(new Point(x, y));
	}

	public void setPosition(Point p) {
		position = p;
	}

	public Point getPosition() {
		return position;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color bc) {
		this.borderColor = bc;
	}

	public Color getColor() {
		return Color.WHITE;
	}

	public abstract Polygon getShape();

	/**
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Color c = this.getColor();
		if (c != null) {
			paintBackground(g, c);
		}

		c = this.getBorderColor();
		if (c != null) {
			paintBorder(g, c);
		}
		// g.setColor(Color.WHITE);
		// // Line2D.Double l = new Line2D.Double();
		// g.setColor(Color.WHITE);
		// ((Graphics2D) g).draw(new Line2D.Double(0.0, 0.0, 200.0, 200.0));
		//
		// g.setColor(Color.WHITE);
		// ((Graphics2D) g).draw(new Line2D.Double(0.0, 0.0, 0.0, 300.0));
	}

	/**
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	public void paintBackground(Graphics g, Color fillColor) {
		Polygon p = getShape();
		g.setColor(fillColor);
		g.fillPolygon(p);
	}

	public void paintBorder(Graphics g, Color c) {
		Polygon p = getShape();
		g.setColor(c);
		g.drawPolygon(p);
	}

	/**
	 * @return the value
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Double value) {
		this.value = value;
	}
}