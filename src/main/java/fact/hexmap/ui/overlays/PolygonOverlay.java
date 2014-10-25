package fact.hexmap.ui.overlays;

import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

/**
 * Draws an polygon on the cameraview.
 * <p>
 * Draws an preset Polygon on the cameraview window.
 * It is possible to show each slice a different polygon
 * 
 * @author Dominik Baack <dominik.baack@udo.edu>
 */
public class PolygonOverlay implements CameraMapOverlay, Serializable 
{			
	private static final long serialVersionUID = -297478010444386377L;
	
	private final double[][] x; 
	private final double[][] y;
    private Color fillColor = Color.GRAY;    
    
    private int slice = 0;      

    
    /**
     * Set nodes for the polygon. Each Coordinate is in mm and a 2D Array
     * First Dimension are the 300 time slices
     * Second Dimension are the specific Coordinates of the nodes.
     * The Dimension from x and y should be the same!
     * 
     * @param X 2D Array for X-Coordinates, first dimension are the slices, second the Coordinates
     * @param Y 2D Array for Y-Coordinates, first dimension are the slices, second the Coordinates
     */
	public PolygonOverlay(double[][] X, double[][] Y){
    	x = X;
    	y = Y;   	
    }

    @Override
    public void setColor(Color c) {
        fillColor = c;
    }

    @Override
    public void paint(Graphics2D g2, FactHexMapDisplay map) 
    {
        double radius = map.getTileRadiusInPixels();

		Paint oldPaint = g2.getPaint();    
        g2.setPaint(fillColor);
        
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2));
        
        AffineTransform old = g2.getTransform();

        double scalingX = 0.172*radius;
        double scalingY = 0.184*radius;

        Polygon poly = new Polygon();
        for(int i=0; i<x[slice].length; i++)
        {
        	poly.addPoint((int)(x[slice][i]*scalingX), -(int)(y[slice][i]*scalingY));
        }        
        
        g2.translate(0, 0);
        
        g2.draw(poly);

        g2.setStroke(oldStroke);
        g2.setPaint(oldPaint);
        g2.setTransform(old);
    }
    
    public int getSlice() {
		return slice;
	}

	public void setSlice(int slice) {		
		this.slice = slice;
	}
	
	public double[][] getX() {
		return x;
	}

	public double[][] getY() {
		return y;
	}
	
    public int getDrawRank(){return 4;}
    
    
}

