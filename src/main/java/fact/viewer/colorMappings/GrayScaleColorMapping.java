/**
 * 
 */
package fact.viewer.colorMappings;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	/**
	 * @see fact.viewer.colorMappings.ColorMapping#map(java.lang.Double)
	 */
	@Override
	public Color map(double v, double minValue, double maxValue) {
		if(Double.isNaN(v)){
			v = neutralValue;
		}
			double value = v + Math.abs(minValue);
			value = value/(maxValue-minValue);
			return Color.getHSBColor(0.0f, 0.0f, (float)value);
		
	}
	

//	@Override
//	public Color map(double value) {
//		if(this.maxValue !=  0.0){
//			map(value, minValue, maxValue);
//		} else {
//			map(value,0.0f, 600.0f);
//		}
//		return null;
//	}
//
//
//	public double getMinValue() {
//		return minValue;
//	}
//	public double getMaxValue() {
//		return maxValue;
//	}
	
}