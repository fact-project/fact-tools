package fact;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.GsonBuilder;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import stream.runtime.ApplicationContext;
import stream.runtime.DefaultProcess;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * This process is a replacement for the standard process from the streams
 * library which computes statistics about the runtime of each processor it
 * calls.
 * <p>
 * You can use it like this:
 * <process class="fact.PerformanceMeasuringProcess" url=
 * "file:./measured_runtime.json" id="1" input="fact" warmupIterations="33">
 * <p>
 * The output will be written to the json file specified by the url parameter.
 * <p>
 * Created by Alexey and Kai on 27.05.15.
 */
public class PerformanceMeasuringProcess extends DefaultProcess {
    static Logger log = LoggerFactory.getLogger(PerformanceMeasuringProcess.class);

    @Parameter(required = false, description = "Url to the .json file where the performance numbers are stored")
    public SourceURL url = null;

    @Parameter(required = false, description = "Number of warmup iterations to be performed before measuring time")
    public int warmupIterations = 0;

    long iterations = 0;
    long dataItems = 0;

    private HashMap<String, DescriptiveStatistics> timeMap = new HashMap<>();

    @Override
    public void init(ApplicationContext context) throws Exception {
        super.init(context);

        HashMap<String, Integer> numberOfUses = new HashMap<>();
        for (Processor proc : processors) {
            String name = proc.getClass().getSimpleName();
            if (numberOfUses.containsKey(name)) {
                numberOfUses.put(name, numberOfUses.get(name) + 1);
            } else {
                numberOfUses.put(name, 1);
            }
            String key = name + String.valueOf(numberOfUses.get(name));
            timeMap.put(key, new DescriptiveStatistics());
            if (proc instanceof StatefulProcessor) {
                ((StatefulProcessor) proc).init(processContext);
            }
        }
        log.info("Measuring time for {} Processors", processors.size());
    }

    @Override
    public Data process(Data data) {

        log.trace("{}: processing data {}", this, data);
        dataItems++;
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        HashMap<String, Integer> numberOfUses = new HashMap<>();
        for (Processor proc : processors) {
            String name = proc.getClass().getSimpleName();
            if (numberOfUses.containsKey(name)) {
                numberOfUses.put(name, numberOfUses.get(name) + 1);
            } else {
                numberOfUses.put(name, 1);
            }
            stopwatch.start();
            data = proc.process(data);
            if (iterations > warmupIterations) {
                String key = name + String.valueOf(numberOfUses.get(name));
                timeMap.get(key).addValue(stopwatch.elapsed(TimeUnit.MICROSECONDS));
            }
            stopwatch.reset();
            if (data == null) {
                return null;
            }
        }
        iterations++;
        if ((iterations - warmupIterations) % 10 == 0) {
            log.info("Measured {} complete iterations for {} data items", (iterations - warmupIterations), dataItems);
        }
        return data;
    }

    @Override
    public void finish() throws Exception {
        log.debug("Finishing process {} (source: {})...", this, this.getInput());

        Table<String, String, Double> perf = HashBasedTable.create();

        log.info("Runtime of used processors in Microseconds ");

        int processorCounter = 0;
        HashMap<String, Integer> numberOfUses = new HashMap<>();
        for (Processor proc : processors) {
            String name = proc.getClass().getSimpleName();
            if (numberOfUses.containsKey(name)) {
                numberOfUses.put(name, numberOfUses.get(name) + 1);
            } else {
                numberOfUses.put(name, 1);
            }
            String key = name + String.valueOf(numberOfUses.get(name));
            double mean = timeMap.get(key).getMean();
            double std = timeMap.get(key).getStandardDeviation();
            double numberOfCallsToProcessor = timeMap.get(key).getN();
            double lower_sigma_quantile = timeMap.get(key).getPercentile(15.87);
            double upper_sigma_quantile = timeMap.get(key).getPercentile(100 - 15.87);
            double lower_quartil = timeMap.get(key).getPercentile(25);
            double upper_quartil = timeMap.get(key).getPercentile(75);
            perf.put(key, "mean", mean);
            perf.put(key, "standard_deviation", std);
            perf.put(key, "numberOfCallsToProcessor", numberOfCallsToProcessor);
            perf.put(key, "order", Double.valueOf(processorCounter++));
            perf.put(key, "upper_sigma_quantile", upper_sigma_quantile);
            perf.put(key, "lower_sigma_quantile", lower_sigma_quantile);
            perf.put(key, "lower_quartil", lower_quartil);
            perf.put(key, "upper_quartil", upper_quartil);

            log.info("      Runtime of Processor {} lower quantile: {}   Mean: {}   upper_quantile: {}",
                    key, lower_sigma_quantile, mean, upper_sigma_quantile);

            if (proc instanceof StatefulProcessor) {
                try {
                    log.debug("Finishing processor {}", proc);
                    ((StatefulProcessor) proc).finish();
                } catch (Exception e) {
                    log.error("Failed to finish processor '{}': {}", proc, e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // print summary to console as json file
        String json = new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(perf.rowMap());
        if (url == null) {
            log.info(json);
        } else {
            try {
                FileOutputStream out = new FileOutputStream(url.getFile());
                out.write(json.getBytes());
                out.close();
            } catch (FileNotFoundException e) {
                log.error("Could not find file specified by the url " + url.getFile());
                log.info(json);
            }
        }

        super.finish();
    }
}
