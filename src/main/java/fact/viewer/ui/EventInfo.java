package fact.viewer.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import stream.Data;
import fact.Constants;

/**
 * this creates the String for the EventInfoWindow
 * 
 * @author bruegge
 * 
 */
public class EventInfo extends JEditorPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Data event;
	private HTMLEditorKit kit;
	private StyleSheet styleSheet;
	private StringBuilder strB;
	private ArrayList<String> pixelInfos = new ArrayList<String>();
	private Set<Integer> selectedSet = null;
	private ArrayList<String> hillasParameterKeys = new ArrayList<String>();
	private ArrayList<String> eventInfos = new ArrayList<String>();

	public void setEvent(Data event) {
		pixelInfos.clear();
		eventInfos.clear();
		hillasParameterKeys.clear();
		this.event = event;
		for (String key : event.keySet()) {
			Serializable val = event.get(key);
			// log.debug("Key {}.getClass() = {}", key, val.getClass());
			// log.debug("Key {}.isArray() = {}", key, val.getClass()
			// .isArray());
			// System.out.println(key);

			if (val != null && val.getClass().isArray()) {
				// log.info("  ComponentType of {}[]: {}", key, val.getClass()
				// .getComponentType());
				if (val.getClass().getComponentType() == float.class) {
					pixelInfos.add(key);
				}
				if (val.getClass().getComponentType() == double.class) {
					pixelInfos.add(key);
				}
			} else if (val != null && !key.startsWith("@")) {
				eventInfos.add(key);
			}
		}
		for (String str : event.keySet()) {
			if (str.startsWith("Hillas") || str.startsWith("Ellipse")) {
				hillasParameterKeys.add(str);
			}
		}

		this.setText(buildString());

	}

	private int softId = 0;

	public int getSoftId() {
		return softId;
	}

	public EventInfo() {

		// this.event = event;
		kit = new HTMLEditorKit();
		this.setEditorKit(kit);

		styleSheet = kit.getStyleSheet();
		styleSheet
				.addRule("body {color:#000; font-family:arial; margin: 4px; }");
		styleSheet.addRule("h1 {color: #56E81C;}");
		styleSheet.addRule("h2 {color: #eeeeee;}");
		styleSheet
				.addRule("pre {font : 10px arial-black; background-color : #fafafa; }");
		this.setText("<h1> Event Infos: </h1>");

	}

	private String buildString() {
		strB = new StringBuilder();
		// strB.append(b)
		strB.append("<h1> Event Infos: </h1>");
		strB.append("<br />");
		strB.append("Event " + "<strong>" + event.get(Constants.KEY_EVENT_NUM)
				+ "</strong>" + " contains the following keys:");
		strB.append("<br />");
		strB.append(pixelInfos);
		strB.append(".");
		strB.append("<br />");
		strB.append(Constants.KEY_EVENT_NUM + ": "
				+ event.get(Constants.KEY_EVENT_NUM));
		strB.append("<br />");
		strB.append(Constants.KEY_TRIGGER_TYPE + ": "
				+ event.get(Constants.KEY_TRIGGER_TYPE));
		strB.append("<br />");
		if (event.containsKey(Constants.SOURCE_POS_X)) {
			strB.append("<br />");
			strB.append("<h2>Source Position:  </h2>");
			strB.append(event.get(Constants.SOURCE_POS_X) + ", "
					+ event.get(Constants.SOURCE_POS_Y));
			strB.append("<br />");
		}

		strB.append("<br />");
		for (String str : eventInfos) {
			strB.append(str + ":  " + event.get(str));
			strB.append("<br />");
		}
		strB.append("<br />");
		for (String str : hillasParameterKeys) {
			strB.append(str + ":  " + event.get(str));
			strB.append("<br />");
		}

		if (pixelInfos != null && selectedSet != null) {
			strB.append("<h2> Selected Pixel Infos: </h2>");
			strB.append("<br />");

			for (Integer o : selectedSet) {
				strB.append("<strong> SoftID: </strong> " + o);
				strB.append("<br />");
				float[] pixels;
				for (String st : pixelInfos) {
					pixels = (float[]) event.get(st);
					if (pixels.length == Constants.NUMBEROFPIXEL) {
						strB.append(st + ": "
								+ pixels[DefaultPixelMapping.getChidID(o)]);
						strB.append("<br />");
					}
				}
				strB.append("<br />");
			}
		}

		return strB.toString();
	}

	public void setSoftIds(Set<Integer> selectedItems) {
		if (event != null) {
			this.selectedSet = selectedItems;
			this.setText(buildString());
		}

	}

}
