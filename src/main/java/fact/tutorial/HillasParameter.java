package fact.tutorial;

import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.EllipseOverlay;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import stream.Data;
import stream.Processor;

import java.util.HashSet;

/**
 * Created by kai on 15.07.15.
 */
public class HillasParameter implements Processor {

    FactPixelMapping pixelMapping = FactPixelMapping.getInstance();

    @Override
    public Data process(Data item) {
        HashSet<Integer> showerPixelSet= (HashSet<Integer>) item.get("showerPixel");

        int[] showerPixel = new int[showerPixelSet.size()];

        int c = 0;
        for (int pixel : showerPixelSet){
            showerPixel[c] = pixel;
            c++;
        }

        double[] photons = (double[]) item.get("photons");

        double[] weights = new double[showerPixel.length];
        double size = 0;
        for (int i = 0; i < showerPixel.length; i++) {
            weights[i] = photons[showerPixel[i]];
            size += weights[i];
        }

        double[] cog = calculateCog(weights, showerPixel, size);

        // Calculate the weighted Empirical variance along the x and y axis.
        RealMatrix covarianceMatrix = calculateCovarianceMatrix(showerPixel, weights, cog);

        // get the eigenvalues and eigenvectors of the matrix and weigh them accordingly.
        EigenDecomposition eig = new EigenDecomposition(covarianceMatrix);

        // turns out the eigenvalues describe the variance in the eigenbasis of
        // the covariance matrix
        double varianceLong = eig.getRealEigenvalue(0) / size;
        double varianceTrans = eig.getRealEigenvalue(1) / size;

        double length = Math.sqrt(varianceLong);
        double width = Math.sqrt(varianceTrans);

        double delta = calculateDelta(eig);



        item.put("@ellipseOverlay", new EllipseOverlay(cog[0], cog[1], 2*width, 2*length, delta));
        item.put("hillas:width", width);
        item.put("hillas:length", length);
        item.put("hillas:size", size);
        item.put("hillas:delta", size);
        return item;
    }


    public double[] calculateCog(double[] weights, int[] showerPixel,
                                 double size) {

        double[] cog = { 0, 0 };
        // find weighted center of the shower pixels.
        int i = 0;
        for (int pix : showerPixel) {
            cog[0] += weights[i] * pixelMapping.getPixelFromId(pix).getXPositionInMM();
            cog[1] += weights[i] * pixelMapping.getPixelFromId(pix).getYPositionInMM();
            i++;
        }
        cog[0] /= size;
        cog[1] /= size;
        return cog;
    }

    public RealMatrix calculateCovarianceMatrix(int[] showerPixel, double[] showerWeights, double[] cog) {
        double variance_xx = 0;
        double variance_yy = 0;
        double covariance_xy = 0;
        int i = 0;
        for (int pix : showerPixel) {
            double weight = showerWeights[i];
            double posx = pixelMapping.getPixelFromId(pix).getXPositionInMM();
            double posy = pixelMapping.getPixelFromId(pix).getYPositionInMM();

            variance_xx += weight * Math.pow(posx - cog[0], 2);
            variance_yy += weight * Math.pow(posy - cog[1], 2);
            covariance_xy += weight * (posx - cog[0]) * (posy - cog[1]);
            i++;
        }

        double[][] matrixData = { { variance_xx, covariance_xy },
                { covariance_xy, variance_yy } };
        return MatrixUtils.createRealMatrix(matrixData);
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

}
