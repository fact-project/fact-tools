/**
 * 
 */
package fact.viewer.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author chris
 *
 */
public class FileInputPanel extends JPanel implements InputPanel<File> {

	/** The unique class ID */
	private static final long serialVersionUID = -6946763402759641373L;
	
	File file;
	JTextField filename = new JTextField( 25 );
	JButton choose = new JButton( "File" );
	
	public FileInputPanel(){
		setLayout( new FlowLayout( FlowLayout.LEFT ) );
		
		choose.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				selectFile();
			}
		});
		
		filename.addFocusListener( new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				file = new File( filename.getText() );
			}
		});
		
		add( filename );
		add( choose );
	}
	
	
	
	
	public void selectFile(){
		
		JFileChooser jfc = new JFileChooser();
	
		int ret = jfc.showSaveDialog( this );
		if( ret == JFileChooser.APPROVE_OPTION ){
			file = jfc.getSelectedFile();
			filename.setText( file.getAbsolutePath() );
		}
	}
	
	
	/**
	 * @see fact.viewer.ui.InputPanel#getValue()
	 */
	@Override
	public File getValue() {
		return file;
	}
}