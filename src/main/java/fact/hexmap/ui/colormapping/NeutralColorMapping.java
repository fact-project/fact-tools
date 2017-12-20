package fact.hexmap.ui.colormapping;

import java.awt.*;


/**
 * Map values to color by linear interpolation from neutralColor to max/minColor.
 */
public class NeutralColorMapping implements ColorMapping {
    double neutralValue = 0.0f;
    Color maxColor = Color.yellow;
    Color minColor = Color.blue;
    Color neutralColor = Color.black;
    private float[] neutralHsbVals = new float[3];
    private float[] maxHsbVals = new float[3];
    private float[] minHsbVals = new float[3];


    public NeutralColorMapping() {
        Color.RGBtoHSB(neutralColor.getRed(), neutralColor.getGreen(), neutralColor.getBlue(), neutralHsbVals);
        Color.RGBtoHSB(maxColor.getRed(), maxColor.getGreen(), maxColor.getBlue(), maxHsbVals);
        Color.RGBtoHSB(minColor.getRed(), minColor.getGreen(), minColor.getBlue(), minHsbVals);
    }


    @Override
    public Color getColorFromValue(double value, double minValue, double maxValue) {

        if (value == Double.NaN) {
            value = neutralValue;
        }

        //log.info( "Scaling {} with {}", v, scale );
        if (value >= neutralValue) {
            //getColorFromValue to positive color
            value = Math.abs(value / (maxValue - neutralValue));
            float hue = (float) (neutralHsbVals[0] + (maxHsbVals[0] - neutralHsbVals[0]) * value);
            float sat = (float) (neutralHsbVals[1] + (maxHsbVals[1] - neutralHsbVals[1]) * value);
            float bri = (float) (neutralHsbVals[2] + (maxHsbVals[2] - neutralHsbVals[2]) * value);
            return Color.getHSBColor(hue, sat, bri);
        } else {
            //getColorFromValue to negative color
            value = Math.abs(value / (neutralValue - minValue));
            float hue = (float) (neutralHsbVals[0] + (minHsbVals[0] - neutralHsbVals[0]) * value);
            float sat = (float) (neutralHsbVals[1] + (minHsbVals[1] - neutralHsbVals[1]) * value);
            float bri = (float) (neutralHsbVals[2] + (minHsbVals[2] - neutralHsbVals[2]) * value);
            return Color.getHSBColor(hue, sat, bri);
        }

    }
}
