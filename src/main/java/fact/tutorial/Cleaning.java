package fact.tutorial;

import fact.container.PixelSetOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.HashSet;

/**
 * Created by kai on 15.07.15.
 */
public class Cleaning implements Processor {

    @Parameter(required = false, description = "The threshold for selecting pixels. Every pixel with a value > threshold" +
            "will be selected")
    private double threshold = 7.5;



    @Override
    public Data process(Data item) {

        double[] photons = (double[]) item.get("photons");
        HashSet<Integer> selectedPixel = new HashSet<>();

        for (int pixel = 0; pixel < 1440; pixel++) {
            if(photons[pixel] > threshold){
                selectedPixel.add(pixel);
            }
        }

        item.put("showerPixel", selectedPixel);
        item.put("numberOfShowerPixel", selectedPixel.size());
        item.put("@showerPixelOverlay", new PixelSetOverlay(selectedPixel));
        return item;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
