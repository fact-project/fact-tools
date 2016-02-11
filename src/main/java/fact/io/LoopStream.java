package fact.io;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;
import stream.io.Stream;
import stream.io.multi.AbstractMultiStream;

/**
 * Loops over a specified amount of events in the inner streams. If the first stream is emptied
 * the next one is read until the specified number of event sis reached.
 * @author chris
 */
public class LoopStream extends AbstractMultiStream {

    static Logger log = LoggerFactory.getLogger(LoopStream.class);

    private int idx = 0;

    @Parameter(description = "How many events you want to loop over." , defaultValue = "100", required = false)
    int events = 100;


    private final ArrayList<Data> items = new ArrayList<>();
    /**
     * @see stream.io.multi.AbstractMultiStream#init()
     */
    @Override
    public void init() throws Exception {
        super.init();

        Map<String, Stream> streams = this.getStreams();
        log.info("Found {} inner streams", streams.size());

        for (Stream stream : streams.values()) {
            stream.init();
            Data item = stream.read();
            while (item != null && items.size() < events) {
                items.add(item);
                item = stream.read();
            }

            if (items.size() >= events) {
                break;
            }
        }

        log.info("Loaded {} events to inner queue.", items.size());
    }

    /**
     * @see stream.io.AbstractStream#readNext()
     */
    @Override
    public Data readNext() throws Exception {
        Data item = items.get(idx % items.size());
        idx = (idx + 1) % items.size();
        return item;
    }


    public void setEvents(int events) {
        this.events = events;
    }

}