package fact.image.overlays;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.Serializable;

import fact.viewer.ui.CameraPixelMap;
import fact.viewer.ui.HexTile;

public class SourceOverlay implements Overlay, Serializable {
	/** The unique class ID */
	private static final long serialVersionUID = 2895110976474196104L;

	private Color color = Color.WHITE;
	private Shape shape;
	private Stroke stroke = new BasicStroke(2.0f);
	public CameraPixelMap camMap;
	protected Point2D point;

	protected double height;

	protected double s;

	protected double width;

	public SourceOverlay(Point2D p) {
		this.point = p;
	}

	public SourceOverlay(double mSourceX, double mSourceY) {
		this.point = new Point2D.Double(mSourceX, mSourceY);
	}

	@Override
	public void paint(Graphics g, HexTile[][] cells) {
		if (camMap != null) {
//			camMap.cellRadius;
		
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(color);
			g2.setStroke(stroke);
			Point p = camMap.getPixelCoordsFromRealCoords(point.getX(), point.getY());
			shape = new Ellipse2D.Double(p.x, p.y, 10.0, 10);
			g2.draw(shape);
		}
		// g2.setColor(Color.WHITE);
		// ((Graphics2D) g2).draw(new Line2D.Double(0.0, 0.0, 200.0, 200.0));
		//
		// g2.setColor(Color.WHITE);
		// ((Graphics2D) g2).draw(new Line2D.Double(0.0, 0.0, 10.0, 300.0));

	}

	@Override
	public void setCamMap(CameraPixelMap camMap) {
		this.camMap = camMap;
		// point = camMap.getPixelCoordsFromRealCoords(xPixel, yPixel);

	}

}
