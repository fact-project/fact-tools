package fact.filter;

import fact.hexmap.FactCameraPixel;

public class GeometricalConvolutionNextNeighboursMean extends GeometricalConvolution {

    protected double getConvolution(int pix, int slice) {
        // 2D Mean Convolution Kernel using only next neighbours:
        //         ___
        //     ___/1/7\___
        //    /1/7\___/1/7\
        //    \___/1/7\___/
        //    /1/7\___/1/7\
        //    \___/1/7\___/
        //        \___/
        
        double buf = data[pixAndSlice2ArrayIndex(pix,slice)];
        FactCameraPixel[] neighbours = pixelMap.getNeighboursFromID(pix);

        for(FactCameraPixel neigbour : neighbours) {
           buf += data[ pixAndSlice2ArrayIndex(neigbour.chid,slice) ];
        }

        return buf / (neighbours.length + 1);        
    }
}