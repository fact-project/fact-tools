/**
 * 
 */
package fact.viewer.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.viewer.colorMappings.ColorMapping;

/**
 * @author chris
 *
 */
public class ScalePanel extends JPanel {

	/** The unique class ID */
	private static final long serialVersionUID = -84543261667712932L;
	static Logger log = LoggerFactory.getLogger( ScalePanel.class );
	
	Double min = 0.0d;
	Double max = 0.0d;
	ColorMapping map;
	DecimalFormat df = new DecimalFormat( "0.00" );
	
	public ScalePanel( ColorMapping cm ){
		setBorder( null );
		map = cm;
	}
	
	
	public int getWidth(){
		return 70;
	}
	
	
	public Dimension getMinimumSize(){
		return new Dimension( 70, super.getMinimumSize().height );
	}

	public Dimension getPreferredSize(){
		return getMinimumSize();
	}
	
	public Double getValue( Integer xpos, Integer ypos ){
		Integer height = getHeight();
		Double val = (height.doubleValue() - ypos.doubleValue() ) / height.doubleValue();
		return val;
	}
	
	
	/**
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		g.setColor( getBackground() );
		g.fillRect( 0, 0, getWidth(), getHeight() );
		
		Integer barHeight = this.getHeight();
		
		int w = getWidth();
		//Double stepSize = 1.0d / height.doubleValue();
		g.setFont( g.getFont().deriveFont( 10.0f ) );
		
		for( int i = 0; i < barHeight; i++ ){
			float range = map.getMaxValue() - map.getMinValue();
			float value = map.getMinValue() + (range)*(((float)i)/((float)barHeight-1));
			Color c = map.map( value );
			g.setColor( c );
			g.drawLine( 40, barHeight - i, w, barHeight - i );
			if(i % 20 == 0 || i == barHeight-1){
				g.setColor( Color.WHITE );
				g.drawString( df.format(value), 1, barHeight - i );	
			}
		}
	}


	public void setColorMapping(ColorMapping c) {
		this.map = c;
		this.repaint();
		
	}
}
