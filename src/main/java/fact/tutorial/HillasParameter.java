//package fact.tutorial;
//
//import fact.Utils;
//import fact.hexmap.ui.overlays.EllipseOverlay;
//import org.apache.commons.math3.linear.EigenDecomposition;
//import org.apache.commons.math3.linear.RealMatrix;
//import stream.Data;
//import stream.Processor;
//
///**
// * Created by kai on 15.07.15.
// */
//public class HillasParameter implements Processor {
//
//    @Override
//    public Data process(Data item) {
//        int[] showerPixel = (int[]) item.get("showerPixel");
//        double[] showerWeights = createShowerWeights(showerPixel,
//                (double[]) input.get(weightsKey));
//
//        double size = 0;
//        for (double v : showerWeights) {
//            size += v;
//        }
//
//        double[] cog = calculateCog(showerWeights, showerPixel, size);
//
//        // Calculate the weighted Empirical variance along the x and y axis.
//        RealMatrix covarianceMatrix = calculateCovarianceMatrix(showerPixel,
//                showerWeights, cog);
//
//        // get the eigenvalues and eigenvectors of the matrix and weigh them
//        // accordingly.
//        EigenDecomposition eig = new EigenDecomposition(covarianceMatrix);
//        // turns out the eigenvalues describe the variance in the eigenbasis of
//        // the covariance matrix
//        double varianceLong = eig.getRealEigenvalue(0) / size;
//        double varianceTrans = eig.getRealEigenvalue(1) / size;
//
//        double length = Math.sqrt(varianceLong);
//        double width = Math.sqrt(varianceTrans);
//
//        double delta = calculateDelta(eig);
//
//        // Calculation of the showers statistical moments (Variance, Skewness, Kurtosis)
//        // Rotate the shower by the angle delta in order to have the ellipse
//        // main axis in parallel to the Camera-Coordinates X-Axis
//        // allocate variables for rotated coordinates
//        double[] longitudinalCoords = new double[showerPixel.length];
//        double[] transversalCoords = new double[showerPixel.length];
//
//        for (int i = 0; i < showerPixel.length; i++) {
//            // translate to center
//            double posx = pixelMap.getPixelFromId(showerPixel[i])
//                    .getXPositionInMM();
//            double posy = pixelMap.getPixelFromId(showerPixel[i])
//                    .getYPositionInMM();
//            // rotate
//            double[] c = Utils.transformToEllipseCoordinates(posx, posy,
//                    cog[0], cog[1], delta);
//
//            // fill array of new showerKey coordinates
//            longitudinalCoords[i] = c[0];
//            transversalCoords[i] = c[1];
//        }
//
//        // find max long coords
//        double maxLongCoord = 0;
//        double minLongCoord = 0;
//        for (double l : longitudinalCoords) {
//            maxLongCoord = Math.max(maxLongCoord, l);
//            minLongCoord = Math.min(minLongCoord, l);
//        }
//
//        double maxTransCoord = 0;
//        for (double l : transversalCoords) {
//            maxTransCoord = Math.max(maxTransCoord, l);
//        }
//
//
//        // double newLength = Math.sqrt(calculateMoment(2, 0,
//        // longitudinalCoords, showerWeights));
//        // double newWidth = Math.sqrt(calculateMoment(2, 0, transversalCoords,
//        // showerWeights));
//        //
//        // double meanLong = calculateMoment(1, 0, longitudinalCoords,
//        // showerWeights);
//        // double meanTrans = calculateMoment(1, 0, transversalCoords,
//        // showerWeights);
//
//        // System.out.println("Width: " + width + " newwidth: " + newWidth);
//        // System.out.println("Length: " + length + " newlength: " + newLength);
//        // System.out.println("Mean long, trans (should be 0): " + meanLong +
//        // ", " + meanTrans);
//
//        // double[][] rot = { {Math.cos(delta), -Math.sin(delta)},
//        // {Math.sin(delta),Math.cos(delta) }
//        // };
//
//        // RealMatrix rotMatrix = MatrixUtils.createRealMatrix(rot);
//        // double[] a = {0 , 20};
//        // RealVector v = MatrixUtils.createRealVector(a);
//        // RealVector cogV = MatrixUtils.createRealVector(cog);
//        // v = rotMatrix.operate(v);
//        // v = v.add(cogV);
//
//        // double[] thead = Utils.transformToEllipseCoordinates(maxLongCoord +
//        // cog[0], 0 + cog[1], cog[0], cog[1], delta );
//        // double[] ttail = Utils.transformToEllipseCoordinates(minLongCoord +
//        // cog[0], 0 + cog[1], cog[0], cog[1], delta );
//        //
//        // double[] tMaxTrans = Utils.transformToEllipseCoordinates(0 + cog[0],
//        // maxTransCoord + cog[1], cog[0], cog[1], delta );
//
//        double[] center = calculateCenter(showerPixel);
//        input.put("Ellipse", new EllipseOverlay(center[0], center[1], width,
//                length, delta));
//
//        // look at what i found
//
//        // V=cov(x,y);
//        // [vec,val]=eig(V);
//        // angles=atan2( vec(2,:),vec(1,:) );
//
//        return input;
//    }
//}
