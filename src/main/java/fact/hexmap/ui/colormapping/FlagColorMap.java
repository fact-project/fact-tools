package fact.hexmap.ui.colormapping;

import java.awt.*;

/**
 * Created by lena on 24.11.15.
 */
public class FlagColorMap implements ColorMapping {

    @Override
    public Color getColorFromValue(double value, double minValue, double maxValue) {

        if (Double.isNaN(value)) {
            return Color.GREEN;
        }

        double l1 = (maxValue - minValue) / 100.;
        int n1 = (int) ((value - minValue));///l1);

        int l2 = (int) n1 % 7;
        //int n2 = (int) ((value - (minValue + n1*l1))/l2);

        //int n2 = 55;

        if (l2 == 0) {
            return Color.RED;
        } else if (l2 == 1) {
            return Color.BLUE;
        } else if (l2 == 2) {
            return Color.WHITE;
        } else if (l2 == 3) {
            return Color.MAGENTA;
        } else if (l2 == 4) {
            return Color.CYAN;
        } else if (l2 == 5) {
            return Color.ORANGE;
        } else {
            return Color.DARK_GRAY;
        }

    }
}
