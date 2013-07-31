/**
 * 
 */
package fact.viewer.exporter;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import fact.FactViewer;
import fact.utils.CutSlices;
import fact.viewer.ui.FileInputPanel;
import fact.viewer.ui.OkCancelDialog;
import fact.viewer.ui.SliceInputPanel;

/**
 * @author chris
 * 
 */
public class ExportDialog extends OkCancelDialog {

	/** The unique class ID */
	private static final long serialVersionUID = 8428633855415228094L;
	JComboBox selectFormatBox;
	String[] formats = new String[] { "PNG Image", "animated GIF",
			"Binary Data", "CSV Data" };

	SliceInputPanel startSlicePanel = new SliceInputPanel(0, 300);
	SliceInputPanel endSlicePanel = new SliceInputPanel(0, 300);
	FileInputPanel filePanel = new FileInputPanel();

	public ExportDialog(JFrame parent) {
		super(parent);

		selectFormatBox = new JComboBox(formats);

		JPanel grid = new JPanel(new GridLayout(4, 2));

		grid.add(new JLabel("Export as:"));
		grid.add(selectFormatBox);

		grid.add(new JLabel("Output File:"));
		grid.add(filePanel);

		grid.add(new JLabel("Start slice:"));
		grid.add(startSlicePanel);

		grid.add(new JLabel("End slice:"));
		grid.add(endSlicePanel);

		grid.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(grid, BorderLayout.CENTER);
		this.pack();
	}

	public void cancel() {
		setVisible(false);
	}

	public void ok() {
		setVisible(false);

		FactViewer viewer = FactViewer.getInstance();
		// TODO: handle all keys
		String source = viewer.getCurrentKey();
		CutSlices slices = new CutSlices();

		slices.setKeys(new String[] { source });
		slices.setStart(startSlicePanel.getValue());
		slices.setEnd(endSlicePanel.getValue());

		// Data out = slices.process( viewer.getEvent() );

		int idx = selectFormatBox.getSelectedIndex();
		switch (idx) {

		case 0:
			break;

		case 1:
			break;

		case 2:
			break;

		default:
			break;

		}
	}

	public static void main(String[] args) {
		ExportDialog d = new ExportDialog(null);
		d.setVisible(true);
	}
}