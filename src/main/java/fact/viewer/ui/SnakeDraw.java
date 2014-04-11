package fact.viewer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.Arrays;


public class SnakeDraw extends Component
{	
	private static final long serialVersionUID = 123456789L;

	private Polygon[] poly;
	
	private double scaleX = 1.0;
	private double scaleY = 1.0;
	private double offsetX = 0.0;
	private double offsetY = 0.0;
	
	private int frame = 0;
	
	private double[][] x = null;
	private double[][] y = null;
	
	double radius = 0;
	
	private Color col;
	
	float thickness = 0.5f;
	
	public void setThickness(float f)
	{
		thickness = f;
	}
	
	public void setCol(int r, int g, int b)
	{
		col = new Color(r, g, b);
	}
	
	// [frame][points] in mm
	public void setShape(double [][] x, double[][] y)
	{		
		this.x = x;
		this.y = y;
		
		if(x == null || y == null || x.length != y.length) 
		{			
			poly = new Polygon[0];
			poly[0] = new Polygon();
			return;
		}		
		
		poly = new Polygon[x.length];
		for(int f=0; f < x.length; f++)
		{
			poly[f] = new Polygon();
			for(int i=0; i<x[f].length;  i++)
			{
				int cx = (int) (x[f][i] * scaleX + offsetX);
				int cy = (int) (y[f][i] * scaleY + offsetY);		
			
			
				poly[f].addPoint(cx, cy);
			}
		}
	}
	
	public void update()
	{
		frame = 0;
		poly = null;
	}
	
	public void paint(Graphics g) 
	{
		if(poly == null) return;
		if(frame < 0 || frame >= poly.length ) return;
		if(poly[frame].npoints <= 0) return;
		
		//System.out.println("PaintSnake");
		
		Color tmpC = g.getColor();		
		Stroke tmpS = ((Graphics2D) g).getStroke();
		
		
		g.setColor(col);	
		((Graphics2D) g).setStroke(new BasicStroke(thickness));
		g.drawPolygon(poly[frame]);	  	
		
		
		g.setColor(tmpC);
		((Graphics2D) g).setStroke(tmpS);
	}
	
	
	public Polygon getPoly()
	{
		return this.poly[frame];
	}
	
	public void setRadius(double radius, double offset)
	{
		//System.out.println(radius);
		
		this.radius = radius;
		
		double aphoterm2 = 2* radius * Math.cos(Math.toRadians(180/6));
		
		// 9.5mm Kante - Kante
		//this.scale = 9.5 / radius  + offset;
		//this.scaleX = 11.547 / 9.5;  // = 10 / (cos(30°)*9.5)
		
		this.scaleX = (aphoterm2*Math.cos(Math.toRadians(30)) - 0.5 ) / (Math.cos(Math.toRadians(30))*9.5);	//Irgendwas stimmt noch nicht ganz!  
		this.scaleY = - (aphoterm2) / 9.5;
		//this.scaleY = - 12.0 / 9.5;	 // = 
	}
	
	public void setOffset(double x, double y)
	{
		//System.out.println("Offset " + x + " " + y);
		this.offsetX = x;
		this.offsetY = y;
	}	
	
	public void addFrame()
	{
		if(poly == null) return;
		
		frame = frame + 4;
		if(frame >= poly.length)
		{
			frame = 0;
		}
		//System.out.println(frame);
	}
	
	public void subFrame()
	{
		if(poly == null) return;
		
		frame = frame - 4;
		if(frame < 0)
		{
			frame = poly.length - 1;
		}
		//System.out.println(frame);
	}
	
	public void updateShape()
	{		
		if(poly == null) return;
		
		for(int f=0; f<poly.length; f++)
		{
			for(int i=0; i<poly[f].npoints; i++)
			{
				poly[f].xpoints[i] = (int) (x[f][i] * scaleX + offsetX);				
				poly[f].ypoints[i] = (int) (y[f][i] * scaleY + offsetY);
			}
		}
	}
	
	public void copyPoly(SnakeDraw p)
	{
		if(p==null)
		{
			this.poly = null;
			x = null;
			y = null;
		}
		else
		{
			this.poly = p.poly;		
			x = p.x.clone();
			y = p.y.clone();		
		}
	}
}
