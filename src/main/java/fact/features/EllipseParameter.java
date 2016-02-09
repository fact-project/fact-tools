package fact.features;

import fact.Utils;
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

public class EllipseParameter implements Processor {

	@Parameter(required = false)
	public String estNumPhotonsKey = "pixels:estNumPhotons";

	@Parameter(required = false, description = "The key to the showerPixel. That is some sort of int[] containing pixel chids.")
	public String pixelSetKey = "shower";

	// the in and outputkeys
	@Parameter(required = false)
	public String outputKey = "shower:ellipse";
//	@Parameter(required = false, defaultValue="M3Long")
//	private String m3longKey = "M3Long";
//	@Parameter(required = false, defaultValue="M3Trans")
//	private String m3transKey = "M3Trans";
//	@Parameter(required = false, defaultValue="M4Long")
//	private String m4longKey = "M4Long";
//	@Parameter(required = false, defaultValue="M4Trans")
//	private String m4transKey = "M4Trans";
//	@Parameter(required = false, defaultValue="COGx")
//	private String cogxKey = "COGx";
//	@Parameter(required = false, defaultValue="COGy")
//	private String cogyKey = "COGy";
//	@Parameter(required = false, defaultValue="Length")
//	private String lengthKey = "Length";
//	@Parameter(required = false, defaultValue="Width")
//	private String widthKey = "Width";
//	@Parameter(required = false, defaultValue="Delta")
//	private String deltaKey = "Delta";

	FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	static Logger log = LoggerFactory.getLogger(EllipseParameter.class);

	@Override
	public Data process(Data item) {

		if (!item.containsKey(pixelSetKey) || !item.containsKey(estNumPhotonsKey) ) {
			return item;
		}


		Utils.isKeyValid(item, pixelSetKey, PixelSet.class);
		Utils.isKeyValid(item, estNumPhotonsKey, double[].class);

		PixelSet showerPixel = (PixelSet) item.get(pixelSetKey);
		double[] showerWeights = createShowerWeights(showerPixel.toIntArray(),
				(double[]) item.get(estNumPhotonsKey));

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
			double posx = pixelMap.getPixelFromId(pix.id).getXPositionInMM();
			double posy = pixelMap.getPixelFromId(pix.id).getYPositionInMM();
			// rotate
			double[] c = Utils.transformToEllipseCoordinates(posx, posy, cog[0], cog[1], delta);

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

//		PixelDistribution2D dist = new PixelDistribution2D(
//				covarianceMatrix.getEntry(0, 0),
//				covarianceMatrix.getEntry(1, 1),
//				covarianceMatrix.getEntry(0, 1), cog[0], cog[1], varianceLong,
//				varianceTrans, m3Long, m3Trans, m4Long, m4Trans, delta, size);

		// add calculated shower parameters to data item
//		item.put(outputKey:, dist);
		item.put(outputKey+ ":m3long", m3Long);
		item.put(outputKey+ ":m3trans", m3Trans);
		item.put(outputKey+ ":m4long", m4Long);
		item.put(outputKey+ ":m4trans", m4Trans);
		item.put(outputKey+ ":cog:x", cog[0]);
		item.put(outputKey+ ":cog:y", cog[1]);
		item.put(outputKey+ ":length", length);
		item.put(outputKey+ ":width", width);
		item.put(outputKey+ ":delta", delta);

		double[] center = calculateCenter(showerPixel);
		item.put("gui:2-sigma-ellipse", new EllipseOverlay(center[0], center[1], 2*width, 2*length, delta));
		item.put("gui:1-sigma-ellipse", new EllipseOverlay(center[0], center[1], width, length, delta));

		item.put("gui:width", width);
		item.put("gui:length", length);

		return item;
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
		double longitudinalComponent = eig.getEigenvector(0).getEntry(0);
		double transversalComponent = eig.getEigenvector(0).getEntry(1);
		return Math.atan(transversalComponent / longitudinalComponent);
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

	public RealMatrix calculateCovarianceMatrix(int[] showerPixel,
			double[] showerWeights, double[] cog) {
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

	
	

}
