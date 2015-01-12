package fact.filter;

import fact.hexmap.FactCameraPixel;

public class GeometricalConvolutionNextNeighboursBell extends GeometricalConvolution {

    protected double getConvolution(int pix, int slice) {
        // 2D Bell Convolution Kernel using only next neighbours:
        //         ___
        //     ___/ 1 \___
        //    / 1 \___/ 1 \
        //    \___/ 6 \___/  __1_
        //    / 1 \___/ 1 \   12
        //    \___/ 1 \___/
        //        \___/

        final double centerToNeighbourRatio = 6;

        double buf = data[pixAndSlice2ArrayIndex(pix,slice)] * centerToNeighbourRatio;
        FactCameraPixel[] neighbours = pixelMap.getNeighboursFromID(pix);

        for(FactCameraPixel neigbour : neighbours) {
           buf += data[ pixAndSlice2ArrayIndex(neigbour.chid,slice) ];
        }

        return buf / (neighbours.length + centerToNeighbourRatio);        
    }
}