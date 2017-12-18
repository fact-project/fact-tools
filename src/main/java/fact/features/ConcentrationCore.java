package fact.features;

import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


public class ConcentrationCore implements Processor{
	static Logger log = LoggerFactory.getLogger(ConcentrationCore.class);

	@Parameter(required=true)
	private String outputKey;
	@Parameter(required = true, description  = "Key of the Center of Gravity X (by Distribution from shower)")
	private String cogxKey;
	@Parameter(required = true, description  = "Key of the Center of Gravity Y (by Distribution from shower)")
	private String cogyKey;
	@Parameter(required = true, description  = "Key of the delta angle")
	private String deltaKey;
	@Parameter(required = true, description  = "Key of the sizeKey")
	private String sizeKey;
	@Parameter(required = true, description  = "Key of the photoncharge array")
	private String photonChargeKey;
	@Parameter(required = true, description  = "Key of the shower pixel array")
	private String pixelSetKey;
	@Parameter(required = true, description  = "Key of the shower width")
	private String widthKey;
	@Parameter(required = true, description  = "Key of the shower lengthKey")
	private String lengthKey;



	/**
	 * Calculate the percentage of photons inside the Hillas Ellipse
	 * aka. the pixels with a Mahalanobis Distance <= 1.
	 */
	public Data process(Data input)
	{

		Utils.mapContainsKeys( input, cogxKey, cogyKey, deltaKey, photonChargeKey, pixelSetKey, lengthKey, widthKey, sizeKey);

		Double cogx = (Double) input.get(cogxKey);
		Double cogy = (Double) input.get(cogyKey);
		Double delta = (Double) input.get(deltaKey);
		double [] photonChargeArray = (double[]) input.get(photonChargeKey);
		PixelSet showerPixelSet = (PixelSet) input.get(pixelSetKey);
		Double length = (Double) input.get(lengthKey);
		Double width = (Double) input.get(widthKey);
		Double size = (Double) input.get(sizeKey);


		double photonsInEllipse = 0;
		for(CameraPixel pix: showerPixelSet.set) {
			double px = pix.getXPositionInMM();
			double py = pix.getYPositionInMM();

			double[] ellipseCoords = Utils.transformToEllipseCoordinates(px, py, cogx, cogy, delta);

			// add a tolerance of 10% of the pixel size to not only get pixels with the center in the ellipse
			double dl = Math.abs(ellipseCoords[0]) - 0.1 * Constants.PIXEL_SIZE_MM;
			double dt = Math.abs(ellipseCoords[1]) - 0.1 * Constants.PIXEL_SIZE_MM;

			double distance = Math.pow(dl / length, 2.0) + Math.pow(dt / width, 2.0);

			if (distance <= 1) {
				photonsInEllipse += photonChargeArray[pix.id];
			}
		}
		double concCore = photonsInEllipse / size;
		input.put(outputKey, concCore);
		return input;
	}
}
