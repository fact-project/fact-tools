package fact.hexmap.ui;

import com.google.common.eventbus.EventBus;

/**
 * Singleton for the evetbus. Cause I dont have Guice
 * Created by kaibrugge on 02.06.14.
 */
public class Bus {
    public static EventBus eventBus = new EventBus();

    private Bus() {

    }
}
