package fact;

import com.google.common.base.Stopwatch;
import fact.rta.WebSocketService;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

/**
 * DataRate counts number of items processed per second. Additionally it can log memory (free, total, max).
 *
 * @author kai
 */
public class DataRate implements StatefulProcessor {
    private Logger log = LoggerFactory.getLogger(DataRate.class);

    private long totalItems = 0;
    private long itemCounter = 0;


    private Runtime runtime;


    @Parameter(required = false, description = "How many data items are collected for each " +
            "measurement of the data rate")
    long every = 200;

    @Parameter(required = false,
            description = "Flags whether to print stuff to console during processing or not.")
    boolean silent = false;

    @Parameter(required = false, description = "Flags whether to log memory usage to data item")
    boolean logmemory = false;

    @Parameter(required = false, description = "The key under which you'll find the datarate " +
            "in the item after calculating it.", defaultValue = "@datarate")
    String output = "@datarate";

    @Parameter(required = true, description = "If true this will update the datarate for the webservice to display")
    private boolean rtamode = false;


    SummaryStatistics statistics = new SummaryStatistics();
    private Stopwatch stopwatch;


    @Override
    public void init(ProcessContext context) throws Exception {
        stopwatch = Stopwatch.createUnstarted();
        runtime = Runtime.getRuntime();
    }

    @Override
    public void resetState() throws Exception {
    }

    @Override
    public void finish() throws Exception {
        log.info("Mean data rate: " + statistics.getMean() + " +- "
                + statistics.getStandardDeviation() / statistics.getN());
    }

    @Override
    public Data process(Data input) {
        if (totalItems == 0) {
            stopwatch.start();
        }
        if (itemCounter == every) {
            double dataRatePerSecond = 1000 *
                    ((double) itemCounter) / (double) stopwatch.elapsed(TimeUnit.MILLISECONDS);
            statistics.addValue(dataRatePerSecond);
            if (!silent) {
                log.info("Current Data rate " + output + "  per second: "
                        + dataRatePerSecond + "  free memory " + runtime.freeMemory());
            }
            if (logmemory) {
                input.put("@freememory", runtime.freeMemory());
                input.put("@totalmemory", runtime.totalMemory());
                input.put("@maxmemory", runtime.maxMemory());
            }
            input.put("@thread", Thread.currentThread().getName());
            input.put(output, dataRatePerSecond);

            if(rtamode){
                WebSocketService.getService().updateDataRate(OffsetDateTime.now(), dataRatePerSecond);
            }

            stopwatch.reset().start();

            itemCounter = 0;
        }

        itemCounter++;
        totalItems++;
        return input;
    }

}
