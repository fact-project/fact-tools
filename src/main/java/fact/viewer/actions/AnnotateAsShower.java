/**
 * 
 */
package fact.viewer.actions;

import java.awt.event.ActionEvent;
import java.util.Set;

import stream.Data;
import fact.FactViewer;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;
import fact.viewer.ui.CameraPixelMap;
import fact.viewer.ui.OverlayPanel;
import fact.viewer.ui.SelectAction;

/**
 * @author chris
 * 
 */
public class AnnotateAsShower extends SelectAction {

	/** The unique class ID */
	private static final long serialVersionUID = -3142622216180413219L;

	public AnnotateAsShower() {
		super("Mark as shower");
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		FactViewer viewer = FactViewer.getInstance();
		CameraPixelMap map = viewer.getCamMap();
		Set<Integer> ids = map.getSelectedIds();
		PixelSet shower = new PixelSet();
		for (Integer id : ids) {
			shower.add(new Pixel(id));
		}
		OverlayPanel overlays = viewer.getOverlayPanel();
		Data event = viewer.getEvent();
		event.put("@shower", shower);
		overlays.set(event);
		overlays.revalidate();
	}
}
