package fact.container;

import com.sun.imageio.plugins.common.I18N;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;


/**
 *
 * A simple Histogram container, that bins values in a given Range from min. Value to max. Value for a given number of bins. The binWidth is calculated automatically.
 *
 * Created by jebuss on 21.09.15.
 */
public class Histogram1D implements Serializable {
    private static final long serialVersionUID = -7653148029952880948L;
    static Logger log = LoggerFactory.getLogger(Histogram1D.class);

    private double  DEFAULT_WEIGHT = 1.;
    private int     nBins       = 100;
    private double  min         = 0;
    private double  max         = 100;
    private double  underflow   = 0;
    private double  overflow    = 0;
    private double  binWidth    = 0;
    private double[] lowEdges;
    private double[] binCenters;
    private double[] counts;
    private double  nEvents     = 0;



    public Histogram1D(double min, double max, int nBins){
        this.max        = max;
        this.min        = min;
        this.nBins      = nBins;
        this.counts     = new double[nBins];
        this.binWidth   = (max - min)/nBins;
        this.lowEdges   = new double[nBins+1];
        this.binCenters = new double[nBins];
        this.calculateBinEdges();
        this.calculateBinCenters();
    }

    public Histogram1D(double min, double binWidth){
        this.binWidth   = binWidth;
        this.nBins      = 2;
        this.min        = min;
        this.max        = binWidth * nBins + min;
        this.counts     = new double[nBins];
        this.lowEdges   = new double[nBins+1];
        this.binCenters = new double[nBins];
        this.calculateBinEdges();
        this.calculateBinCenters();
    }

    private void addBinsBelow(int nBins){
        double[] newCounts = new double[nBins];
        double[] newLowEdges = new double[nBins];
        double[] newBinCenters = new double[nBins];

        this.min -= nBins*binWidth;

        this.counts      = ArrayUtils.addAll(newCounts, counts);
        this.lowEdges    = ArrayUtils.addAll(newLowEdges, lowEdges);
        this.binCenters  = ArrayUtils.addAll(newBinCenters, binCenters);

        calculateBinEdges();
        calculateBinCenters();

        this.nBins = counts.length;
    }

    private void addBinsAbove(int nBins){
        double[] newCounts = new double[nBins];
        double[] newLowEdges = new double[nBins];
        double[] newBinCenters = new double[nBins];

        this.max += nBins*binWidth;

        this.counts      = ArrayUtils.addAll(counts, newCounts);
        this.lowEdges    = ArrayUtils.addAll(lowEdges, newLowEdges);
        this.binCenters  = ArrayUtils.addAll(binCenters, newBinCenters);

        calculateBinEdges();
        calculateBinCenters();

        this.nBins = counts.length;
    }

    public void addValue(double value, double weight){

        int bin = calculateBinFromVal(value);

        if(bin < 0){
            addBinsBelow(Math.abs(bin));
            log.debug("Bin is missing, adding bin Below");
//            counts[0] += weight;
//            underflow += weight;
//            return;
        } else if (bin >= counts.length){
            addBinsAbove(bin - counts.length + 1);
            log.debug("Bin is missing, adding bin Above");
//            counts[counts.length - 1] += weight;
//            overflow += weight;
//            return;
        }

        bin = calculateBinFromVal(value);
        counts[bin] += weight;

        nEvents += 1.;
    }


    public void addValue(double value){
        addValue(value, DEFAULT_WEIGHT);
    }

    public void addSeries(double[] series){
        for (double val : series){
            addValue(val);
        }
    }

    public void addSeries(double[] series, double[] weights){
        int i =0;
        for (double val : series){
            addValue(val, weights[i]);
            i++;
        }
    }

    private void calculateBinEdges(){
        for( int i = 0; i < lowEdges.length; i++){
            lowEdges[i] = min + i*binWidth;
        }
    }

    public void calculateBinCenters(){
        for (int i = 0; i < this.binCenters.length; i++){
            this.binCenters[i] = this.lowEdges[i] +  this.binWidth/2;
        }
    }

    private int calculateBinFromVal(double value){
        return (int) ((value - this.lowEdges[0]) / this.binWidth);
    }

    public double[][] toArray() {
        double[][] XYArray = new double[2][this.nBins];
        XYArray[0] = this.binCenters;
        XYArray[1] = this.counts;

        return XYArray;
    }


    public int getnBins() {
        return nBins;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getUnderflow() {
        return underflow;
    }

    public double getOverflow() {
        return overflow;
    }

    public double getBinWidth() {
        return binWidth;
    }

    public double[] getLowEdges() {
        return lowEdges;
    }

    public double getnEvents() {
        return nEvents;
    }

    public double[] getCounts() {
        return counts;
    }
}
