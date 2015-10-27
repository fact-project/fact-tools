package fact.utils;

import fact.Constants;
import fact.Utils;
import fact.container.Histogram1D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

/**
 * Created by jebuss on 23.09.15.
 */
public class FillPixelHistogramm implements StatefulProcessor {
    static Logger log = LoggerFactory.getLogger(FillPixelHistogramm.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey;
    @Parameter(required = false)
    private String numEventsKey = "numEvents"+outputKey;
    @Parameter(required = false)
    private double binWidth = 20;
    @Parameter(required = false)

    private int  nBins = 2;
    private double min = 0;
    private double max = 100;
    private int npix = Constants.NUMBEROFPIXEL;
    private Histogram1D[] histograms;

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");


        double[][] data = (double[][]) input.get(key);

        double[] numEvents = new double[npix];

        for (int pix = 0; pix < npix; pix++){
            histograms[pix].addSeries(data[pix]);
            numEvents[pix] = histograms[pix].getnEvents();
        }

        input.put(outputKey, histograms);
        input.put(numEventsKey, numEvents);

        return input;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        histograms = new Histogram1D[npix];
        for(int i = 0; i < npix; i++){
//            Histogram1D hist = new Histogram1D(this.min, this.max, this.nBins);
            Histogram1D hist = new Histogram1D(this.min, this.binWidth);
            histograms[i] = hist;
        }
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setNumEventsKey(String numEventsKey) {
        this.numEventsKey = numEventsKey;
    }

    public void setnBins(int nBins) {
        this.nBins = nBins;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setBinWidth(double binWidth) {
        this.binWidth = binWidth;
    }
}
