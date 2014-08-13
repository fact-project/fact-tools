package fact.hexmap.ui.components.selectors;

import fact.hexmap.ui.Bus;
import fact.hexmap.ui.events.TimeSeriesSelectionChangedEvent;
import stream.Data;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This selector can display all time series (double[]) which are 1440*300 or 1440*1024 in length
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
            try {
                double[] series = (double[]) item.get(key);
                if (series !=  null && (series.length == 1440*300 || series.length == 1440*1024)){
                    SeriesKeySelectorItem newSelectorItem = new SeriesKeySelectorItem(key, Color.GRAY, this);
                    //newSelectorItem.setSelector(this);
                    newItems.add(newSelectorItem);
                }
            } catch (ClassCastException e) {
                continue;
            }
        }
        return newItems;
    }
}
