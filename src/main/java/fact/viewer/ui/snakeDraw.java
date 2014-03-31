package fact.viewer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;


public class snakeDraw extends Component
{	
	private static final long serialVersionUID = 123456789L;

	private Polygon poly;
	
	private double scale = 1.0;
	private double offsetX = 0.0;
	private double offsetY = 0.0;
	
	// X, Y in Pixelunits
	public void setShape(double [] x, double[] y)
	{
		if(x.length != y.length || x == null || y == null) 
		{
			poly = new Polygon();
			return;
		}		
		
		poly = new Polygon();
		
		for(int i=0; i<x.length;  i++)
		{
			int cx = (int) (x[i] * scale + offsetX);
			int cy = (int) (y[i] * scale + offsetY);		
			
			poly.addPoint(cx, cy);
		}
	}
	
	public void paint(Graphics g) 
	{
		if(poly == null || poly.npoints <= 0) return;
		
		System.out.println("PaintSnake");
		
		Color tmp = g.getColor();
		
		g.setColor(Color.RED);
		g.drawPolygon(poly);	  
		
		g.setColor(tmp);
	}
	
	
	public Polygon getPoly()
	{
		return this.poly;
	}
	
	public void setRadius(double radius, double offset)
	{
		this.scale = (radius / 2.0) + offset;
	}
	
	public void setOffset(double x, double y)
	{
		System.out.println("Offset " + x + " " + y);
		this.offsetX = x;
		this.offsetY = y;
	}
	
}
