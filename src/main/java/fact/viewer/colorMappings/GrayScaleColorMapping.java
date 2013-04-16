/**
 * 
 */
package fact.viewer.colorMappings;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Map values to color by linear interpolation from neutralColor to max/minColor.
 *
 */
public class GrayScaleColorMapping 
	implements ColorMapping 
{
	static Logger log = LoggerFactory.getLogger( GrayScaleColorMapping.class );
	Double neutralValue = 0.0d;
	Color maxColor = Color.white;
	Color minColor = Color.black;
	public float minValue,maxValue;
	public float getMinValue() {	return minValue;	}
	public float getMaxValue() {	return maxValue;	}


	public GrayScaleColorMapping(){
	}
	

	/**
	 * @see fact.viewer.colorMappings.ColorMapping#map(java.lang.Double)
	 */
	@Override
	public Color map(Double v) {
		if(v.equals(Double.NaN)){
			v = neutralValue;
		}
			float value = v.floatValue() + Math.abs(minValue);
			value = value/(maxValue-minValue);
			return Color.getHSBColor(0.0f, 0.0f, value);
		
	}

	
	public void setMinMax( Double min, Double max ){
		this.minValue = min.floatValue() ; 
		this.maxValue = max.floatValue() ;
	}
	
	
	public void setMinMax( Float min, Float max ){
		setMinMax( min.doubleValue(), max.doubleValue() );
	}
	
	
	/**
	 * @see fact.viewer.colorMappings.ColorMapping#map(java.lang.Float)
	 */
	@Override
	public Color map(Float f) {
		return map( f.doubleValue() );
	}
}