/**
 * 
 */
package fact.viewer.colorMappings;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author chris
 *
 */
public class DefaultColorMapping 
	implements ColorMapping 
{
	static Logger log = LoggerFactory.getLogger( DefaultColorMapping.class );
	Double min = 0.0d;
	public float getMinValue() {	return min.floatValue();	}
	Double max = 1.0d;
	public float getMaxValue() {	return max.floatValue();	}

	Double scale = 1.0d;
	
	public DefaultColorMapping(){
	}
	

	/**
	 * @see fact.viewer.colorMappings.ColorMapping#map(java.lang.Double)
	 */
	@Override
	public Color map(Double v) {
		if(v.equals(Double.NaN)){
			v = 0.0;
		}
		Double value = v;
		//log.info( "Scaling {} with {}", v, scale );
		value = (v - min) / scale; //scale * v;
		//log.info( "     {} ~> {}", v, value );
		//Double range = Math.PI / 3.0f * 2.0f;
		//Double offset = Math.PI * 4.0f / 3.0f;
		//Double scaled = offset - value * range;
		Double d = Math.PI + 0.25d * Math.PI * value;
		
		
		Double weight0 = value;
		if( weight0 > 1.0d )
			weight0 = 1.0d;
		
		if( weight0 < 0.0d )
			weight0 = 0.0d;
		
		
		
		//return Color.getHSBColor(  d.floatValue(), 1.0f - value.floatValue() * value.floatValue(), 1.0f * (1.0f - value.floatValue()));
		return Color.getHSBColor(  d.floatValue(), 1.0f - value.floatValue() * value.floatValue(), 1.0f * (1.0f - value.floatValue()));
		//return Color.getHSBColor(  d.floatValue(), 1.0f - value.floatValue(), 1.0f * (1.0f - value.floatValue()));
	}

	
	public void setMinMax( Double min, Double max ){
		
		this.min = min ; 
		this.max = max ;
		scale = (this.max - this.min);
		if( scale == 0.0d )
			scale = 1.0d;
	}
	
	
	public void setMinMax( Float min, Float max ){
		setMinMax( min.doubleValue(), max.doubleValue() );
	}
	
	public void setMinMax( float min, float max ){
		setMinMax( Float.valueOf(min), Float.valueOf(max) );
	}
	/**
	 * @see fact.viewer.colorMappings.ColorMapping#map(java.lang.Float)
	 */
	@Override
	public Color map(Float f) {
		return map( f.doubleValue() );
	}
}