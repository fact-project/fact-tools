/**
 * 
 */
package fact.viewer.exporter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import fact.viewer.ui.HexMap;

/**
 * @author chris
 *
 */
public class ExportPNGAction extends ExportAction {
	
	/** The unique class ID */
	private static final long serialVersionUID = 7823191946043840210L;
	final HexMap hexMap;
	
	public ExportPNGAction( HexMap map ){
		super( "Export as PNG" );
		this.hexMap = map;
	}
	

	public BufferedImage export( HexMap map ) throws Exception {
		BufferedImage buf = new BufferedImage( map.getWidth(), map.getHeight() + 8, BufferedImage.TYPE_INT_RGB );

		Graphics2D g = buf.createGraphics();

		g.setColor( Color.BLACK );
		g.fillRect( 0, 0, buf.getWidth(), buf.getHeight() );
		map.paint( g );

		int y = buf.getHeight() - 10;
		int x = 10;
		g.setColor( Color.WHITE );
		g.setFont( g.getFont().deriveFont( 8.0f ) );
		g.drawString( "2011/11/27, Run 32, Event 321", x, y );

		map.paintVersion( g, buf.getWidth(), buf.getHeight() );

		return buf;
	}
	
	public void exportToFile( HexMap map, File image ) throws Exception {
		ImageIO.write( export( map ), "png", image );
	}


	/**
	 * @see fact.viewer.exporter.ExportAction#export()
	 */
	@Override
	public void export() {

		File out = this.chooseOutputFile( ".png" );
		if( out != null ){
			try {
				exportToFile( hexMap, out );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}