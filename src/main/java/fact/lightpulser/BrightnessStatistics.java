package fact.lightpulser;

import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;

/**
 * Created by lena on 02.07.15.
 */
public class BrightnessStatistics implements Processor{
    FactPixelMapping map = FactPixelMapping.getInstance();
    @Override
    public Data process(Data data) {
        int roi = (Integer) data.get("NROI");
        double[] data_neu = (double[]) data.get("DataCalibrated");

        double summe_pixel = 0;
        for (FactCameraPixel p : map.pixelArray){
            double[] pixelData = p.getPixelData(data_neu, roi);



            for(int i=25; i<250; i++){

                summe_pixel = summe_pixel+pixelData[i]/225.0;

            }

        }
        double mittelwert = summe_pixel/1440.0;

        data.put("Brightness",mittelwert);

        return data;
    }
}
