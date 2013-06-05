/**
 * 
 */
package fact.viewer.colorMappings;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Map values to color by linear interpolation from neutralColor to max/minColor.
 * @author kai
 */
public class GrayScaleColorMapping 
	implements ColorMapping 
{
	static Logger log = LoggerFactory.getLogger( GrayScaleColorMapping.class );
	float neutralValue = 0.0f;
	Color maxColor = Color.white;
	Color minColor = Color.black;
	float minValue, maxValue;
	/**
	 * @see fact.viewer.colorMappings.ColorMapping#map(java.lang.Double)
	 */
	@Override
	public Color map(float v, float minValue, float maxValue) {
		if(Float.isNaN(v)){
			v = neutralValue;
		}
			float value = v + Math.abs(minValue);
			value = value/(maxValue-minValue);
			return Color.getHSBColor(0.0f, 0.0f, value);
		
	}
	

//	@Override
//	public Color map(float value) {
//		if(this.maxValue !=  0.0){
//			map(value, minValue, maxValue);
//		} else {
//			map(value,0.0f, 600.0f);
//		}
//		return null;
//	}
//
//
//	public float getMinValue() {
//		return minValue;
//	}
//	public float getMaxValue() {
//		return maxValue;
//	}
	
}