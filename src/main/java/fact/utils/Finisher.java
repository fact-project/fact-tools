/**
 * 
 */
package fact.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;

/**
 * @author chris
 *
 */
public class Finisher extends AbstractProcessor {

    final static AtomicInteger count = new AtomicInteger(0);
    final static AtomicInteger finished = new AtomicInteger(0);

    static Map<String, Long> IDs = new HashMap<String, Long>();
    static Map<String, Long> groupCounts = new HashMap<String, Long>();

    static Logger log = LoggerFactory.getLogger(Finisher.class);

    @Parameter
    String id = null;

    @Parameter
    String group = "_undefined_";

    Long events = 0L;

    /**
     * @see stream.AbstractProcessor#init(stream.ProcessContext)
     */
    @Override
    public void init(ProcessContext ctx) throws Exception {
        super.init(ctx);
        int localId = count.incrementAndGet();

        if (id == null) {
            id = ctx.getId() + "::Finisher[" + localId + "]";
        }

        synchronized (IDs) {
            IDs.put(id, 0L);
        }
    }

    /**
     * @see stream.Processor#process(stream.Data)
     */
    @Override
    public Data process(Data item) {
        events++;
        return item;
    }

    /**
     * @see stream.AbstractProcessor#finish()
     */
    @Override
    public void finish() throws Exception {
        super.finish();
        log.info("Finisher {} is closing, {} events processed.", id, events);

        synchronized (IDs) {
            IDs.put(id, events);
        }

        int done = finished.incrementAndGet();
        int total = count.get();
        log.info("{} finisher called, {} exist in total.", done, total);

        synchronized (groupCounts) {
            Long cnt = groupCounts.get(group);
            if (cnt == null) {
                cnt = 0L;
            }
            cnt += events;
            groupCounts.put(group, cnt);
        }

        if (done == total) {
            Long sum = 0L;
            for (String id : IDs.keySet()) {
                sum += IDs.get(id);
            }
            log.info("{} events processed in total.", sum);
            for (String group : groupCounts.keySet()) {
                log.info("Count for '{}' is '{}'", group, groupCounts.get(group));
            }
        }
    }

}
