/**
 * 
 */
package fact.viewer.exporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;

/**
 * @author chris
 *
 */
public abstract class ExportAction extends AbstractAction implements ActionListener {

	/** The unique class ID */
	private static final long serialVersionUID = -5927185492411621381L;
	final String name;

	public ExportAction( String name ){
		this.name = name;
		this.putValue( Action.NAME, name );
	}
	
	public String getName(){
		return name;
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		export();
	}
	
	
	public abstract void export();
	
	public String toString(){
		return getName();
	}
	
	public File chooseOutputFile( String ext ){
		JFileChooser jfc = new JFileChooser();
		int ret = jfc.showSaveDialog( null );
		if( ret == JFileChooser.APPROVE_OPTION ){
			File file = jfc.getSelectedFile();
			if( file != null ){
				if( ! file.getAbsolutePath().toLowerCase().endsWith( ext ) ){
					return new File( file.getAbsolutePath() + "." + ext );
				}
			}
			return file;
		} 
		return null;
	}
}