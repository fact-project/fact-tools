package fact.lightpulser;

import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import stream.Processor;

/**
 * Created by lena on 02.07.15.
 * integrates Lightpulser events and defines arrival time as mean value
 * creates a file with puls arrival times for each pixel
 */
public class BrightnessStatistics implements Processor{
    FactPixelMapping map = FactPixelMapping.getInstance();
    @Override
    public Data process(Data data) {
        int roi = (Integer) data.get("NROI");
        int npix = (Integer) data.get("NPIX");

        double[] data_neu = (double[]) data.get("DataCalibrated");
        int[] arrivaltime_pixel = new int[npix];
        double [] amplitude_pixel = new double[npix];
        double[] brightness_pixel = new double[npix];
        //IntervalMarker[] m = new IntervalMarker[npix];

        double brightness_mean = 0;
        double arrivaltime_mean = 0;
        double amplitude_mean = 0;

        int k = 0;

        for (FactCameraPixel p : map.pixelArray){

            double[] pixelData = p.getPixelData(data_neu, roi);

            double current_max = 0;
            arrivaltime_pixel[k] = 0;
            amplitude_pixel[k] = 0;
            brightness_pixel[k] = 0;

            /*PulsArrivalTime (max gradient)*/
            for(int i=25; i<100; i++){
                double temp = pixelData[i] - pixelData[i-1];
                if(temp > current_max){
                    current_max = temp;
                    arrivaltime_pixel[k] = i;
                }
            }


            for(int i=arrivaltime_pixel[k]; i<250; i++){
                if(pixelData[i] > amplitude_pixel[k]) {
                    amplitude_pixel[k] = pixelData[i];
                }

                brightness_pixel[k] = brightness_pixel[k] + pixelData[i]/(225.0-arrivaltime_pixel[k]);

            }

           // m[k] = new IntervalMarker(arrivaltime_pixel[k], arrivaltime_pixel[k] + 1);
            arrivaltime_mean = arrivaltime_mean + arrivaltime_pixel[k]/1440.0;
            brightness_mean = brightness_mean + brightness_pixel[k]/1440.0;
            amplitude_mean = amplitude_mean + amplitude_pixel[k]/1440.0;

            k++;
        }

       /* data.put("Brightness", brightness_mean);
        data.put("ArrivalTime", arrivaltime_mean);
        data.put("Amplitude", amplitude_mean);*/
        data.put("ArrivalTimePixel", arrivaltime_pixel);
        data.put("BrightnessPixel", brightness_pixel);
        data.put("AmplitudePixel", amplitude_pixel);
        //data.put("Marker", m);

        return data;
    }
}
