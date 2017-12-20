package fact.hexmap.ui.events;

import org.apache.commons.math3.util.Pair;

import java.awt.*;
import java.util.Set;

/**
 * Created by kaibrugge on 02.06.14.
 */
public class OverlaySelectionChangedEvent {
    public final Set<Pair<String, Color>> selectedItems;

    public OverlaySelectionChangedEvent(Set<Pair<String, Color>> kl) {
        this.selectedItems = kl;
    }

}
