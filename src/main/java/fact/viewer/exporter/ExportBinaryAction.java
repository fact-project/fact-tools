/**
 * 
 */
package fact.viewer.exporter;

import javax.swing.JFileChooser;

import fact.FactViewer;
import fact.io.BinaryFactWriter;

/**
 * @author chris
 * 
 */
public class ExportBinaryAction extends ExportAction {

	/** The unique class ID */
	private static final long serialVersionUID = -3478826948439279406L;

	final FactViewer viewer;

	/**
	 * @param name
	 * @param map
	 */
	public ExportBinaryAction(FactViewer viewer) {
		super("Export to Binary");
		this.viewer = viewer;
	}

	/**
	 * @see javax.swing.AbstractAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return viewer != null && viewer.getEvent() != null;
	}

	/**
	 * @see fact.viewer.exporter.ExportAction#export()
	 */
	@Override
	public void export() {

		try {
			JFileChooser jfc = new JFileChooser();
			int ret = jfc.showSaveDialog(null);
			if (ret == JFileChooser.APPROVE_OPTION) {

				BinaryFactWriter writer = new BinaryFactWriter();
				writer.setFile(jfc.getSelectedFile().getAbsolutePath());
				writer.process(viewer.getEvent());
				writer.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
