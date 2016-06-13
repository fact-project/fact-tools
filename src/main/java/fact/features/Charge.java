 package fact.features;

import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.hexmap.ui.overlays.LineOverlay;

/**
 * Created by Thomas Jung (thomas3.jung@tu-dortmund.de) on 13.06.2016.
 */
public class Charge implements Processor {
    /*
     * This Process calculate the ID with the Max charge
     * and calculate the new Mainachse with a Line form the ID to the Point
     * which is the most distant Point.
     */
    static Logger log = LoggerFactory.getLogger(Charge.class);

    @Parameter(required = true)
    private String pixelSetKey;
    @Parameter(required = true)
    private String weights;
    @Parameter(required = true)
    private String ChargeIDOutputKey;
    @Parameter(required = true)
    private String ChargmaxOutputKey;
    @Parameter(required = true)
    private String NewAngleOutputKey;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys( input, pixelSetKey, weights);

        PixelSet showerPixel = (PixelSet) input.get(pixelSetKey);
        double[] photonCharge = (double[]) input.get(weights);

        int chargeID = 0;
        double chargemax = -10;


        for (CameraPixel pix: showerPixel.set){
            if (photonCharge[pix.id] > chargemax) {
                chargeID = chargeID - chargeID + pix.id;
                chargemax = chargemax - chargemax + photonCharge[pix.id];
            }
        }

        double x_pos_max = getx(chargeID);
        double y_pos_max = gety(chargeID);
        double differenz_max = 0;
        double differenz_min = 200000000;
        int id_max_ab = 0;
        int id_min_ab = 0;
        double x_diff = 0;
        double y_diff = 0;
        double differen = 0;
        for (CameraPixel pix: showerPixel.set) {
            int anzahl_neigh = 0;
            FactCameraPixel[] pixelsetneigh = pixelMap.getNeighboursFromID(pix.id);
            for (CameraPixel pix_zahl: pixelsetneigh){
                if (java.util.Arrays.toString(showerPixel.toIntArray()).contains(Integer.toString(pix_zahl.id))){
                    anzahl_neigh += 1;
                }
            }

            if (anzahl_neigh < 6){
                x_diff = (x_pos_max - getx(pix.id));
                y_diff = (y_pos_max - gety(pix.id));
                differen = (x_diff * x_diff + y_diff * y_diff);
                if (differen > differenz_max){
                    differenz_max = differen;
                    id_max_ab = pix.id;
                }

                if (differen < differenz_min){
                    differenz_min = differen;
                    id_min_ab = pix.id;
                }
            }
        }
        double anglenew = Math.atan((x_pos_max - getx(id_max_ab)) / (y_pos_max - gety(id_max_ab)));
        input.put("Linemax", new LineOverlay(x_pos_max, y_pos_max, getx(id_max_ab), gety(id_max_ab)));
        input.put("Linemin", new LineOverlay(x_pos_max, y_pos_max, getx(id_min_ab), gety(id_min_ab)));
        input.put(ChargeIDOutputKey , (double)  chargeID);
        input.put(ChargmaxOutputKey , chargemax);
        input.put(NewAngleOutputKey , anglenew);
        return input;

    }

    double getx (int id){
        return pixelMap.getPixelFromId(id).getXPositionInMM();
    }

    double gety (int id){
        return pixelMap.getPixelFromId(id).getYPositionInMM();
    }
    public String getChargeIDOutputKey() {
        return ChargeIDOutputKey;
    }

    public void setChargeIDOutputKey(String ChargeIDOutputKey) {
        this.ChargeIDOutputKey = ChargeIDOutputKey;
    }

    public String getChargmaxOutputKey() {
        return ChargmaxOutputKey;
    }

    public void setChargmaxOutputKey(String ChargmaxOutputKey) {
        this.ChargmaxOutputKey = ChargmaxOutputKey;
    }

    public String getNewAngleOutputKey() {
        return NewAngleOutputKey;
    }

    public void setNewAngleOutputKey(String NewAngleOutputKey) {
        this.NewAngleOutputKey = NewAngleOutputKey;
    }

    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }

    public String getWeights() {
        return weights;
    }

    public void setWeights(String weights) {
        this.weights = weights;
    }
}
