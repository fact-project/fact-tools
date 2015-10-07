package fact.features.singlePulse;

import org.apache.commons.math3.analysis.MultivariateFunction;

/**
 * Multivariate Function to calculate the Loglikelihood function to model the single pe spectrum
 *
 * Created by jebuss on 02.10.15.
 */
public class SinglePeSpectrumLogLikelihood implements MultivariateFunction {

    private double[] counts;

    private double[] binCenters;

    public SinglePeSpectrumLogLikelihood(double[] binCenters, double[] counts){
        this.counts = counts;
        this.binCenters = binCenters;
    }

    @Override
    public double value(double[] point) {
        double negLL = 0.;

        for (int bin = 0; bin < counts.length; bin++){
            double f = model2(binCenters[bin], point);
            negLL += - counts[bin] * Math.log( f ) + f;
        }

        return negLL;
    }

    private double model(double x, double[] point ){
        double amplitude = point[0];
        double gain      = point[1];
        double sigma     = point[2];
        double cross     = point[3];
        double shift     = point[4];
        double noise     = point[5];
        double expo      = point[6];

        double y = 0.;
        sigma = sigma*gain;
        double sig1 = Double.NaN;
        double amp  = Double.NaN;

        for (int N = 1; N < 5; N++){
            amp = amplitude / (N*2);
            double muN = N * gain + shift;
            double sigN = Math.sqrt(N * sigma * sigma + noise * noise);
            double p = Math.pow(cross, (N-1)) * Math.pow(N, (-expo) );

            y += amp * Math.exp(-1/2*  Math.pow( (x-muN)/sigN , 2. )) *p/sigN ;
            sig1 = Math.sqrt(sigma*sigma + noise*noise);
        }

        return amp*sig1*y;
    }


    /**
     * A model for the single Pe spectrum according to:
     * https://trac.fact-project.org/browser/trunk/Mars/fact/analysis/gain/fit_spectra.C
     *
     */
    private double model2(double x, double[] point ){
        double amplitude = point[0];
        double gain      = point[1];
        double sigma     = point[2]*gain;
        double cross     = point[3];
        double shift     = point[4];
        double noise     = point[5] < 0 ? sigma : point[5];
        double expo      = point[6];

        double y = 0.;

        for (int N = 1; N < 14; N++){

            double muN = N * gain + shift;
            double sigN = Math.sqrt(N * sigma * sigma + noise * noise);
            double p = Math.pow(cross, N-1 ) * Math.pow(N, -expo );

            y += Math.exp(-1/2*  Math.pow( (x-muN)/sigN , 2. )) *p/sigN ;
        }
        double sig1 = Math.sqrt(sigma*sigma + noise*noise);

        return amplitude*sig1*y;
    }

}
