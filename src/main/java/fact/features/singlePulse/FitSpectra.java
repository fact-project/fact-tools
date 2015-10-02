package fact.features.singlePulse;

import fact.Constants;
import fact.Utils;
import fact.container.Histogram1D;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

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

    private int eventIncrement = 1000;

    private int npix = Constants.NUMBEROFPIXEL;

    private Histogram1D[] histograms;

    ParametricUnivariateFunction FitFunction;

    double[][] fitParamters;



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

            double[] binCenters = histograms[pix].getBinCenters();
            double[] counts     = histograms[pix].getCounts();
            int maxBin    = histograms[pix].getMaximumBin();
            double maxFrequency = histograms[pix].getCounts()[maxBin];
            double maxIntegral  = histograms[pix].getBinCenters()[maxBin];


            SinglePeSpectrumLogLikelihood negLnL = new SinglePeSpectrumLogLikelihood(binCenters, counts);
            ObjectiveFunction ob_negLnL = new ObjectiveFunction(negLnL);

            MaxEval maxEval = new MaxEval(1000);
            InitialGuess start_values = new InitialGuess(new double[] {maxFrequency, maxIntegral, .4, 1.5, 5, .0005, 0.2});
            PowellOptimizer optimizer = new PowellOptimizer(1e-4, 1e-8);

            double[] parameters = new double[7];
            try{
                PointValuePair result = optimizer.optimize(ob_negLnL, GoalType.MINIMIZE, start_values, maxEval);

                parameters = result.getPoint();
            } catch (TooManyEvaluationsException e){
                for (int i = 0; i < 7; i++) {
                    parameters[i] = Double.NaN;
                }
            }

            fitParamters[pix] = parameters.clone();
        }

        input.put(outputKey, fitParamters);
        return input;
    }

    @Override
    public void init(ProcessContext context) throws Exception {

        fitParamters = new double[npix][];
        for( double[] parameters : fitParamters){
            parameters = new double[7];
            for (int i = 0; i < 7; i++) {
                parameters[i] = Double.NaN;
            }
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
}
