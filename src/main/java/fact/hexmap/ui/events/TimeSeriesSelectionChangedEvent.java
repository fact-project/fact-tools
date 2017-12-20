package fact.hexmap.ui.events;

import org.apache.commons.math3.util.Pair;

import java.awt.*;
import java.util.Set;

/**
 * Created by kaibrugge on 16.05.14.
 */
public class TimeSeriesSelectionChangedEvent {
    public final Set<Pair<String, Color>> selectedItems;

    public TimeSeriesSelectionChangedEvent(Set<Pair<String, Color>> kl) {
        this.selectedItems = kl;
    }
}
