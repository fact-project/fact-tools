package fact.hexmap.ui.colormapping;

import java.awt.*;

/**
 * Colormap minimal value: black, maximal value white
 * Created by lena on 30.06.15.
 */
public class HotColorMap implements ColorMapping {


    @Override
    public Color getColorFromValue(double value, double minValue, double maxValue) {
        //Color hot[] = new Color[]{Color.black, Color.red, new Color(255, 128, 0), Color.yellow, Color.white};

        if(Double.isNaN(value)){
            return Color.GREEN;
        }

        double l1 = (maxValue - minValue)/3.;
        int n1 = (int) ((value - minValue)/l1);

        double l2 = l1/255.;
        int n2 = (int) ((value - (minValue + n1*l1))/l2);

        //int n2 = 55;

        if(n1 == 0){
            return new Color(n2, 0 ,0);
        }
        if(n1 == 1){
            return new Color(255, n2, 0);
        }
        if(n1 == 2){
            return new Color(255, 255, n2);
        }
        else{
            return new Color(255, 255, 255);
        }

    }
}


