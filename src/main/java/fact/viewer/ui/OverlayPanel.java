/**
 * 
 */
package fact.viewer.ui;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import stream.Data;
import fact.FactViewer;
import fact.image.overlays.Overlay;

/**
 * @author chris
 * 
 */
public class OverlayPanel extends JPanel {

	/** The unique class ID */
	private static final long serialVersionUID = 5014565792714703438L;

	// final Map<String, Overlay> over = new LinkedHashMap<String, Overlay>();
	// final Map<String, Boolean> overlays = new LinkedHashMap<String,
	// Boolean>();
	final Map<String, JCheckBox> checkboxes = new LinkedHashMap<String, JCheckBox>();

	// this list can be the same thats being hold in the mainUI
	protected ArrayList<CamWindow> camList = null;
	protected CameraPixelMap mainCamView = null;

	private final FactViewer ui;

	public OverlayPanel(FactViewer ui) {
		super(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel("Overlays: "));
		this.ui = ui;
	}

	public void set(Data item) {
		// over.clear();
		for (final String key : item.keySet()) {
			Serializable value = item.get(key);
			if (value instanceof Overlay) {
				if (!checkboxes.containsKey(key)) {
					final JCheckBox box = new JCheckBox(key);

					box.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent arg0) {
							ui.getCamMap().setActiveOverlayKeys(
									getSelectedKeys());
							for (CamWindow cM : ui.getCamWindowList()) {
								cM.setActiveOverlayKeys(getSelectedKeys());
							}
						}
					});
					// box.addChangeListener(new ChangeListener() {
					// @Override
					// public void stateChanged(ChangeEvent arg0) {
					// ui.getCamMap().repaint();
					// for(CamWindow cM : ui.getCamWindowList()){
					// cM.getCamMap().repaint();
					// }
					// }
					// });
					// overlays.put(key, new Boolean(false));

					// deselect per default
					box.setSelected(false);
					// makes it visible
					box.setEnabled(true);
					// add new checkbox to th checkboxes list
					checkboxes.put(key, box);
					// add it to the drawn panel. subject to change!
					add(box);
					// nned to refresh the viewpot?
					// repaint();
				} else {
					// checkbox already there

				}
				// over.put(key, (Overlay) value);
				// checkboxes.get(key).setEnabled(true);
			}
		}
		// check for keys that dont exist anymore in the current event and
		// disable them. does this ever happen?
		// for (String key : checkboxes.keySet()) {
		// if (!item.containsKey(key)) {
		// checkboxes.get(key).setEnabled(false);
		// }
		// }
	}

	// TODO: maybe this can be done faster
	public Set<String> getSelectedKeys() {
		Set<String> keys = new LinkedHashSet<String>();
		for (String key : checkboxes.keySet()) {
			JCheckBox box = checkboxes.get(key);
			if (box.isEnabled()) {
				if (box.isSelected())
					keys.add(key);
			}
		}
		return keys;
	}

	/**
	 * @see fact.viewer.ui.Overlay#paint(java.awt.Graphics,
	 *      fact.viewer.ui.HexTile[][])
	 */
	// @Override
	// public void paint(Graphics g, HexTile[][] cells) {
	// for (String key : getSelectedKeys()) {
	// Overlay over = this.over.get(key);
	// if (over != null) {
	// over.paint(g, cells);
	// }
	// }
	// }
}