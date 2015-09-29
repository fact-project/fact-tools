package fact.container;

import fact.Constants;
import fact.Utils;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jebuss on 25.09.15.
 */
public class PixelHistogram1D implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(PixelHistogram1D.class);

    private String key;
    private String histogramsKey;
    private String datasetKey;

    private double binWidth = .1;
    private SimpleHistogramDataset[] datasets;
    private int nPix = Constants.NUMBEROFPIXEL;


    @Override
    public void init(ProcessContext processContext) throws Exception {
        datasets = new SimpleHistogramDataset[nPix];

        for (Integer i = 0; i < nPix; i++){
            datasets[i] = new SimpleHistogramDataset(key + "_Pix" + i.toString() );
        }


    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, key);

        double[][] data = (double[][]) input.get(key);

        for (int pix = 0; pix < nPix; pix++){
            for (double val : data[pix]){
                try{
                    datasets[pix].addObservation(val);
                } catch (RuntimeException e ) {
                    SimpleHistogramBin bin = new SimpleHistogramBin(
                            Math.floor(val/binWidth)*binWidth,
                            Math.floor(val/binWidth)*binWidth + binWidth - 1e-15,
                            true,
                            false);
                    //The 1e-15 in the upper limit avoids rounding errors which lead to overlapping bins and therefore missing histogram entries
                    try{
                        datasets[pix].addBin(bin);
                        datasets[pix].addObservation(val);

                    } catch (Exception ee){
                        log.warn("Overlapping bin");
                    }
                }
            }
        }



        input.put(datasetKey, datasets);
        input.put(histogramsKey, generatePixelHistogramArrays());
        return input;
    }
    public int calculateNumBinsTotal(){
        int maxNumBins = 0;

        for( int pix = 0; pix < nPix; pix++){
            int nBins = datasets[pix].getItemCount(0);
            if (nBins > maxNumBins){
                maxNumBins = nBins;
            }
        }
        return maxNumBins;
    }

    public double[][][] generatePixelHistogramArrays(){
        double[][][] pixelHistogramArrays = new double[nPix][2][];

        for( int pix = 0; pix < nPix; pix++){
            int nBins = datasets[pix].getItemCount(0);
            double center[] = new double[nBins];
            double counts[] = new double[nBins];

            for (int bin = 0; bin< nBins; bin++){
                center[bin] = datasets[pix].getXValue(0, bin);
                counts[bin] = datasets[pix].getEndYValue(0, bin);
            }
            pixelHistogramArrays[pix][0] = center;
            pixelHistogramArrays[pix][1] = counts;
        }
        return pixelHistogramArrays;

    }



    public void setKey(String key) {
        this.key = key;
    }

    public void setHistogramsKey(String histogramsKey) {
        this.histogramsKey = histogramsKey;
    }

    public void setDatasetKey(String datasetKey) {
        this.datasetKey = datasetKey;
    }

    public void setBinWidth(double binWidth) {
        this.binWidth = binWidth;
    }
}
