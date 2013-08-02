package fact.viewer.ui;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.ui.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;

public class SourceSelector extends JPanel {
	static Logger log = LoggerFactory.getLogger(SourceSelector.class);
	private static final long serialVersionUID = 5040974663073010249L;
	final Map<String, JCheckBox> checkboxes = new LinkedHashMap<String, JCheckBox>();
	private ChartWindow cW;
	private ArrayList<String> selectedKeys = new ArrayList<String>();

	public SourceSelector(Data event, ChartWindow cW) {
		setBackground(Color.WHITE);
		this.cW = cW;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

	}

	public void setEvent(Data event) {

		if (event == null) {
			return;
		}
		for (final String key : event.keySet()) {
			final Serializable value = event.get(key);
			if (value == null) {
				log.info("Key " + key + " is null. Not showing in List.");
				return;
			}

			//check if value is an array of things we can draw on the chartpanel
			if (value.getClass().isArray()
					&& ( 
							value.getClass().getComponentType().equals(float.class)  ||
							value.getClass().getComponentType().equals(IntervalMarker.class)
							)) {
				if (!checkboxes.containsKey(key)) {
					final JCheckBox box = new JCheckBox(key);
					box.setBackground(Color.WHITE);
					box.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent arg0) {
							// cW.getPlotPanel().set;
							if (selectedKeys.contains(key)) {
								selectedKeys.remove(key);
							} else {
								selectedKeys.add(key);
							}
							cW.updateGraph();
						}
					});
					// deselect per default
					box.setSelected(false);
					// makes it visible
					box.setEnabled(true);
					// add new checkbox to th checkboxes list
					checkboxes.put(key, box);
					// add it to the drawn panel. subject to change!
					add(box);
				} 
			}
		}
		// check for keys that dont exist anymore in the current event and
		// disable them. does this ever happen?
		for (String key : checkboxes.keySet()) {
			if (!event.containsKey(key)) {
				checkboxes.remove(key);
				selectedKeys.remove(key);
			}
		}
	}

	public ArrayList<String> getSelectedKeys() {
		return selectedKeys;
	}

}
