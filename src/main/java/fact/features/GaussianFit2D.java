package fact.features;

import fact.Constants;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.EllipseOverlay;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.special.Erf;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

/*import fact.features.DistributionFromShower;*/

/**
 * Created by thomno on 15.08.15.
 */
public class GaussianFit2D implements StatefulProcessor {
    @Parameter(required = false, description = "Key containing the photoncharges", defaultValue = "photoncharge")
    private String photonchargeKey = "photoncharge";
    @Parameter(required = false, description = "The pixelSet on which the fit is performed", defaultValue = "shower")
    private String pixelSetKey = "shower";
  	@Parameter(required=false, description="key to the xvalue of the cog of the shower")
  	private String cogxKey = null;
  	@Parameter(required=false, description="key to the yvalue of the cog of the shower")
  	private String cogyKey = null;

    @Parameter(required = false, description = "Base name for the output keys", defaultValue = "Gaussian_2D_")
    private String outputKey = "Gaussian_2D_";

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();


    @Override
    public Data process(Data data) {
        //Erf.erf(10)
        double[] photoncharge = (double[]) data.get(photonchargeKey);
        PixelSet pixelSet = (PixelSet) data.get(pixelSetKey);
        double[] showerWeights = createShowerWeights(pixelSet.toIntArray(),
            photoncharge);

        // Calculate size vor later
        double size = 0;
        for (double v : showerWeights) {
          size += v;
        }

        //Get Center
    		double cogx = (Double) data.get(cogxKey);
    		double cogy = (Double) data.get(cogyKey);

        // Calculate sigma
        double[][] covarianceMatrix = calculateCovarianceMatrix(pixelSet.toIntArray(),
    				photoncharge, cogx, cogy);
        // System.out.println("Anzahl");

        GaussianNegLogLikelihood negLnL = new GaussianNegLogLikelihood(photoncharge, pixelSet);
        ObjectiveFunction ob_negLnL = new ObjectiveFunction(negLnL);

        MaxEval maxEval = new MaxEval(10000000);
        InitialGuess start_values = new InitialGuess(new double[] {cogx, cogy, covarianceMatrix[0][0], covarianceMatrix[0][1], covarianceMatrix[1][1]});
        PowellOptimizer optimizer = new PowellOptimizer(1e-4, 1e-2);
        PointValuePair result;
        try {
            result = optimizer.optimize(ob_negLnL, GoalType.MINIMIZE, start_values, maxEval);
        } catch (TooManyEvaluationsException e){
            // System.out.println("Bla nicht geschaft");
            result = new PointValuePair(new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN}, Double.NaN);
        }
        double[] result_point = result.getPoint();

        Double x = result_point[0];
        Double y = result_point[1];
        Double sigma11 = Math.abs(result_point[2]);
        Double sigma12 = Math.abs(result_point[3]);
        Double sigma22 = Math.abs(result_point[3]);
        data.put(outputKey + "x", x);
        data.put(outputKey + "y", y);
        data.put(outputKey + "sigma11", sigma11);
        data.put(outputKey + "sigma12", sigma12);
        data.put(outputKey + "sigma22", sigma22);
        if (!x.isNaN()) {
          double[][] matrixData = { { sigma11, sigma12 },
    				  { sigma12, sigma22 } };
          // System.out.println(sigma11);
          // System.out.println(sigma12);
          // System.out.println(sigma22);
          RealMatrix sigma_Matrix = MatrixUtils.createRealMatrix(matrixData);

          EigenDecomposition eig = new EigenDecomposition(sigma_Matrix);
    		  // turns out the eigenvalues describe the variance in the eigenbasis of
    		  // the covariance matrix
    		  double varianceLong = eig.getRealEigenvalue(0) / size;
    		  double varianceTrans = eig.getRealEigenvalue(1) / size;

    		  double length = Math.sqrt(varianceLong);
    		  double width = Math.sqrt(varianceTrans);

    		  double delta = calculateDelta(eig);
          data.put(outputKey + "width", width);
          data.put(outputKey + "length", length);
          data.put(outputKey + "delta", delta);
          //double r = Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2.0));
          if (0==0) {
              data.put(outputKey + "overlay_1", new EllipseOverlay(cogx, cogy, width, length, delta));
              data.put(outputKey + "overlay_2", new EllipseOverlay(cogx, cogy, 2*width, 2*length, delta));
          }
        }

