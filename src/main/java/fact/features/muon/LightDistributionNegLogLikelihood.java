package fact.features.muon;

import fact.Constants;
import fact.hexmap.FactPixelMapping;
import org.apache.commons.math3.analysis.MultivariateFunction;

/**
 * Created by maxnoe on 13.08.15.
 */
public class LightDistributionNegLogLikelihood implements MultivariateFunction {
    private double[] photoncharge;
    private int[] cleaning_pixel;
    private FactPixelMapping pixelMapping = FactPixelMapping.getInstance();
    private double mirror_radius = 1.85;
    private double pixel_fov = Math.toRadians(0.11);
    private double focal_length = 4889;  // in mm
    // int_{lambda_1}^{lambda_2} lambda^{-2} dlambda
    private double integral = 2222222.22;

    /**
     * @param photoncharge double array containing the photoncharge for each pixel
     * @param cleaning_pixel int array containing all pixel chids for the pixel that survived cleaning
     */
    public LightDistributionNegLogLikelihood(double[] photoncharge, int[] cleaning_pixel){
        this.photoncharge = photoncharge;
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
        double rho = point[4];
        double phi = point[5];
        double eps = point[6];
        double ln_L = 0;

        for (int i = 0; i < cleaning_pixel.length; i++) {
            int pix = cleaning_pixel[i];
            double photons = photon_expectance_pixel(pix, r, x, y, sigma, rho, phi, eps);
            ln_L += PoissonLogP(photons, photoncharge[pix]);
        }

        return -ln_L;
    }

    public double gauss_density(double x, double mean, double std){
        double norm = 1.0 / Math.sqrt(2.0 * Math.PI * Math.pow(std, 2.0));
        return  norm * Math.exp(- 0.5 * Math.pow((x - mean)/std, 2.0));
    }

    private double intensity(double phi, double ring_radius, double rho, double eps){
        double theta_c = radius2theta(ring_radius);
        double D;
        if (rho > mirror_radius){
            if (Math.abs(phi) < Math.asin(mirror_radius / rho)) {
                D = 2 * mirror_radius * Math.sqrt(1 - Math.pow((rho / mirror_radius) * Math.sin(phi), 2.0));
            } else {
                D = 0;
            }
        } else {
            D = Math.sqrt(1 - Math.pow(rho / mirror_radius * Math.sin(phi), 2.0));
            D += rho / mirror_radius * Math.cos(phi);
            D *= mirror_radius;
        }

        return eps * 1.0/(137 * 2.0) * integral * (pixel_fov / theta_c) * Math.sin(2.0 * theta_c) * D;
    }

    public double radius2theta(double radius){
        return radius / focal_length;
    }

    public double photon_expectance_pixel(int chid, double r, double x, double y, double sigma, double rho, double phi, double eps){
        double pixel_x = pixelMapping.getPixelFromId(chid).getXPositionInMM();
        double pixel_y = pixelMapping.getPixelFromId(chid).getYPositionInMM();
        double pixel_phi = Math.atan2(pixel_y - y, pixel_x - x);
        double pixel_r = Math.sqrt(Math.pow(pixel_x - x, 2.0) + Math.pow(pixel_y - y, 2.0));
        return intensity(pixel_phi - phi, r, rho, eps) * Constants.PIXEL_SIZE * gauss_density(pixel_r, r, sigma);
    }

    private double PoissonLogP(double lambda, double k){
        return k * Math.log(lambda) - lambda;
    }
}