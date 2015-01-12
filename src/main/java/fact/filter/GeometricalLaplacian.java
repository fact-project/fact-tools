package fact.filter;

import fact.Utils;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 *@author Sebastian Mueller &lt;sebmuell@phys.ethz.ch&gt;
 *
 */
public class GeometricalLaplacian implements Processor {
    static Logger log = LoggerFactory.getLogger(GeometricalLaplacian.class);

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Parameter (required = true, description = "The key to the double array to be convoluted with the laplacian kernel")
    String key;

    @Parameter (required = true, description = "The outputKey to which the laplace convoluted data will be written to the stream")
    String outputKey;
    
    private int npix;
    
    // 2D Laplacian convolution Kernel:
    //         ___
    //     ___/+1 \___
    //    /+1 \___/+1 \
    //    \___/-6 \___/
    //    /+1 \___/+1 \
    //    \___/+1 \___/
    //        \___/

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);
        Utils.isKeyValid(item, "NPIX", Integer.class);
        double[] data = (double[]) item.get(key);
        npix = (Integer) item.get("NPIX");

        int roi = data.length / npix;
        double[] smoothedData= new double[data.length];

        for(int slice = 0; slice < roi; slice++) {
            for(int pix = 0; pix < npix; pix++) {

                int pos = pix * roi + slice;

                FactCameraPixel[] neighbours = pixelMap.getNeighboursFromID(pix);
                smoothedData[pos] = -6.0*data[pos];

                for(int neighbour = 0; neighbour < neighbours.length; neighbour++) {
                    smoothedData[pos] += data[ (neighbours[neighbour].chid * roi) + slice];
                }
            }
        }

        item.put(outputKey, smoothedData);
        return item;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
