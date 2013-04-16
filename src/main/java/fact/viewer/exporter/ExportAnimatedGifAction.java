/**
 * 
 */
package fact.viewer.exporter;

import java.io.File;
import java.util.Date;

import fact.FactViewer;
import fact.io.CreateAnimatedGif;

/**
 * @author chris
 *
 */
public class ExportAnimatedGifAction extends ExportAction {

	/** The unique class ID  */
	private static final long serialVersionUID = -8981526415102970940L;
	
	final FactViewer viewer;
	
	
	public ExportAnimatedGifAction( FactViewer viewer ){
		super( "Export as animated GIF" );
		this.viewer = viewer;
	}
	
	
	/**
	 * @see fact.viewer.exporter.ExportAction#export()
	 */
	@Override
	public void export() {
		
		File out = this.chooseOutputFile( ".gif" );
		if( out != null ){

			try {
				CreateAnimatedGif.createAnimatedGif( out, new Date(), 0, viewer.getEvent(), 0, 300, 2 );
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
}