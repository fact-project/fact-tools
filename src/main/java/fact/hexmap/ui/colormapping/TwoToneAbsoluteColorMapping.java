package fact.hexmap.ui.colormapping;


import java.awt.*;


/**
 * @author kai
 */
public class TwoToneAbsoluteColorMapping implements ColorMapping {

    Double neutralValue = 0.0d;
    Color maxColor = Color.yellow;
    Color minColor = Color.green;
    Color neutralColor = Color.black;
    private float[] neutralHsbVals = new float[3];
    private float[] maxHsbVals = new float[3];
    private float[] minHsbVals = new float[3];

    public TwoToneAbsoluteColorMapping() {
        Color.RGBtoHSB(neutralColor.getRed(), neutralColor.getGreen(), neutralColor.getBlue(), neutralHsbVals);
        Color.RGBtoHSB(maxColor.getRed(), maxColor.getGreen(), maxColor.getBlue(), maxHsbVals);
        Color.RGBtoHSB(minColor.getRed(), minColor.getGreen(), minColor.getBlue(), minHsbVals);
    }


    @Override
    public Color getColorFromValue(double value, double minValue, double maxValue) {
        if (Double.isNaN(value)) {
            value = 0.0f;
        }
        if (value >= maxValue) {
            return Color.red;
        }
        if (value <= minValue) {
            return Color.blue;
        }

        //log.info( "Scaling {} with {}", v, scale );
        if (value >= neutralValue) {
            //getColorFromValue to positive color
            value = (float) Math.abs(value / (maxValue - neutralValue));
            float hue = (float) (neutralHsbVals[0] + maxHsbVals[0]);
            float sat = (float) (neutralHsbVals[1] + (maxHsbVals[1] - neutralHsbVals[1]) * value);
            float bri = (float) (neutralHsbVals[2] + (maxHsbVals[2] - neutralHsbVals[2]) * value);
            return Color.getHSBColor(hue, sat, bri);
        } else {
            //getColorFromValue to negative color. hue should be Constant
            value = (float) Math.abs(value / (neutralValue - minValue));
            float hue = (float) (neutralHsbVals[0] + minHsbVals[0]);
            float sat = (float) (neutralHsbVals[1] + (minHsbVals[1] - neutralHsbVals[1]) * value);
            float bri = (float) (neutralHsbVals[2] + (minHsbVals[2] - neutralHsbVals[2]) * value);
            return Color.getHSBColor(hue, sat, bri);
        }
    }

}
