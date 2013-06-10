/**
 * 
 */
package fact.viewer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.viewer.colorMappings.ColorMapping;
import fact.viewer.colorMappings.DefaultColorMapping;

/**
 * @author chris
 *
 */
public class ColorTest 
	extends JPanel
{
	/** The unique class ID */
	private static final long serialVersionUID = -3349061849657286986L;
	static Logger log = LoggerFactory.getLogger( ColorTest.class );
	ColorMapping map;

	
	public void test(){
		TestFrame frame = new TestFrame( new DefaultColorMapping() );
		frame.setSize( 1024, 768 );
		frame.setVisible( true );
	}
	
	
	
	public ColorTest(){
		map = new DefaultColorMapping();
	}
	
	public void setColorMapping( ColorMapping cm ){
		map = cm;
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
		
		Integer height = this.getHeight();
		int w = getWidth();
		//Double stepSize = 1.0d / height.doubleValue();
		
		for( int i = 0; i < this.getHeight(); i++ ){
			Double y = (height.doubleValue() - i) / height.doubleValue();
			Color c = map.map( y.floatValue(), 0, 600 );
			log.info( "Mapping {} to {}", y, c );
			y = y * height.doubleValue();
			g.setColor( c );
			g.drawLine( 0, y.intValue(), w, y.intValue() );
		}
		
	}

	public static class TestFrame extends JFrame implements MouseMotionListener {

		/** The unique class ID */
		private static final long serialVersionUID = -8822026709490797747L;
		final JLabel pos = new JLabel( "Position: , Value: ");
		ColorTest test;
		
		public TestFrame( ColorMapping cm ){
			setLayout( new BorderLayout() );
			JPanel p = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			p.add( pos );
			add( p, BorderLayout.NORTH );
			test = new ColorTest();
			test.setColorMapping( cm );
			test.addMouseMotionListener( this );
			add( test, BorderLayout.CENTER );
		}
		
		
		@Override
		public void mouseDragged(MouseEvent arg0) {
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			pos.setText( "Position: (" + arg0.getX() + "," + arg0.getY() + "), Value: " + test.getValue( arg0.getX(), arg0.getY() ) );
		}
	}
}