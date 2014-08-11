package fact.mapping.ui.components.selectors;

import fact.mapping.ui.Bus;
import fact.mapping.ui.Viewer;
import fact.mapping.ui.events.OverlaySelectionChangedEvent;
import fact.mapping.ui.overlays.CameraMapOverlay;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
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
        for  (String key: item.keySet()){
            try {
                CameraMapOverlay b = (CameraMapOverlay) item.get(key);
                newItems.add(new SeriesKeySelectorItem(key, new Color(186, 217, 246), this));
            } catch (ClassCastException e){
                continue;
            }
        }
        return newItems;
    }
}
