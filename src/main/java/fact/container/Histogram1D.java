package fact.container;

import java.io.Serializable;


/**
 *
 * A simple Histogram container, that bins values in a given Range from min. Value to max. Value for a given number of bins. The binWidth is calculated automatically.
 *
 * Created by jebuss on 21.09.15.
 */
public class Histogram1D implements Serializable {
    private static final long serialVersionUID = -7653148029952880948L;

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
        this.CalculateBinEdges();
        this.binCenters = this.calculateBinCenters();
    }

    private void CalculateBinEdges(){
        for( int i = 0; i < nBins+1; i++){
            lowEdges[i] = min + i*binWidth;
        }
    }

    public void AddValue(double value, double weight){
        if(value < min){
            underflow += weight;
        } else if (value >= max){
            overflow += weight;
        } else {
            int bin = (int) ((value - this.min) / this.binWidth);
            counts[bin] += weight;
        }
        nEvents += 1.;
    }

    public void AddValue(double value){
        AddValue(value, DEFAULT_WEIGHT);
    }

    public void AddSeries(double[] series){
        for (double val : series){
            AddValue(val);
        }
    }

    public void AddSeries(double[] series, double[] weights){
        int i =0;
        for (double val : series){
            AddValue(val, weights[i]);
            i++;
        }
    }

    public double[] calculateBinCenters(){
        double[] binCenters = new double[this.counts.length];
        for (int i = 0; i < this.counts.length; i++){
            binCenters[i] = this.lowEdges[i] +  this.binWidth/2;
        }
        return binCenters;
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
