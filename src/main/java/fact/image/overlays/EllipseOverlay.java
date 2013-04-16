package fact.image.overlays;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import fact.viewer.ui.CameraPixelMap;
import fact.viewer.ui.HexTile;

public class EllipseOverlay implements Overlay, Serializable {
	/** The unique class ID */
	private static final long serialVersionUID = 2895110976474196104L;

	private Color color = Color.WHITE;
	private Shape shape;
	private Stroke stroke = new BasicStroke(2.0f);
	protected CameraPixelMap camMap;
	protected Point camOriginPoint;

	private double centerX;

	private double centerY;

	private double width;

	private double length;

	private double delta;

	protected double distance;

	public EllipseOverlay(Shape shape) {

		this.shape = shape;
	}

	public EllipseOverlay(Shape shape, Color color) {
		this.color = color;
		this.shape = shape;

	}

	public EllipseOverlay(Shape shape, Color color, Stroke stroke) {
		this.color = color;
		this.shape = shape;
		this.stroke = stroke;
	}

	public EllipseOverlay(double centerX, double centerY, double width,
			double length, double distance, double delta) {
		if (camMap != null) {
			camOriginPoint = camMap.getPixelCoordsFromRealCoords(centerX,
					centerY);
		}
		this.centerX = centerX;
		this.centerY = centerY;
		// TODO: fix it?
		this.width = length;
		this.length = width;// !!
		this.delta = delta;
		this.distance = distance;
		// shape = new Ellipse2D.Double(point.x, point.y, width, length);
	}

	@Override
	public void paint(Graphics g, HexTile[][] cells) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(color);
		g2.setStroke(stroke);
		if (camMap != null) {
			camOriginPoint = camMap.getPixelCoordsFromRealCoords(0.0, 0.0);
			Point p = camMap.getPixelCoordsFromRealCoords(centerX, -centerY);
			shape = new Ellipse2D.Double(0.0 - width * 0.5, 0.0 - length * 0.5,
					width * 10, length * 10);
			AffineTransform af = g2.getTransform();
			AffineTransform newTrans = new AffineTransform();
			// newTrans.translate(camOriginPoint.x, camOriginPoint.y);
			newTrans.translate(p.x, p.y);
			newTrans.rotate(-delta * (Math.PI) / 180);
			// newTrans.translate(distance, 0.0);
			// newTrans.translate(p.x, p.y);

			// newTrans.ro

			g2.setTransform(newTrans);
			g2.draw(shape);
			g2.setTransform(af);
			g2.setColor(Color.BLUE);

			g2.fill(new Rectangle2D.Double(p.x, p.y, 10, 10));
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
