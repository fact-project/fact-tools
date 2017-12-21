package fact.features.video;

import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


public class CenterOfGravity implements Processor {


    private static final Logger log = LoggerFactory.getLogger(CenterOfGravity.class);

    @Parameter(required = true, defaultValue = "showerPixelKey", description = "Key to the array of showerpixel chids.")
    private String showerPixelKey;

    @Parameter(required = true, defaultValue = "DataCalibrated", description = "Key to the calibrated data array.")
    private String dataCalibratedKey;

    @Parameter(required = true, defaultValue = "CenterOfGravity", description = "The output key tag. Will be inserted before all output keys.")
    public String outputKey;

    @Parameter(required = true, defaultValue = "4", description = "Minimum of shower pixel to start calculation.")
    public int numberOfShowerPixelThreshold;


    private FactPixelMapping pixelMap = FactPixelMapping.getInstance();


    /**
     * This function calculates the center of gravity for every slice. It uses only shower pixel.
     * It also calculates the variance and covariance of the center of gravity.
     */

    @Override
    public Data process(Data input) {
        /// init helper and utils
//      mpGeomXCoord =  DefaultPixelMapping.getGeomXArray();
//      mpGeomYCoord =  DefaultPixelMapping.getGeomYArray();

        // check keys
        Utils.mapContainsKeys(input, showerPixelKey, dataCalibratedKey);
        Utils.isKeyValid(input, "NROI", Integer.class);
        int sliceCount = (int) input.get("NROI");

        Utils.isKeyValid(input, showerPixelKey, PixelSet.class);
        PixelSet showerPixelArray = (PixelSet) input.get(showerPixelKey);

        Utils.isKeyValid(input, dataCalibratedKey, double[].class);
        double[] dataCalibratedArray = (double[]) input.get(dataCalibratedKey);

        if (showerPixelArray.size() < numberOfShowerPixelThreshold) {
            input.put(outputKey + "_X", null);
            input.put(outputKey + "_Y", null);

            input.put(outputKey + "_VarX", null);
            input.put(outputKey + "_VarY", null);
            input.put(outputKey + "_CovXY", null);

            input.put(outputKey + "_VelX", null);
            input.put(outputKey + "_VelY", null);

            input.put(outputKey + "_Vel", null);
            input.put(outputKey + "_VelErr", null);

            input.put(outputKey + "_MinVel", Double.NaN);
            input.put(outputKey + "_MinVelId", Double.NaN);
            input.put(outputKey + "_MinVelError", Double.NaN);

            input.put(outputKey + "_MaxVel", Double.NaN);
            input.put(outputKey + "_MaxVelId", Double.NaN);
            input.put(outputKey + "_MaxVelError", Double.NaN);

            input.put(outputKey + "_BestVel", Double.NaN);
            input.put(outputKey + "_BestVelError", Double.NaN);
            input.put(outputKey + "_BestVelId", Double.NaN);
            return input;
        }

        /// init internal parameter

        // COG for every slice

        double[] cogx = new double[sliceCount];
        double[] cogy = new double[sliceCount];

        double[] varcogx = new double[sliceCount];
        double[] varcogy = new double[sliceCount];
        double[] covcog = new double[sliceCount];

        double[] cogVelocityX = new double[sliceCount - 1];
        double[] cogVelocityY = new double[sliceCount - 1];
        double[] cogVelocity = new double[sliceCount - 1];
        double[] cogVelocityXError = new double[sliceCount - 1];
        double[] cogVelocityYError = new double[sliceCount - 1];
        double[] cogVelocityError = new double[sliceCount - 1];
        double[] size = new double[sliceCount];

        double minimalVelocity = Double.MAX_VALUE; // minimal velocity of all slices
        int minimalVelocityId = 0;
        double maximalVelocity = Double.MIN_VALUE; // maximal velocity of all slices
        int maximalVelocityId = 0;
        double bestVelocity = 0; // velocity with minimal "error"
        int bestVelocityId = 0;
        double bestVelocityError = Double.MAX_VALUE; // the corresponding "error"


        // Baseline correction
        double eventBaseline = 0.0;
        for (CameraPixel pixel : showerPixelArray) {
            for (int slice = 0; slice < sliceCount; slice++) {
                eventBaseline = eventBaseline > dataCalibratedArray[pixel.id * sliceCount + slice] ? dataCalibratedArray[pixel.id * sliceCount + slice] : eventBaseline;
            }
        }
        eventBaseline = eventBaseline > 0 ? eventBaseline : -eventBaseline;

        for (int slice = 0; slice < sliceCount; slice++) {
            size[slice] = 0;
            cogx[slice] = 0;
            cogy[slice] = 0;
            varcogx[slice] = 0;
            varcogy[slice] = 0;
            covcog[slice] = 0;

            // Calculate COGs
            for (CameraPixel pixel : showerPixelArray) {
                int chid = pixel.id;
                //TODO insert rotate by hillas_delta switch
                double posx = pixel.getXPositionInMM();
                double posy = pixel.getXPositionInMM();
                size[slice] += dataCalibratedArray[chid * sliceCount + slice] + eventBaseline;
                cogx[slice] += (dataCalibratedArray[chid * sliceCount + slice] + eventBaseline) * posx;
                cogy[slice] += (dataCalibratedArray[chid * sliceCount + slice] + eventBaseline) * posy;

            }
            cogx[slice] /= size[slice];
            cogy[slice] /= size[slice];

            // Calculate variance and covariance
            for (CameraPixel pixel : showerPixelArray) {
                int chid = pixel.id;
                double posx = pixel.getXPositionInMM();
                double posy = pixel.getXPositionInMM();
                varcogx[slice] += (dataCalibratedArray[chid * sliceCount + slice] + eventBaseline) * (posx - cogx[slice]) * (posx - cogx[slice]);
                varcogy[slice] += (dataCalibratedArray[chid * sliceCount + slice] + eventBaseline) * (posy - cogy[slice]) * (posy - cogy[slice]);
                covcog[slice] += (dataCalibratedArray[chid * sliceCount + slice] + eventBaseline) * (posx - cogx[slice]) * (posy - cogy[slice]);
            }
            varcogx[slice] /= size[slice];
            varcogy[slice] /= size[slice];
            covcog[slice] /= size[slice];

            // Calculate velocities on the fly
            if (slice > 0) {
                cogVelocityX[slice - 1] = (cogx[slice] - cogx[slice - 1]) / 0.5f;
                cogVelocityY[slice - 1] = (cogy[slice] - cogy[slice - 1]) / 0.5f;
                cogVelocity[slice - 1] = (double) Math.sqrt(cogVelocityX[slice - 1] * cogVelocityX[slice - 1] + cogVelocityY[slice - 1] * cogVelocityY[slice - 1]);
                cogVelocityXError[slice - 1] = 2.0 * (double) Math.sqrt(varcogx[slice] * varcogx[slice] + varcogx[slice - 1] * varcogx[slice - 1]);
                cogVelocityYError[slice - 1] = 2.0 * (double) Math.sqrt(varcogy[slice] * varcogy[slice] + varcogy[slice - 1] * varcogy[slice - 1]);
                // here i will define a better quality parameter
                /*cogVelocityError[slice - 1] = Math.sqrt((cogVelocityX[slice - 1] * cogVelocityX[slice - 1] *
                                                        cogVelocityXError[slice - 1] * cogVelocityXError[slice - 1] +
                                                        cogVelocityY[slice - 1] * cogVelocityY[slice - 1] *
                                                        cogVelocityYError[slice - 1] * cogVelocityYError[slice - 1] ) /
                                                        (cogVelocityX[slice - 1] * cogVelocityX[slice - 1] + cogVelocityY[slice - 1] * cogVelocityY[slice - 1]) );
                 */
                // common term
                double ct = 1.0 / Math.sqrt(Math.pow(cogx[slice] + cogx[slice - 1], 2.0) + Math.pow(cogy[slice] + cogy[slice - 1], 2.0));
                double ct2 = Math.pow(ct, 2.0);
                double xdiff2 = Math.pow(cogx[slice] - cogx[slice - 1], 2.0);
                double ydiff2 = Math.pow(cogy[slice] - cogy[slice - 1], 2.0);
                double f13 = ct2 * (cogx[slice] - cogx[slice - 1]) * (cogy[slice] - cogy[slice - 1]) * covcog[slice];
                double f24 = ct2 * (cogx[slice] - cogx[slice - 1]) * (cogy[slice] - cogy[slice - 1]) * covcog[slice - 1];
                cogVelocityError[slice - 1] = Math.sqrt(ct2 * varcogx[slice] * xdiff2 +
                        ct2 * varcogx[slice - 1] * xdiff2 +
                        ct2 * varcogy[slice] * ydiff2 +
                        ct2 * varcogy[slice - 1] * ydiff2 +
                        2 * f13 + 2 * f24
                );

                if (cogVelocity[slice - 1] < minimalVelocity) {
                    minimalVelocity = cogVelocity[slice - 1];
                    minimalVelocityId = slice - 1;
                }
                if (cogVelocity[slice - 1] > maximalVelocity) {
                    maximalVelocity = cogVelocity[slice - 1];
                    maximalVelocityId = slice - 1;
                }
                if (cogVelocityError[slice - 1] < bestVelocityError) {
                    bestVelocityError = cogVelocityError[slice - 1];
                    bestVelocity = cogVelocity[slice - 1];
                    bestVelocityId = slice - 1;
                }

            }
        }


        input.put(outputKey + "_X", cogx);
        input.put(outputKey + "_Y", cogy);

        input.put(outputKey + "_VarX", varcogx);
        input.put(outputKey + "_VarY", varcogy);
        input.put(outputKey + "_CovXY", covcog);

        input.put(outputKey + "_VelX", cogVelocityX);
        input.put(outputKey + "_VelY", cogVelocityY);

        input.put(outputKey + "_Vel", cogVelocity);
        input.put(outputKey + "_VelErr", cogVelocityError);

        input.put(outputKey + "_MinVel", minimalVelocity);
        input.put(outputKey + "_MinVelId", minimalVelocityId);
        input.put(outputKey + "_MinVelError", cogVelocityError[minimalVelocityId]);

        input.put(outputKey + "_MaxVel", maximalVelocity);
        input.put(outputKey + "_MaxVelId", maximalVelocityId);
        input.put(outputKey + "_MaxVelError", cogVelocityError[maximalVelocityId]);

        input.put(outputKey + "_BestVel", bestVelocity);
        input.put(outputKey + "_BestVelError", bestVelocityError);
        input.put(outputKey + "_BestVelId", bestVelocityId);

        return input;
    }
}
