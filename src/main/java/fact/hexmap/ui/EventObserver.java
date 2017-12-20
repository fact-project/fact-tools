package fact.hexmap.ui;

import org.apache.commons.math3.util.Pair;
import stream.Data;

/**
 * Created by kaibrugge on 29.04.14.
 */
public interface EventObserver {


    /**
     * Pass the event to this observer. The EventObserver has to decide what he wants to display.
     *
     * @param itemKeyPair the current data item we want to display
     */
    public void handleEventChange(Pair<Data, String> itemKeyPair);

}
