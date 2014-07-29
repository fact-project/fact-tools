package fact.mapping.ui.colormapping;

import java.awt.*;


/**
 * Map values to color by linear interpolation from neutralColor to max/minColor.
 *
 */
public class NeutralColorMapping  implements ColorMapping{
    double neutralValue = 0.0f;
    Color maxColor = Color.yellow;
    Color minColor = Color.blue;
    Color neutralColor = Color.black;
    private	float[] neutralHsbVals = new float[3];
    private float[] maxHsbVals = new float[3];
    private float[] minHsbVals = new float[3];



    public NeutralColorMapping(){
        Color.RGBtoHSB(neutralColor.getRed(), neutralColor.getGreen(), neutralColor.getBlue(), neutralHsbVals);
        Color.RGBtoHSB(maxColor.getRed(), maxColor.getGreen(), maxColor.getBlue(), maxHsbVals);
        Color.RGBtoHSB(minColor.getRed(),minColor.getGreen(), minColor.getBlue(), minHsbVals);
    }


    @Override
    public Color getColorFromValue(double v, double minValue, double maxValue) {
        Double value = new Double(v);
        if(value.equals(Double.NaN)){
            value = neutralValue;
        }

        //log.info( "Scaling {} with {}", v, scale );
        if (v >= neutralValue){
            //getColorFromValue to positive color
            value = (double) Math.abs( v/(maxValue-neutralValue) );
            float hue = (float) (neutralHsbVals[0] + (maxHsbVals[0] - neutralHsbVals[0])*value);
            float sat = (float) (neutralHsbVals[1] + (maxHsbVals[1] - neutralHsbVals[1])*value);
            float bri = (float) (neutralHsbVals[2] + (maxHsbVals[2] - neutralHsbVals[2])*value);
            return Color.getHSBColor(hue, sat, bri);
        } else {
            //getColorFromValue to negative color
            value = (double) Math.abs( v/(neutralValue-minValue) );
            float hue = (float) (neutralHsbVals[0] + (minHsbVals[0] - neutralHsbVals[0])*value);
            float sat = (float) (neutralHsbVals[1] + (minHsbVals[1] - neutralHsbVals[1])*value);
            float bri = (float) (neutralHsbVals[2] + (minHsbVals[2] - neutralHsbVals[2])*value);
            return Color.getHSBColor(hue, sat, bri);
        }

    }
}