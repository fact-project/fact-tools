/**
 * 
 */
package fact.viewer.ui;

import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author chris
 *
 */
public class SliceInputPanel extends JPanel implements InputPanel<Integer> {

	/** The unique class ID */
	private static final long serialVersionUID = -590785834304037620L;

	JTextField text = new JTextField( 4 );
	JSlider slider = new JSlider();
	
	public SliceInputPanel( int min, int max ){
		super( new FlowLayout( FlowLayout.CENTER ) );
		
		add( text );
		
		text.addFocusListener( new FocusListener(){

			@Override
			public void focusGained(FocusEvent arg0) {
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					Integer val = new Integer( text.getText() );
					slider.setValue( val );
				} catch (Exception e) {
					JOptionPane.showMessageDialog( null, "Invalid value for input: " + e.getMessage() );
				}
			}
		});
		
		slider.setValue( min );
		slider.setMinimum( min );
		slider.setMaximum( max );
		
		slider.addChangeListener( new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent arg0) {
				int val = slider.getValue();
				text.setText( "" + val );
			}
		});
		
		add( slider );
	}
	
	
	public Integer getValue(){
		return new Integer( text.getText() );
	}
}
