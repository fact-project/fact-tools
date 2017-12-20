package fact.hexmap.ui.components.selectors;

import fact.hexmap.ui.Bus;
import fact.hexmap.ui.events.OverlaySelectionChangedEvent;
import fact.hexmap.ui.overlays.CameraMapOverlay;
import stream.Data;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The overlay selector will be shown next to a camerawindow which displays the overlays.
 * Created by kaibrugge on 02.06.14.
 */
public class CameraOverlayKeySelector extends KeySelector {
    @Override
    public void selectionUpdate() {
        Bus.eventBus.post(new OverlaySelectionChangedEvent(getSelectedItemPairs()));
    }

    @Override
    public Set<SeriesKeySelectorItem> filterItems(Data item) {
        Set<SeriesKeySelectorItem> newItems = new HashSet<>();
        for (String key : item.keySet()) {
            try {
                CameraMapOverlay b = (CameraMapOverlay) item.get(key);
                newItems.add(new SeriesKeySelectorItem(key, new Color(186, 217, 246), this));
            } catch (ClassCastException e) {
                continue;
            }
        }
        return newItems;
    }
}
