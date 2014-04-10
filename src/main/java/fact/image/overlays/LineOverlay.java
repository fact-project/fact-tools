package fact.image.overlays;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.io.Serializable;

import fact.viewer.ui.CameraPixelMap;
import fact.viewer.ui.HexTile;

public class LineOverlay implements Overlay, Serializable {

	private static final long serialVersionUID = -8716719485927279356L;
	private Color color = Color.WHITE;
	private Shape shape;
	private Stroke stroke = new BasicStroke(2.0f);
	private CameraPixelMap camMap;
	private double startX;
	private double startY;
	private double phi;
	
	public LineOverlay(double startX, double startY, double phi, Color c){
		this.startX = startX;
		this.startY = startY;
		this.phi = phi;
		
		color = c;
	}
	

	@Override
	public void paint(Graphics g, HexTile[][] cells) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(color);
		g2.setStroke(stroke);
		if (camMap != null) {
//			camOriginPoint = camMap.getPixelCoordsFromRealCoords(0.0, 0.0);
			Point pStart = camMap.getPixelCoordsFromRealCoords(startX, -startY);
			Point pEnd = camMap.getPixelCoordsFromRealCoords(startX + 100, -startY);
			shape = new Line2D.Double(pStart, pEnd); 				
			AffineTransform af = g2.getTransform();
			AffineTransform newTrans = new AffineTransform();
			newTrans.translate(pStart.x,pStart.y);
//			newTrans.translate(p.x, p.y);
			newTrans.rotate(-phi);
			// newTrans.translate(distance, 0.0);
			newTrans.translate(-pStart.x,-pStart.y);
			// newTrans.translate(p.x, p.y);
			// newTrans.ro

			g2.setTransform(newTrans);
			g2.draw(shape);
			g2.setTransform(af);
//			g2.setColor(Color.BLUE);
//			g2.fill(new Rectangle2D.Double(p.x, p.y, 10, 10));
		}
		
	}

	@Override
	public void setCamMap(CameraPixelMap camMap) {
		this.camMap = camMap;
	}

}
