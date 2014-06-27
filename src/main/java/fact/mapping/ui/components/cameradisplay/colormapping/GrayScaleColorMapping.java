package fact.mapping.ui.components.cameradisplay.colormapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;


/**
 * Map values to grayscale by linear interpolation from minimal Value in camera to maximum value in camera.
 * @author kai
 */
public class GrayScaleColorMapping
        implements ColorMapping
{
    static Logger log = LoggerFactory.getLogger( GrayScaleColorMapping.class );
    double neutralValue = 0.0f;
    Color maxColor = Color.white;
    Color minColor = Color.black;
    double minValue, maxValue;

    @Override
    public Color getColorFromValue(double v, double minValue, double maxValue) {
        if(Double.isNaN(v)){
            v = neutralValue;
        }
        double value = v + Math.abs(minValue);
        value = value/(maxValue-minValue);
        return Color.getHSBColor(0.0f, 0.0f, (float)value);

    }
}