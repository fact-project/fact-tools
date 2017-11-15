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
 * 
 *  This Processor corrects the Pixel-Delays read from a .csv file.
  * @author Maximilian Noethe &lt;maximilian.noethe@tu-dortmund.de&gt;
 * 
 */
public class CorrectPixelDelays implements StatefulProcessor {
    static Logger log = LoggerFactory.getLogger(CorrectPixelDelays.class);
    
    @Parameter(required = true, description = "arrivalTime input")
    private String arrivalTimeKey;
    
    @Parameter(required = true, description = "The name of the output")
    private String outputKey;
    
    @Parameter(description = "The url to the inputfiles for pixel Delays")
    private SourceURL url = null;

    Data pixelDelayData = null;
    private double[] pixelDelay = null;

    private int npix = Constants.NUMBEROFPIXEL;
    
    @Override
    public Data process(Data item) {

        double[] arrivalTime = (double[]) item.get(arrivalTimeKey);
        double[] corrArrivalTime = new double[this.npix];
        for(int pix=0; pix < this.npix; pix++)
        {
            corrArrivalTime[pix] = arrivalTime[pix] - pixelDelay[pix];
        }
        
        item.put(outputKey, corrArrivalTime);
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
                log.error("Could not load .drs file specified in the url.");
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
            this.pixelDelay = new double[this.npix];
            CsvStream stream = new CsvStream(inputUrl, " ");
            stream.setHeader(false);
            stream.init();

            for (int i = 0; i < this.npix; i++)
            {
                pixelDelayData = stream.readNext();
                String key = "column:0";
                Double Delay = (Double) pixelDelayData.get(key);
                this.pixelDelay[i] = Delay;
            }

        } 
        catch (Exception e) 
        {
            log.error("Failed to load pixel delay data: {}", e.getMessage());
            e.printStackTrace();
        }

    }
    

    /*
     * Getter and Setter
     */
    
    public String getPhotonChargeKey() {
        return arrivalTimeKey;
    }

    public void setArrivalTimeKey(String arrivalTimeKey) {
        this.arrivalTimeKey = arrivalTimeKey;
    }
    
    
    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
    
    public SourceURL getUrl() {
        return url;
    }

    public void setUrl(SourceURL url) {
        this.url = url;
    }

}
