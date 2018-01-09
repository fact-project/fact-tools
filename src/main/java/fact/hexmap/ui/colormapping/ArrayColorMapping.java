package fact.hexmap.ui.colormapping;

import java.awt.*;

import static java.lang.Math.round;

/**
 * Uses an array of RGB values for the colorMapping.
 * BaseClasses must override the getColorFromIndexMethod
 * Created by maxnoe on 04.02.16.
 */
public abstract class ArrayColorMapping implements ColorMapping {
    @Override
    public Color getColorFromValue(double v, double minValue, double maxValue) {
        if (Double.isNaN(v)) {
            return getNaNColor();
        }
        if (v == Double.POSITIVE_INFINITY) {
            return getOverflowColor();
        }
        if (v == Double.NEGATIVE_INFINITY) {
            return getUnderflowColor();
        }

        double value = (v - minValue) / (maxValue - minValue);
        int index = (int) round(255 * value);
        if (index < 0) {
            return getUnderflowColor();
        }
        if (index > 255) {
            return getOverflowColor();
        }
        return getColorFromIndex(index);
    }

    protected Color getNaNColor() {
        return Color.GRAY;
    }

    protected Color getOverflowColor() {
        return Color.GRAY;
    }

    protected Color getUnderflowColor() {
        return Color.GRAY;
    }

    public abstract Color getColorFromIndex(int index);
}
