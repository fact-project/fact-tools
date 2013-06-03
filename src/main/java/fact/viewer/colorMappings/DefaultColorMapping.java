/**
 * 
 */
package fact.viewer.colorMappings;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author kai
 *
 */
public class DefaultColorMapping 
	implements ColorMapping 
{
	static Logger log = LoggerFactory.getLogger( DefaultColorMapping.class );
	float scale = 1.0f;
	float minValue, maxValue;
	public DefaultColorMapping(){
	}
	

	/**
	 * @see fact.viewer.colorMappings.ColorMapping#map(java.lang.Double)
	 */
	@Override
	public Color map(float value, float min, float max) {
		scale = (max - min);
		if( scale == 0.0d )
			scale = 1.0f;
		
		if(Float.isNaN(value)){
			value = 0.0f;
		}
		//log.info( "Scaling {} with {}", v, scale );
		value = (value - min) / scale; //scale * v;
		//log.info( "     {} ~> {}", v, value );
		//Double range = Math.PI / 3.0f * 2.0f;
		//Double offset = Math.PI * 4.0f / 3.0f;
		//Double scaled = offset - value * range;
		float d = (float) (Math.PI + 0.25d * Math.PI * value);
		float weight0 = value;
		if( weight0 > 1.0f )
			weight0 = 1.0f;
		
		if( weight0 < 0.0d )
			weight0 = 0.0f;
		return Color.getHSBColor(  d, 1.0f - value * value, 1.0f * (1.0f - value));
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