/**
 * 
 */
package fact.viewer.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author chris
 *
 */
public abstract class OkCancelDialog extends JDialog {

	/** The unique class ID */
	private static final long serialVersionUID = 2359696258742645927L;
	

	public OkCancelDialog( JFrame parent ){
		super( parent );
		setModal( true );
		
		getContentPane().setLayout( new BorderLayout() );
		
		JPanel buttons = new JPanel( new FlowLayout() );
		JButton ok = new JButton( "Ok" );
		ok.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				ok();
			}
		});
		buttons.add( ok );

		JButton cancel = new JButton( "Cancel" );
		cancel.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				cancel();
			}
		});
		buttons.add( cancel );
		
		getContentPane().add( buttons, BorderLayout.SOUTH );
	}
	
	
	public abstract void cancel();
	
	public abstract void ok();
}