        return data;
    }

    public void init(ProcessContext context) {
        int npix = Constants.NUMBEROFPIXEL;
        double[] pixel_x = new double[npix];
        double[] pixel_y = new double[npix];

        FactPixelMapping mapping = FactPixelMapping.getInstance();

        for (int pix = 0; pix < npix; pix++) {
            pixel_x[pix] = mapping.getPixelFromId(pix).getXPositionInMM();
            pixel_y[pix] = mapping.getPixelFromId(pix).getYPositionInMM();
        }
    }

    public void finish(){}

    public void resetState(){}

    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }

    public void setPhotonchargeKey(String photonchargeKey) {
        this.photonchargeKey = photonchargeKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }


    public String getCogxKey() {
    	return cogxKey;
    }


    public void setCogxKey(String cogxKey) {
    	this.cogxKey = cogxKey;
    }


    public String getCogyKey() {
    	return cogyKey;
    }


    public void setCogyKey(String cogyKey) {
    	this.cogyKey = cogyKey;
    }

    double getx (int id){
  		return pixelMap.getPixelFromId(id).getXPositionInMM();
  	}

  	double gety (int id){
  		return pixelMap.getPixelFromId(id).getYPositionInMM();
  	}

    public double[][] calculateCovarianceMatrix(int[] showerPixel,
  			double[] photoncharge, double cogx, double cogy) {
  		double variance_xx = 0;
  		double variance_yy = 0;
  		double covariance_xy = 0;
  		int i = 0;
  		for (int pix : showerPixel) {
  			double weight = photoncharge[pix];
  			double posx = pixelMap.getPixelFromId(pix).getXPositionInMM();
  			double posy = pixelMap.getPixelFromId(pix).getYPositionInMM();

  			variance_xx += weight * (posx - cogx) * (posx - cogx);
  			variance_yy += weight * (posy - cogy) * (posy - cogy);
  			covariance_xy += weight * (posx - cogx) * (posy - cogy);

  			i++;
  		}

  		double[][] matrixData = { { variance_xx, covariance_xy },
  				{ covariance_xy, variance_yy } };
  		return matrixData;
  	}
    public double calculateDelta(EigenDecomposition eig) {
  		// calculate the angle between the eigenvector and the camera axis.
  		// So basicly the angle between the major-axis of the ellipse and the
  		// camrera axis.
  		// this will be written in radians.
  		double longitudinalComponent = eig.getEigenvector(0).getEntry(0);
  		double transversalComponent = eig.getEigenvector(0).getEntry(1);
  		return Math.atan(transversalComponent / longitudinalComponent);
  	}

    public double[] createShowerWeights(int[] shower, double[] pixelWeights) {
      double[] weights = new double[shower.length];
      for (int i = 0; i < shower.length; i++) {
        weights[i] = pixelWeights[shower[i]];
      }
      return weights;
    }

    public class GaussianNegLogLikelihood implements MultivariateFunction {
        private double[] photoncharge;
        private PixelSet pixelSet;

        /**
         * @param photoncharge double array containing the photoncharge for each pixel
         * @param pixelSet int array containing all pixel chids for the pixel that survived cleaning
         * @param pixel_x double array containing the x coordinates for all pixel
         * @param pixel_y double array containing the y coordinates for all pixel
         */
        public GaussianNegLogLikelihood(double[] photoncharge, PixelSet pixelSet){
            this.photoncharge = photoncharge;
            this.pixelSet = pixelSet;
        }

        /**
         *
         * @param point a double array with length 4 containing r, x, y, sigma in this order
         * @return the negative log likelihood at this point for the given data
         */
        public double value(double[] point) {
            double x_0 = point[0];
            double y_0 = point[1];
            double sigma_11 = point[2];
            double sigma_12 = point[3];
            double sigma_22 = point[4];
            double neg_ln_L = 0;



            for (CameraPixel pix: pixelSet.set) {
              double x_1 = getx(pix.id) - x_0;
              double y_1 = gety(pix.id) - y_0;
              double term1 = 1 / 2 * Math.log(sigma_11 * sigma_22 - Math.pow(sigma_12, 2.0));
              double term2 = 1 / 2 * 1 / (sigma_11 * sigma_22 - Math.pow(sigma_12, 2.0)) * (Math.pow(x_1, 2.0) * sigma_22 + Math.pow(y_1, 2.0) * sigma_11 - 2 * sigma_12 * x_1 * y_1);
              neg_ln_L += (term1 + term2) * photoncharge[pix.id];
              /*
              double x_1 = getx(pix.id) - x_0;
              double y_1 = gety(pix.id) - y_0;

              double corrkof = sigma_12 / (Math.sqrt(sigma_11) * Math.sqrt(sigma_22));
              double term1 = Math.log(Math.sqrt(sigma_11) * Math.sqrt(sigma_22));
              double term2 = 1/2 * Math.log(1 - Math.pow(corrkof, 2.0));
              double term3 = 1 / (2 * (1 - Math.pow(corrkof, 2.0))) * (Math.pow(x_1, 2.0) / sigma_11 + Math.pow(y_1, 2.0) / sigma_22 - 2 * corrkof * x_1 * y_1 / (Math.sqrt(sigma_11) * Math.sqrt(sigma_22)));
              neg_ln_L += (term1 + term2 + term3) * photoncharge[pix.id];

                double x_1 = getx(pix.id) - x_0;
                double y_1 = gety(pix.id) - y_0;
                double term1 = Math.log(sigma_11 * sigma_22 - Math.pow(sigma_12, 2.0));
                double term2 = sigma_22 * Math.pow(x_1, 2.0) - 2 * sigma_12 * x_1 * y_1 + sigma_11 * Math.pow(y_1, 2.0);
                neg_ln_L += 0.5 * (term1 + term2) * photoncharge[pix.id];
              */
            }
            return neg_ln_L;
        }
    }
}
