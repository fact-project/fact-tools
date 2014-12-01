package fact.hexmap.ui.overlays;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;

public class MarkerOverlay implements CameraMapOverlay
{
	private Color fillColor = Color.RED;
	private static final float[] markX = { -5, 5, -5, 5 };
	private static final float[] markY = { -5, 5, 5, -5 };

	private double x = 0;
	private double y = 0;

	public MarkerOverlay(double ergX, double ergY)
	{
		this.x = ergX;
		this.y = ergY;
	}

	@Override
	public void setColor(Color c)
	{
		this.fillColor = c;
	}

	@Override
	public void paint(Graphics2D g2, FactHexMapDisplay map, int slice)
	{
		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();
		AffineTransform oldTransform = g2.getTransform();
		
		
		g2.setPaint(fillColor);
		g2.setStroke(new BasicStroke(2));

		double radius = map.getTileRadiusInPixels();
		double scalingX = 0.172 * radius;
		double scalingY = 0.184 * radius;

		g2.drawLine((int) (scalingX * (markX[0] + x)), (int) (scalingY * (markY[0] - y)), (int) (scalingX * (markX[1] + x)),
				(int) (scalingY * (markY[1] - y)));
		g2.drawLine((int) (scalingX * (markX[2] + x)), (int) (scalingY * (markY[2] - y)), (int) (scalingX * (markX[3] + x)),
				(int) (scalingY * (markY[3] - y)));

				
		 g2.setStroke(oldStroke);
	     g2.setPaint(oldPaint);
	     g2.setTransform(oldTransform);
	}

	@Override
	public int getDrawRank()
	{
		return 15;
	}

}
