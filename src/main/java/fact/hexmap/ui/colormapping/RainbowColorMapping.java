package fact.hexmap.ui.colormapping;

import java.awt.*;

/**
 * Created by kaibrugge on 02.07.14.
 */
public class RainbowColorMapping implements ColorMapping {
    @Override
    public Color getColorFromValue(double value, double minValue, double maxValue) {
        float hue = (float) (value / (maxValue - minValue + 50));
        return Color.getHSBColor(hue + 0.6f, 0.85f, 0.8f);
    }
}
