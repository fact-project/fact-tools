package fact.hexmap.ui.events;

/**
 * Created by kaibrugge on 30.04.14.
 */
public class SliceChangedEvent {
    public int currentSlice = 0;

    public SliceChangedEvent(int currentSlice) {
        this.currentSlice = currentSlice;
    }
}
