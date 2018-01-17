package fact.filter;

import fact.Constants;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Small Processor to smooth the camera image. Works on discrete pixel values, not on a whole time series!!
 * Gets an array of 1440 values (for example photoncharge or arrival times).
 * 2D Bell Convolution Kernel using only next neighbours:
 * ___
 * ___/ 1 \___
 * / 1 \___/ 1 \
 * \___/ 6 \___/  __1_
 * / 1 \___/ 1 \   12
 * \___/ 1 \___/
 * \___/
 * <p>
 * Created by lena on 22.04.16.
 */
public class SmoothBell implements Processor {
    FactPixelMapping mapping = FactPixelMapping.getInstance();


    @Parameter(required = true, description = "Key for output array")
    public String outputKey;

    @Parameter(required = true, description = "Array/Values to smooth, e.g. photoncharge")
    public String inputKey;

    @Override
    public Data process(Data data) {

        double[] pixelValues = (double[]) data.get(inputKey);

        double[] smoothedImage = new double[Constants.N_PIXELS];


        for (int i = 0; i < Constants.N_PIXELS; i++) {
            CameraPixel[] neighbors = mapping.getNeighborsFromID(i);
            double sumNeighbors = 0;
            for (CameraPixel n : neighbors) {
                sumNeighbors += pixelValues[n.id];
            }

            smoothedImage[i] = (sumNeighbors + neighbors.length * pixelValues[i]) / (2 * neighbors.length);
        }


        data.put(outputKey, smoothedImage);

        return data;
    }
}
