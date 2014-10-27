package fact.utils;

import fact.Constants;
import stream.Data;
import stream.Processor;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.util.ArrayList;

/**
 * Created by jbuss on 24.10.14.
 */
public class PullValueFromDistribution implements Processor{
    @Override
    public Data process(Data data) {
        return null;
    }


    private void loadDistributionFromFile(SourceURL inputUrl) {
        try {
            CsvStream stream = new CsvStream(inputUrl, " ");
            stream.setHeader(false);
            stream.init();

            String line = stream.readLine();
            ArrayList<Double> distribution = new ArrayList<Double>();
            String key = "column:0";


            while (line != null){
                distribution.add(Double.parseDouble(line));
                line = stream.readLine();
            }


        } catch (Exception e) {
            log.error("Failed to load integral Gain data: {}", e.getMessage());
            e.printStackTrace();
        }

    }
}
