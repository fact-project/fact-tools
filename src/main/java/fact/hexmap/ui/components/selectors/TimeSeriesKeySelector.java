package fact.hexmap.ui.components.selectors;

import fact.Constants;
import fact.Utils;
import fact.hexmap.ui.Bus;
import fact.hexmap.ui.events.TimeSeriesSelectionChangedEvent;
import stream.Data;

import java.awt.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This selector can display all arrays which are 1440xB with B &gt; 1 length. The Selector will be displayed
 * in the plotting window.
 * Created by kaibrugge on 02.06.14.
 */
public class TimeSeriesKeySelector extends KeySelector {

    @Override
    public void selectionUpdate() {

        Bus.eventBus.post(new TimeSeriesSelectionChangedEvent(getSelectedItemPairs()));

    }

    @Override
    public Set<SeriesKeySelectorItem> filterItems(Data item) {
        Set<SeriesKeySelectorItem> newItems = new HashSet<>();
        for (String key : item.keySet()) {

            Serializable value = item.get(key);
            if (value == null) {
                continue;
            }
            double[] series = Utils.toDoubleArray(value);
            if (series != null && (series.length > Constants.N_PIXELS) && (series.length % Constants.N_PIXELS == 0)) {
                SeriesKeySelectorItem newSelectorItem = new SeriesKeySelectorItem(key, Color.GRAY, this);
                newItems.add(newSelectorItem);
            }
        }
        return newItems;
    }
}
