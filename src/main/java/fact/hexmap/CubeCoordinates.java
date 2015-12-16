package fact.hexmap;

import stream.Data;
import stream.Processor;

/**
 * Created by lena on 03.12.15.
 */
public class CubeCoordinates implements Processor {
    FactPixelMapping mapping = FactPixelMapping.getInstance();

    @Override
    public Data process(Data input) {

        FactCameraPixel p = mapping.getPixelFromOffsetCoordinates(-18,-12);

        System.out.println(3%2);





        return null;
    }
}
