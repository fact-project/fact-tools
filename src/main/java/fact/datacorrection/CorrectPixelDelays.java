package fact.datacorrection;

import fact.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.CsvStream;
import stream.io.SourceURL;

/**
 * This Processor corrects the Pixel-Delays read from a .csv file.
 *
 * @author Maximilian Noethe &lt;maximilian.noethe@tu-dortmund.de&gt;
 */
public class CorrectPixelDelays implements StatefulProcessor {
    static Logger log = LoggerFactory.getLogger(CorrectPixelDelays.class);

    @Parameter(required = true, description = "arrivalTime input")
    public String arrivalTimeKey;

    @Parameter(required = true, description = "The name of the output")
    public String outputKey;

    @Parameter(required=true, description = "The url to the inputfiles for pixel Delays")
    public SourceURL url = null;

    private double[] pixelDelays = null;

    private int npix = Constants.N_PIXELS;

    @Override
    public Data process(Data item) {

        double[] arrivalTime = (double[]) item.get(arrivalTimeKey);
        double[] corrArrivalTime = new double[npix];

        for (int pix = 0; pix < npix; pix++) {
            corrArrivalTime[pix] = arrivalTime[pix] - pixelDelays[pix];
        }

        item.put(outputKey, corrArrivalTime);
        return item;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
            loadPixelDelayFile(url);
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    private void loadPixelDelayFile(SourceURL inputUrl) {
        try {
            this.pixelDelays = new double[npix];
            CsvStream stream = new CsvStream(inputUrl, " ");
            stream.setHeader(false);
            stream.init();

            for (int i = 0; i < npix; i++) {
                Data pixelDelayData = stream.readNext();
                String key = "column:0";
                Double Delay = (Double) pixelDelayData.get(key);
                this.pixelDelays[i] = Delay;
            }

        } catch (Exception e) {
            log.error("Failed to load pixel delay data: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

}
