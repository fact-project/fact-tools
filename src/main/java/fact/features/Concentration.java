package fact.features;

import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Concentration implements Processor {
    static Logger log = LoggerFactory.getLogger(Concentration.class);

    @Parameter(required = true)
    public String pixelSetKey;

    @Parameter(required = true)
    public String weights;

    @Parameter(required = true)
    public String concOneOutputKey;

    @Parameter(required = true)
    public String concTwoOutputKey;

    @Override
    public Data process(Data item) {

        PixelSet showerPixel;
        double[] photonCharge;
        try {
            showerPixel = (PixelSet) item.get(pixelSetKey);
            photonCharge = (double[]) item.get(weights);
        } catch (ClassCastException e) {
            log.error("Could  not cast the keys to the right types");
            throw e;
        }
        if (showerPixel == null || showerPixel.set.size() == 0) {
            log.warn("No shower in event. not calculating conenctration");
            return item;
        }


        //concentration according to F.Temme
        double max_photon_charge = 0;
        double second_max_photon_charge = 0;

        double size = 0;

        for (CameraPixel pix : showerPixel.set) {
            size += photonCharge[pix.id];
            if (photonCharge[pix.id] > max_photon_charge) {
                second_max_photon_charge = max_photon_charge;
                max_photon_charge = photonCharge[pix.id];
            } else if (photonCharge[pix.id] > second_max_photon_charge) {
                second_max_photon_charge = photonCharge[pix.id];
            }

        }

        item.put(concOneOutputKey, max_photon_charge / size);
        item.put(concTwoOutputKey, (max_photon_charge + second_max_photon_charge) / size);
        return item;
    }
}
