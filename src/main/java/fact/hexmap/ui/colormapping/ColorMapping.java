package fact.hexmap.ui.colormapping;


import java.awt.*;

/**
 * @author Kai
 */
public interface ColorMapping {

    /**
     * This returns a color based on the given value.
     *
     * @param value    the value you want to assign to a color
     * @param minValue the minimum value possible
     * @param maxValue the maximum value possible
     * @return a color depending on the value passed in.
     */
    public Color getColorFromValue(double value, double minValue, double maxValue);
}
