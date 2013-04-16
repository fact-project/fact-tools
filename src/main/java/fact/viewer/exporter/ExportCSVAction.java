/**
 * 
 */
package fact.viewer.exporter;

import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.FactViewer;
import fact.io.CSVFactWriter;

/**
 * @author chris
 * 
 */
public class ExportCSVAction extends ExportAction {

	/** The unique class ID */
	private static final long serialVersionUID = -3478826948439279406L;

	static Logger log = LoggerFactory.getLogger(ExportCSVAction.class);

	final FactViewer viewer;

	/**
	 * @param name
	 * @param map
	 */
	public ExportCSVAction(FactViewer viewer) {
		super("Export to CSV");
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

				CSVFactWriter writer = new CSVFactWriter();

				//TODO: export all interesting keys? or just some selected ones?
				String key = viewer.getCurrentKey();
				log.info("Exporting source '{}'", key);

				writer.setKey(key);
				writer.setUrl(jfc.getSelectedFile().toURI().toURL().toString());
				writer.process(viewer.getEvent());
				writer.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
