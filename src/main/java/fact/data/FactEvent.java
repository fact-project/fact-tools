/**
 * 
 */
package fact.data;

import fact.viewer.ui.DefaultPixelMapping;

/**
 * <p>
 * This is an implementation of the Data item interface that provides easy
 * access to all pixels of an event by their SoftID.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 */
public interface FactEvent {

        public final static String DATA_KEY = "Data";
        public final static String EVENT_ID_KEY = "EventNum";
        public final static String TRIGGER_NUM_KEY = "TriggerNum";
        public final static String TRIGGER_TYPE_KEY = "TriggerType";

        public final static int NUM_OF_PIXELS = 1440;

        public final static DefaultPixelMapping PIXEL_MAPPING = new DefaultPixelMapping();
        

	
}

