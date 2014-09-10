package fact.features;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Utils;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.EllipseOverlay;
import fact.statistics.PixelDistribution2D;

public class DistributionFromShower implements Processor {

	@Parameter(required = true)
	private String weightsKey = null;

	@Parameter(required = true, description = "The key to the showerPixel. That is some sort of int[] containing pixel chids.  ")
	private String showerKey = null;

	@Parameter(required = false)
	private String sizeKey;

	// hte in and outputkeys
	@Parameter(required = true)
	private String outputKey = null;

	FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	private double[] longitudinalCoords = null;
	private double[] transversalCoords = null;

	// A logger
	static Logger log = LoggerFactory.getLogger(DistributionFromShower.class);

	// what do we need to calculate the ellipse?

	@Override
	public Data process(Data input) {
		// get the required stuff from the getColorFromValue
		// in case the getColorFromValue doesn't contain a shower return the
		// original input.
		try {
			Utils.isKeyValid(input, showerKey, int[].class);
			Utils.isKeyValid(input, weightsKey, double[].class);
		} catch (Exception e) {
			log.debug(
					"No shower data found for key '{}', no distribution can be computed.",
					showerKey);
			return input;
		}

		int[] showerPixel = (int[]) input.get(showerKey);
		double[] wheightsArray = (double[]) input.get(weightsKey);

		double size = 0;
		for (int pix : showerPixel) {
			size += wheightsArray[pix];
		}

		double[] cog = calculateCog(wheightsArray, showerPixel, size);

		// Calculate the weighted Empirical variance along the x and y axis.

		RealMatrix covarianceMatrix = calculateCovarianceMatrix(showerPixel,
				wheightsArray, cog);

		// get the eigenvalues and eigenvectors of the matrix and weigh them
		// accordingly.
		EigenDecomposition eig = new EigenDecomposition(covarianceMatrix);
		// turns out the eigenvalues describe the variance in the eigenbasis of
		// the covariance matrix
		double varianceLong = eig.getRealEigenvalue(0) / size;
		double varianceTrans = eig.getRealEigenvalue(1) / size;

		double length = Math.sqrt(varianceLong);
		double width = Math.sqrt(varianceTrans);

		// calculate the angle between the eigenvector and the camera axis.
		// So basicly the angle between the major-axis of the ellipse and the
		// camrera axis.
		// this will be written in radians.
		double longitudinalComponent = eig.getEigenvector(0).getEntry(0);
		double transversalComponent = eig.getEigenvector(0).getEntry(1);
		double delta = Math.atan(transversalComponent / longitudinalComponent);

		// Calculation of the showers statistical moments (Variance, Skewness,
		// Kurtosis)
		// =======================================
		// Rotate the shower by the angle delta in order to have the ellipse
		// main axis in parallel to the Camera-Coordinates X-Axis

		// allocate variables for rotated coordinates
		longitudinalCoords = new double[showerPixel.length];
		transversalCoords = new double[showerPixel.length];

		for (int i = 0; i < showerPixel.length; i++) {
			// translate to center
			double posx = pixelMap.getPixelFromId(showerPixel[i])
					.getXPositionInMM();
			double posy = pixelMap.getPixelFromId(showerPixel[i])
					.getYPositionInMM();
			// rotate
			double[] c = Utils.rotateAndTranslatePointInShowerSystem(posx,
					posy, cog[0], cog[1], delta);
			// fill array of new showerKey coordinates
			longitudinalCoords[i] = c[0];
			transversalCoords[i] = c[1];
		}
		double m3Long = calculateSkewness(0, longitudinalCoords, wheightsArray);
		double m3Trans = calculateSkewness(0, transversalCoords, wheightsArray);

		double m4Long = calculateKurtosis(0, longitudinalCoords, wheightsArray);
		double m4Trans = calculateKurtosis(0, transversalCoords, wheightsArray);

		PixelDistribution2D dist = new PixelDistribution2D(
				covarianceMatrix.getEntry(0, 0),
				covarianceMatrix.getEntry(1, 1),
				covarianceMatrix.getEntry(0, 1), cog[0], cog[1], varianceLong,
				varianceTrans, m3Long, m3Trans, m4Long, m4Trans, delta, size);

		// add calculated shower parameters to data item
		input.put(outputKey, dist);
		input.put("varianceLong", varianceLong);
		input.put("varianceTrans", varianceTrans);
		input.put("M3Long", m3Long);
		input.put("M3Trans", m3Trans);
		input.put("M4Long", m4Long);
		input.put("M4Trans", m4Trans);
		input.put("COGx", cog[0]);
		input.put("COGy", cog[1]);
		input.put("Length", length);
		input.put("Width", width);
		input.put("Delta", delta);
		input.put("Ellipse", new EllipseOverlay(cog[0], cog[1], width, length,
				delta));

		input.put("@width", width);
		input.put("@length", length);
		// input.put(Constants.ELLIPSE_OVERLAY, new LineOverlay(cogX, cogY,
		// delta, Color.green));

		// look at what i found
		// V=cov(x,y);
		// [vec,val]=eig(V);
		// angles=atan2( vec(2,:),vec(1,:) );

		return input;
	}

	public double calculateSkewness(double mean, double[] values,
			double[] weights) {
		double sumWeights = 0;
		double skewness = 0;
		for (int i = 0; i < values.length; i++) {
			sumWeights += weights[i];
			skewness += weights[i] * Math.pow(mean - values[i], 3);
		}
		return skewness / sumWeights;
	}

	public double calculateKurtosis(double mean, double[] values,
			double[] weights) {
		double sumWeights = 0;
		double kurtosis = 0;
		for (int i = 0; i < values.length; i++) {
			sumWeights += weights[i];
			kurtosis += weights[i] * Math.pow(mean - values[i], 4);
		}
		return kurtosis / sumWeights;
	}

	public double[] calculateCog(double[] weights, int[] showerPixel,
			double size) {

		double[] cog = { 0, 0 };
		// find weighted center of the shower pixels.
		for (int pix : showerPixel) {
			cog[0] += weights[pix]
					* pixelMap.getPixelFromId(pix).getXPositionInMM();
			cog[1] += weights[pix]
					* pixelMap.getPixelFromId(pix).getYPositionInMM();
		}
		// divide the center coordinates by size. I'm not sure if this is
		// correct. I checked it. It is.
		cog[0] /= size;
		cog[1] /= size;

		return cog;

	}

	public RealMatrix calculateCovarianceMatrix(int[] showerPixel,
			double[] wheightsArray, double[] cog) {
		double variance_xx = 0;
		double variance_yy = 0;
		double covariance_xy = 0;
		for (int pix : showerPixel) {
			double posx = pixelMap.getPixelFromId(pix).getXPositionInMM();
			double posy = pixelMap.getPixelFromId(pix).getYPositionInMM();
			variance_xx += wheightsArray[pix] * (posx - cog[0])
					* (posx - cog[0]);
			variance_yy += wheightsArray[pix] * (posy - cog[1])
					* (posy - cog[1]);
			covariance_xy += wheightsArray[pix] * (posx - cog[0])
					* (posy - cog[1]);
		}

		double[][] matrixData = { { variance_xx, covariance_xy },
				{ covariance_xy, variance_yy } };
		return MatrixUtils.createRealMatrix(matrixData);
	}

	public void setWeightsKey(String wheights) {
		this.weightsKey = wheights;
	}

	public void setShowerKey(String showerKey) {
		this.showerKey = showerKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public void setSizeKey(String sizeKey) {
		this.sizeKey = sizeKey;
	}

}
