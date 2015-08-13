package fact.features.muon;

import org.apache.commons.math3.analysis.MultivariateFunction;

/**
 * Created by maxnoe on 13.08.15.
 */
public class GaussianNegLogLikelihood implements MultivariateFunction {
    private double[] photoncharge;
    private double[] pixel_x;
    private double[] pixel_y;
    private int[] cleaning_pixel;

    /**
     * @param photoncharge double array containing the photoncharge for each pixel
     * @param cleaning_pixel int array containing all pixel chids for the pixel that survived cleaning
     * @param pixel_x double array containing the x coordinates for all pixel
     * @param pixel_y double array containing the y coordinates for all pixel
     */
    public GaussianNegLogLikelihood(double[] photoncharge, int[] cleaning_pixel, double[] pixel_x, double[] pixel_y){
        this.photoncharge = photoncharge;
        this.pixel_x = pixel_x;
        this.pixel_y = pixel_y;
        this.cleaning_pixel = cleaning_pixel;
    }

    /**
     *
     * @param point a double array with length 4 containing r, x, y, sigma in this order
     * @return the negative log likelihood at this point for the given data
     */
    public double value(double[] point) {
        double r = point[0];
        double x = point[1];
        double y = point[2];
        double sigma = point[3];
        double neg_ln_L = 0;

        for (int i = 0; i < cleaning_pixel.length; i++) {
            int pix = cleaning_pixel[i];
            double distance = Math.sqrt(Math.pow(pixel_x[pix] - x, 2.0) + Math.pow(pixel_y[pix] - y, 2.0));
            neg_ln_L += (Math.log(sigma) + Math.pow((distance - r) / sigma, 2)) * photoncharge[pix];
        }

        return neg_ln_L;
    }
}