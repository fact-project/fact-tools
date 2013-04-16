/**
 * 
 */
package fact.viewer.ui;

import java.awt.FlowLayout;
import java.io.File;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 * @author chris
 * 
 */
public class SlicePanel extends JPanel {

	/** The unique class ID */
	private static final long serialVersionUID = -4439222773970111981L;
	final JTextField fileField = new JTextField(30);
	final JTextField eventNumber = new JTextField(4);
	final JButton next = new JButton("Next");
	final JSlider slider = new JSlider();
	final JTextField sliceField = new JTextField(4);
	final JComboBox sources = new JComboBox();

	public SlicePanel() {
		super(new FlowLayout(FlowLayout.LEFT));

		slider.setMinimum(0);
		slider.setMaximum(299);
		slider.setValue(0);
		add(new JLabel("Slice:"));
		add(slider);
		add(sliceField);
	}

	public JComboBox getSourceBox() {
		return sources;
	}

	public void setSource(Collection<String> srcs) {
		sources.removeAllItems();
		for (String str : srcs) {
			sources.addItem(str);
		}
	}

	public String getSource() {
		return sources.getSelectedItem() + "";
	}

	public void setFile(File file) {
		fileField.setText(file.getName());
	}

	public void setEventNumber(Integer id) {
		eventNumber.setText(id.toString());
	}

	public JButton getNextButton() {
		return next;
	}

	public JSlider getSliceSlider() {
		return slider;
	}

	public JTextField getSliceField() {
		return sliceField;
	}
}