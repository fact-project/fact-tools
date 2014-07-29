#Creating custom color maps for the viewer

So far there are just a few basic color mapping schemes implemented. To change the color mapping of the current
camera display just right click on the image to open up a context menu where you can choose from different 
color maps.

<div id="textimg">
   <img src="../images/colormap.png" style="width:300px;" />
</div>

You can easily add your own colormap by simply implementing the 
[ColorMapping Interface](../apidocs/fact/mapping/ui/colormapping/ColorMapping.html). You simply add 
your own class somewhere in the project ( you're encouraged to use the *fact/mapping/ui/colormapping/* package )
and the next time you start the viewer it will be automatically added to the context menu in the FACT viewer GUI.
Below is a simple example of a class implementing the interface making everything yellow.
Keep in mind that the passed value for a pixel can be a NaN.

    public class GrayScaleColorMapping
            implements ColorMapping
    {
        @Override
        public Color getColorFromValue(double value, double minValue, double maxValue) {
            if(Double.isNaN(value)){
                return Color.GRAY;
            }
            return Color.YELLOW;
        }
    }