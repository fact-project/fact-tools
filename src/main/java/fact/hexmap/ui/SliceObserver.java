package fact.hexmap.ui;

import com.google.common.eventbus.Subscribe;
import fact.hexmap.ui.events.SliceChangedEvent;

/**
 * Created by kaibrugge on 29.04.14.
 */
public interface SliceObserver {

    /**
     * In case we display dynamic data we can set which slice/point in time we want to display
     *
     * @param ev the time slice to display. has to be between 0 and numberOfSlices - 1
     */
    @Subscribe
    public void handleSliceChangeEvent(SliceChangedEvent ev);


}
