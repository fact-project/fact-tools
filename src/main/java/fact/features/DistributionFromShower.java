package fact.features;

import fact.Utils;
import fact.container.PixelDistribution2D;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.EllipseOverlay;
import fact.container.PixelSet;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class DistributionFromShower implements Processor {

	@Parameter(required = true)
	private String weightsKey = null;
	@Parameter(required = true, description = "The key to the showerPixel. "
			+ "That is some sort of int[] containing pixel chids.")
	private String pixelSetKey = null;

	// the in and outputkeys
	@Parameter(required = true)
	private String outputKey = null;
	@Parameter(required = false, defaultValue="M3Long")
	private String m3longKey = "M3Long";
	@Parameter(required = false, defaultValue="M3Trans")
	private String m3transKey = "M3Trans";
	@Parameter(required = false, defaultValue="M4Long")
	private String m4longKey = "M4Long";
	@Parameter(required = false, defaultValue="M4Trans")
	private String m4transKey = "M4Trans";
	@Parameter(required = false, defaultValue="COGx")
	private String cogxKey = "COGx";
	@Parameter(required = false, defaultValue="COGy")
	private String cogyKey = "COGy";
	@Parameter(required = false, defaultValue="Length")
	private String lengthKey = "Length";
	@Parameter(required = false, defaultValue="Width")
	private String widthKey = "Width";
	@Parameter(required = false, defaultValue="Delta")
	private String deltaKey = "Delta";

	FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	// A logger
	static Logger log = LoggerFactory.getLogger(DistributionFromShower.class);

	@Override
	public Data process(Data input) {
		// get the required stuff from the getColorFromValue
		// in case the getColorFromValue doesn't contain a shower return the
		// original input.

		if (!input.containsKey(pixelSetKey)) {
			return input;
		}

		if (!input.containsKey(weightsKey)) {
			return input;
		}

		Utils.isKeyValid(input, pixelSetKey, PixelSet.class);
		Utils.isKeyValid(input, weightsKey, double[].class);

		PixelSet showerPixel = (PixelSet) input.get(pixelSetKey);
		double[] showerWeights = createShowerWeights(showerPixel.toIntArray(),
				(double[]) input.get(weightsKey));

		double size = 0;
		for (double v : showerWeights) {
			size += v;
		}

		double[] cog = calculateCog(showerWeights, showerPixel.toIntArray(), size);

		// Calculate the weighted Empirical variance along the x and y axis.
		RealMatrix covarianceMatrix = calculateCovarianceMatrix(showerPixel.toIntArray(), showerWeights, cog);

		// get the eigenvalues and eigenvectors of the matrix and weigh them
		// accordingly.
		EigenDecomposition eig = new EigenDecomposition(covarianceMatrix);
		// turns out the eigenvalues describe the variance in the eigenbasis of
		// the covariance matrix
		double varianceLong = eig.getRealEigenvalue(0) / size;
		double varianceTrans = eig.getRealEigenvalue(1) / size;

		double length = Math.sqrt(varianceLong);
		double width = Math.sqrt(varianceTrans);

		double delta = calculateDelta(eig);

		// Calculation of the showers statistical moments (Variance, Skewness, Kurtosis)
		// Rotate the shower by the angle delta in order to have the ellipse
		// main axis in parallel to the Camera-Coordinates X-Axis
		// allocate variables for rotated coordinates
		double[] longitudinalCoords = new double[showerPixel.set.size()];
		double[] transversalCoords = new double[showerPixel.set.size()];

		int counter =0;
		for (CameraPixel pix : showerPixel.set) {
			// translate to center
			double posx = pixelMap.getPixelFromId(pix.id)
					.getXPositionInMM();
			double posy = pixelMap.getPixelFromId(pix.id)
					.getYPositionInMM();
			// rotate
			double[] c = Utils.transformToEllipseCoordinates(posx, posy,
					cog[0], cog[1], delta);

			// fill array of new shower coordinates
			longitudinalCoords[counter] = c[0];
			transversalCoords[counter] = c[1];
			counter++;
		}

		// find max long coords
		double maxLongCoord = 0;
		double minLongCoord = 0;
		for (double l : longitudinalCoords) {
			maxLongCoord = Math.max(maxLongCoord, l);
			minLongCoord = Math.min(minLongCoord, l);
		}

		double maxTransCoord = 0;
		for (double l : transversalCoords) {
			maxTransCoord = Math.max(maxTransCoord, l);
		}

		double m3Long = calculateMoment(3, 0, longitudinalCoords, showerWeights);
		m3Long /= Math.pow(length, 3);
		double m3Trans = calculateMoment(3, 0, transversalCoords, showerWeights);
		m3Trans /= Math.pow(width, 3);

		double m4Long = calculateMoment(4, 0, longitudinalCoords, showerWeights);
		m4Long /= Math.pow(length, 4);
		double m4Trans = calculateMoment(4, 0, transversalCoords, showerWeights);
		m4Trans /= Math.pow(width, 4);

		PixelDistribution2D dist = new PixelDistribution2D(
				covarianceMatrix.getEntry(0, 0),
				covarianceMatrix.getEntry(1, 1),
				covarianceMatrix.getEntry(0, 1), cog[0], cog[1], varianceLong,
				varianceTrans, m3Long, m3Trans, m4Long, m4Trans, delta, size);

		// add calculated shower parameters to data item
		input.put(outputKey, dist);
		input.put(m3longKey, m3Long);
		input.put(m3transKey, m3Trans);
		input.put(m4longKey, m4Long);
		input.put(m4transKey, m4Trans);
		input.put(cogxKey, cog[0]);
		input.put(cogyKey, cog[1]);
		input.put(lengthKey, length);
		input.put(widthKey, width);
		input.put(deltaKey, delta);

		double[] center = calculateCenter(showerPixel);
		input.put("2-sigma-ellipse", new EllipseOverlay(center[0], center[1], 2*width,
				2*length, delta));
		input.put("1-sigma-ellipse", new EllipseOverlay(center[0], center[1], width,
				length, delta));

		input.put("@width", width);
		input.put("@length", length);

		return input;
	}

	public double[] createShowerWeights(int[] shower, double[] pixelWeights) {
		double[] weights = new double[shower.length];
		for (int i = 0; i < shower.length; i++) {
			weights[i] = pixelWeights[shower[i]];
		}
		return weights;
	}

	public double calculateDelta(EigenDecomposition eig) {
		// calculate the angle between the eigenvector and the camera axis.
		// So basicly the angle between the major-axis of the ellipse and the
		// camrera axis.
		// this will be written in radians.
		double x = eig.getEigenvector(0).getEntry(0);
		double y = eig.getEigenvector(0).getEntry(1);
		return Math.atan2(y, x);
	}

	public double calculateMoment(int moment, double mean, double[] values,
			double[] weights) {
		double sumWeights = 0;
		double m = 0;
		for (int i = 0; i < values.length; i++) {
			sumWeights += weights[i];
			m += weights[i] * Math.pow(values[i] - mean, moment);
		}
		return m / sumWeights;
	}

	public double[] calculateCog(double[] weights, int[] showerPixel,
			double size) {

		double[] cog = { 0, 0 };
		// find weighted center of the shower pixels.
		int i = 0;
		for (int pix : showerPixel) {
			cog[0] += weights[i] * pixelMap.getPixelFromId(pix).getXPositionInMM();
			cog[1] += weights[i] * pixelMap.getPixelFromId(pix).getYPositionInMM();
			i++;
		}
		cog[0] /= size;
		cog[1] /= size;
		return cog;
	}

	public double[] calculateCenter(PixelSet showerPixel) {

		double[] cog = { 0, 0 };
		// find center of the shower pixels.
		for (CameraPixel pix : showerPixel.set) {
			cog[0] += pixelMap.getPixelFromId(pix.id).getXPositionInMM();
			cog[1] += pixelMap.getPixelFromId(pix.id).getYPositionInMM();
		}
		cog[0] /= showerPixel.set.size();
		cog[1] /= showerPixel.set.size();
		return cog;
	}

	public RealMatrix calculateCovarianceMatrix(int[] showerPixel, double[] showerWeights, double[] cog) {
		double variance_xx = 0;
		double variance_yy = 0;
		double covariance_xy = 0;
		int i = 0;
		for (int pix : showerPixel) {
			double weight = showerWeights[i];
			double posx = pixelMap.getPixelFromId(pix).getXPositionInMM();
			double posy = pixelMap.getPixelFromId(pix).getYPositionInMM();

			variance_xx += weight * (posx - cog[0]) * (posx - cog[0]);
			variance_yy += weight * (posy - cog[1]) * (posy - cog[1]);
			covariance_xy += weight * (posx - cog[0]) * (posy - cog[1]);

			i++;
		}

		double[][] matrixData = { { variance_xx, covariance_xy },
				{ covariance_xy, variance_yy } };
		return MatrixUtils.createRealMatrix(matrixData);
	}

	public void setWeightsKey(String wheights) {
		this.weightsKey = wheights;
	}

	public void setPixelSetKey(String pixelSetKey) {
		this.pixelSetKey = pixelSetKey;
	}


	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}


	public void setM3longKey(String m3longKey) {
		this.m3longKey = m3longKey;
	}


	public void setM3transKey(String m3transKey) {
		this.m3transKey = m3transKey;
	}


	public void setM4longKey(String m4longKey) {
		this.m4longKey = m4longKey;
	}

	public void setM4transKey(String m4transKey) {
		this.m4transKey = m4transKey;
	}


	public void setCogxKey(String cogxKey) {
		this.cogxKey = cogxKey;
	}


	public void setCogyKey(String cogyKey) {
		this.cogyKey = cogyKey;
	}


	public void setLengthKey(String lengthKey) {
		this.lengthKey = lengthKey;
	}


	public void setWidthKey(String widthKey) {
		this.widthKey = widthKey;
	}


	public void setDeltaKey(String deltaKey) {
		this.deltaKey = deltaKey;
	}



}
