package fact.features.singlePulse;

import fact.Constants;
import fact.Utils;
import fact.container.Histogram1D;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.util.ArrayList;
import java.util.Collection;

import static org.apache.commons.math3.fitting.AbstractCurveFitter.*;

/**
 * Gets a pixel Array of Histogram1D objects that contain a single Pe like spectrum and
 * fits a single pe spectrum model to the data to calculate gain, crosstalk etc. for each pixel.
 *
 * Created by jebuss on 01.10.15.
 */
public class FitSpectra implements StatefulProcessor{

    static Logger log = LoggerFactory.getLogger(FitSpectra.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = false)
    private String outputKey;
    @Parameter(required = false)
    private String startValuesKey;

    private int eventIncrement = 250;

    private int npix = Constants.NUMBEROFPIXEL;

    private Histogram1D[] histograms;

    ParametricUnivariateFunction FitFunction;

    double[][] fitParamters;
    double[][] startValues;

    double[] amplitude = new double[npix];
    double[] gain      = new double[npix];
    double[] sigma     = new double[npix];
    double[] cross     = new double[npix];
    double[] shift     = new double[npix];
    double[] noise     = new double[npix];
    double[] expo      = new double[npix];



    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        int eventNum = (int) input.get("EventNum");

        Utils.isKeyValid(input, key, Histogram1D[].class);
        histograms = (Histogram1D[]) input.get(key);



        if ( !(eventNum % eventIncrement == 0) ){
            input.put(outputKey, fitParamters);
            return input;
        }


        for( int pix = 0; pix < npix; pix++){
            startValues[pix]  = estimateStartValues(histograms[pix]);
            fitParamters[pix] = FitHistogram(histograms[pix], startValues[pix]);
        }

        input.put(outputKey, fitParamters);
        input.put(startValuesKey, startValues);
        return input;
    }

    @Override
    public void init(ProcessContext context) throws Exception {

        fitParamters = new double[npix][];
        startValues  = new double[npix][];

        for (int pix = 0; pix < npix; pix++) {

            fitParamters[pix] = new double[7];
            startValues[pix]  = new double[7];
            for (int i = 0; i < 7; i++) {
                fitParamters[pix][i] = Double.NaN;
                startValues[pix][i]  = Double.NaN;
            }
        }


    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    public double EstimateSigma(double fractionOfMax, Histogram1D histogram){
        int maxBin    = histogram.getMaximumBin();
        double[] counts = histogram.getCounts();
        double[] centers = histogram.getBinCenters();
        double maximum = counts[maxBin];

        double halfWidth = 0.;
        for (int i = 0; i < counts.length - maxBin; i++) {
            try{
                if (counts[maxBin + i] <= fractionOfMax * maximum ||
                        counts[maxBin - i] <= fractionOfMax * maximum){
                    halfWidth = centers[maxBin + i] - centers[maxBin - i];
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e){
                halfWidth = 0.;
            }
        }
        double sigma = halfWidth/ 2 / Math.sqrt(2 * Math.log(1 / fractionOfMax));

        return sigma;
    }

    private double[] estimateStartValues(Histogram1D histogram){
        int maxBin    = histogram.getMaximumBin();

        double maxFrequency = histogram.getCounts()[maxBin];
        double maxIntegral  = histogram.getBinCenters()[maxBin];
        double sigma_start  = EstimateSigma(3./4, histogram);

        double[] start_values = new double[] {
                maxFrequency,   //amplitude
                maxIntegral,    //gain
                sigma_start,    //sigma
                0.10,           //crosstalk
                0.,             //baselineshift
                .3,          //noise
                0.95             //coeffn
        };
        return start_values;
    }

    public double[] FitHistogram(Histogram1D histogram, double[] start_values){

        double[] binCenters = histogram.getBinCenters();
        double[] counts     = histogram.getCounts();

//        int borderBin = histogram.calculateBinFromVal(200);
//        int newLength = binCenters.length;
//
//        if (borderBin < binCenters.length){
//            newLength = binCenters.length - borderBin;
//        } else {
//            borderBin = 0;
//        }
//
//        double[] newCenters = new double[newLength];
//        double[] newCounts  = new double[newLength];
//
//        System.arraycopy(binCenters, borderBin, newCenters, 0, newLength);
//        System.arraycopy(counts, borderBin, newCounts, 0, newLength);
//



        SinglePeSpectrumLogLikelihood negLnL = new SinglePeSpectrumLogLikelihood(binCenters, counts);
        ObjectiveFunction ob_negLnL = new ObjectiveFunction(negLnL);

        MaxEval maxEval = new MaxEval(1000);
        InitialGuess intitial_guess = new InitialGuess(start_values);
        PowellOptimizer optimizer = new PowellOptimizer(1e-4, 1e-8);

        double[] parameters = new double[7];
        try{
            PointValuePair result = optimizer.optimize(ob_negLnL, GoalType.MINIMIZE, intitial_guess, maxEval);

            parameters = result.getPoint();
        } catch (TooManyEvaluationsException e){
            for (int i = 0; i < 7; i++) {
                parameters[i] = Double.NaN;
            }
        }

        return parameters.clone();
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setStartValuesKey(String startValuesKey) {
        this.startValuesKey = startValuesKey;
    }
}
