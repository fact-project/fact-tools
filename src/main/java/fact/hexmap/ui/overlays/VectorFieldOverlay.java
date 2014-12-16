package fact.hexmap.ui.overlays;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;

public class VectorFieldOverlay implements CameraMapOverlay
{
	 private Color fillColor = Color.GRAY;	 
	 
	private final int ARR_SIZE = 4;

	
	// Size must be ? x 4  (posX, poyY, vecX, vecY)
	double [][] arrows = null;
	
	final double scale = 0.5;

	private void drawArrow(Graphics2D g1, int x1, int y1, int x2, int y2) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        
        if(len < 8) return;
        
        
        AffineTransform at = AffineTransform.getTranslateInstance(x1, -y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.drawLine(0, 0, len, 0);
        g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
                      new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
    }
	
	@Override
    public void setColor(Color c) {
        fillColor = c;
    }

	@Override
	public void paint(Graphics2D g2, FactHexMapDisplay map, int slice) 
	{
		g2.setPaint(fillColor);	
		g2.setStroke(new BasicStroke(2));
		
		double radius = map.getTileRadiusInPixels();
	    double scalingX = 0.172*radius;
	    double scalingY = 0.184*radius;
	    
	    for(int i=0; i<arrows.length; i++)
	    {	    	
	    	drawArrow(g2, (int)(arrows[i][0]*scalingX), (int)(arrows[i][1]*scalingY),
	    			(int)(arrows[i][0]*scalingX)-(int)(arrows[i][2]*scalingX*scale), 
	    			(int)(arrows[i][1]*scalingY)+(int)(arrows[i][3]*scalingY*scale));
	    }
	}

	@Override
	public int getDrawRank() {
		return 10;
	}
	
	public void setArrows(double[][] arr){this.arrows = arr;}

}
