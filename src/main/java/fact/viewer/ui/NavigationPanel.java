/**
 * 
 */
package fact.viewer.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 * @author chris
 * 
 */
public class NavigationPanel extends JPanel {

	/** The unique class ID */
	private static final long serialVersionUID = -4439222773970111981L;
	JTextField fileField = new JTextField(30);
	JTextField eventNumber = new JTextField(4);
	JButton next = new JButton("Next");
	JButton prev = new JButton("Previous");
	// JButton showTicksButton = new JButton("Show Ticks");
	final JSlider slider = new JSlider();
	JTextField sliceField = new JTextField(4);

	// JComboBox primSources = new JComboBox();
	// JComboBox secSources = new JComboBox();

	public NavigationPanel() {
		super(new FlowLayout(FlowLayout.LEFT));

		add(new JLabel("File:"));
		fileField.setEditable(false);
		add(fileField);
		eventNumber.setEditable(false);
		add(new JLabel("Event: "));
		add(eventNumber);
		eventNumber.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Integer num = new Integer(eventNumber.getText());
					if (num > 0) {

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// add( prev );
		// prev.setEnabled(true);
		add(next);
		// next.addActionListener( new ActionListener(){
		// @Override
		// public void actionPerformed(ActionEvent arg0) {
		// }
		// });
		// add( primSources );
		// add( secSources );

		// add( showTicksButton);
		slider.setMinimum(0);
		slider.setMaximum(299);
		slider.setValue(0);
		// slider.setMajorTickSpacing(10);
		slider.setPreferredSize(new Dimension(300,
				slider.getPreferredSize().height));
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(10);
		add(new JLabel("Slice:"));
		add(slider);
		add(sliceField);
	}
	
	public void setRoi(int roi){
		slider.setMaximum(roi-1);
	}

	// public JComboBox getSourceBox(){
	// return primSources;
	// }
	// public JComboBox getSecSourceBox(){
	// return secSources;
	// }
	//
	//
	// // public void setSource( Collection<String> srcs ){
	// // primSources.removeAllItems();
	// //
	// // for( String str : srcs ){
	// // primSources.addItem( str );
	// //
	// // }
	// // }
	//
	// public String getSource(){
	// return primSources.getSelectedItem() + "";
	// }

	public void setFile(File file) {
		fileField.setText(file.getName());
	}

	public void setEventNumber(Integer id) {
		eventNumber.setText(id.toString());
	}

	public JButton getNextButton() {
		return next;
	}

	public JButton getPrevButton() {
		return prev;
	}

	public JSlider getSliceSlider() {
		return slider;
	}

	public JTextField getSliceField() {
		return sliceField;
	}

	//
	// public JButton getShowTicks() {
	// return showTicksButton;
	// }
	//
	//
	// public void setShowTicks(JButton showTicks) {
	// this.showTicksButton = showTicks;
	// }
}