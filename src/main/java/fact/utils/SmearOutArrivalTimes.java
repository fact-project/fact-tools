package fact.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by jbuss on 28.10.14.
 */
public class SmearOutArrivalTimes implements Processor{
    static Logger log = LoggerFactory.getLogger(SmearOutArrivalTimes.class);

    @Parameter(required = true)
    private String key = null;

    @Parameter(required = true)
    private String outputKey = null;


    @Override
    public Data process(Data input) {
        return input;
    }
}
