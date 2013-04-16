/**
 * 
 */
package fact.viewer.ui;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.viewer.colorMappings.ColorMapping;

/**
 * Map values to color by linear interpolation from neutralColor to max/minColor.
 *
 */
public class NeutralColorMapping 
	implements ColorMapping 
{
	static Logger log = LoggerFactory.getLogger( NeutralColorMapping.class );
	Double neutralValue = 0.0d;
	Color maxColor = Color.yellow;
	Color minColor = Color.blue;
	Color neutralColor = Color.black;
	private	float[] neutralHsbVals = new float[3];
	private float[] maxHsbVals = new float[3];
	private float[] minHsbVals = new float[3];
	public float minValue,maxValue;
	public float getMinValue() {	return minValue;	}
	public float getMaxValue() {	return maxValue;	}


	public NeutralColorMapping(){
		Color.RGBtoHSB(neutralColor.getRed(), neutralColor.getGreen(), neutralColor.getBlue(), neutralHsbVals);
		Color.RGBtoHSB(maxColor.getRed(), maxColor.getGreen(), maxColor.getBlue(), maxHsbVals);
		Color.RGBtoHSB(minColor.getRed(), minColor.getGreen(), minColor.getBlue(), minHsbVals);
	}
	

	/**
	 * @see fact.viewer.colorMappings.ColorMapping#map(java.lang.Double)
	 */
	@Override
	public Color map(Double v) {
		if(v.equals(Double.NaN)){
			v = neutralValue;
		}
		Double value = v;

		//log.info( "Scaling {} with {}", v, scale );
		if (v >= neutralValue){
			//map to positive color
			value = Math.abs( v/(maxValue-neutralValue) );
			float hue = (float) (neutralHsbVals[0] + (maxHsbVals[0] - neutralHsbVals[0])*value);
			float sat = (float) (neutralHsbVals[1] + (maxHsbVals[1] - neutralHsbVals[1])*value);
			float bri = (float) (neutralHsbVals[2] + (maxHsbVals[2] - neutralHsbVals[2])*value);
			return Color.getHSBColor(hue, sat, bri);
		} else {
			//map to negative color
			value = Math.abs( v/(neutralValue-minValue) );
			float hue = (float) (neutralHsbVals[0] + (minHsbVals[0] - neutralHsbVals[0])*value);
			float sat = (float) (neutralHsbVals[1] + (minHsbVals[1] - neutralHsbVals[1])*value);
			float bri = (float) (neutralHsbVals[2] + (minHsbVals[2] - neutralHsbVals[2])*value);
			return Color.getHSBColor(hue, sat, bri);
		}

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