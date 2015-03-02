package fact.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.util.ArrayList;
import java.util.Random;

/** load a set of data from a file and sample a random value from this set.
 * Created by jbuss on 24.10.14.
 */
public class PullValueFromDistribution implements Processor{
    static Logger log = LoggerFactory.getLogger(PullValueFromDistribution.class);

//    @Parameter(required = true)
//    private String key = null;
//
    @Parameter(required = true)
    private String outputKey = null;

    @Parameter(required = true, description = "The url to the inputfile for the distibution values")
    private SourceURL url = null;

    private ArrayList<Double> distribution = null;

    @Override
    public Data process(Data input) {

        Random rand = new Random();
        Double rndValue = distribution.get( rand.nextInt( distribution.size() ) ) ;
        input.put(outputKey, rndValue);
        return input;
    }


    public void setUrl(SourceURL url) {
        try {
            loadDistributionFromFile(url);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        this.url = url;
    }

    public SourceURL getUrl() {
        return url;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    private void loadDistributionFromFile(SourceURL inputUrl) {
        try {
            CsvStream stream = new CsvStream(inputUrl, " ");
            stream.setHeader(false);
            stream.init();

            Data line = stream.readNext();
            this.distribution = new ArrayList<Double>();
            String key = "column:0";

            while (line != null){
                Double value = (Double) line.get(key);
                this.distribution.add(value);
                line = stream.readNext();
            }

        } catch (Exception e) {
            log.error("Failed to load integral Gain data: {}", e.getMessage());
            e.printStackTrace();
        }

    }
}
