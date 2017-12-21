package fact.features;

import fact.container.PixelDistribution2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Length implements Processor {
    static Logger log = LoggerFactory.getLogger(Length.class);

    @Parameter(required = true)
    public String distribution;

    @Parameter(required = true)
    public String outputKey;


    @Override
    public Data process(Data input) {
        if (!input.containsKey(distribution)) {
            log.info("No shower in event. Not calculating length");
            return input;
        }
        PixelDistribution2D dist = (PixelDistribution2D) input.get(distribution);

        double length = Math.sqrt(dist.getEigenVarianceX());
        input.put(outputKey, length);

        return input;
    }
}
