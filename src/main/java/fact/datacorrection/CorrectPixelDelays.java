package fact.datacorrection;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.CsvStream;
import stream.io.SourceURL;

/**
 * 
 *  This Processor corrects the Pixel-Delays read from a .csv file.
  * @author Maximilian Noethe &lt;maximilian.noethe@tu-dortmund.de&gt;
 * 
 */
public class CorrectPixelDelays implements StatefulProcessor {
    static Logger log = LoggerFactory.getLogger(CorrectPixelDelays.class);
    
    @Parameter(required = false, description = "arrivalTime input", defaultValue="pixels:arrivalTimes")
    private String arrivalTimeKey = "pixels:arrivalTimes";
    
    @Parameter(required = false, description = "The name of the output", defaultValue="pixels:arrivalTimes")
    private String outputKey = "pixels:arrivalTimes";
    
    // TODO: Define reasonable default value
    @Parameter(required = true, description = "The url to the inputfiles for pixel Delays")
    private SourceURL url = null;

    private double[] pixelDelays = null;

    private int npix = Constants.NUMBEROFPIXEL;
    
    @Override
    public Data process(Data item) {

        double[] arrivalTimes = (double[]) item.get(arrivalTimeKey);
        double[] corrArrivalTimes = new double[this.npix];
        for(int pix=0; pix < this.npix; pix++)
        {
            corrArrivalTimes[pix] = arrivalTimes[pix] - pixelDelays[pix];
        }
        
        item.put(outputKey, corrArrivalTimes);
        return item;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception
    {
        if (url != null)
        {
            try
            {
                loadPixelDelayFile(url);
            } catch (Exception e)
            {
                log.error("Could not load pixel delay file specified in the url.");
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    private void loadPixelDelayFile(SourceURL inputUrl) {
        try 
        {
            this.pixelDelays = new double[this.npix];
            CsvStream stream = new CsvStream(inputUrl, " ");
            stream.setHeader(false);
            stream.init();
            
            Data pixelDelaysData = null;

            for (int i = 0; i < this.npix; i++)
            {
                pixelDelaysData = stream.readNext();
                String key = "column:0";
                Double Delay = (Double) pixelDelaysData.get(key);
                this.pixelDelays[i] = Delay;
            }

        } 
        catch (Exception e) 
        {
            log.error("Failed to load pixel delay data: {}", e.getMessage());
            e.printStackTrace();
        }

    }
}
