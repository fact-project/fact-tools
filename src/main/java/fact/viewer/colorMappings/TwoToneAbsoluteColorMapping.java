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
public class TwoToneAbsoluteColorMapping 
	implements ColorMapping 
{
	static Logger log = LoggerFactory.getLogger( TwoToneAbsoluteColorMapping.class );
	Double neutralValue = 0.0d;
	Color maxColor = Color.yellow;
	Color minColor = Color.green;
	Color neutralColor = Color.black;
	private	float[] neutralHsbVals = new float[3];
	private float[] maxHsbVals = new float[3];
	private float[] minHsbVals = new float[3];
	public float minValue = -20,maxValue = 500;
	public float getMinValue() {	return minValue;	}
	public float getMaxValue() {	return maxValue;	}


	public TwoToneAbsoluteColorMapping(){
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
		if(v >= maxValue){
			return Color.red;
		}
		if(v <= minValue){
			return Color.blue;
		}

		//log.info( "Scaling {} with {}", v, scale );
		if (v >= neutralValue){
			//map to positive color
			value = Math.abs( v/(maxValue-neutralValue) );
			float hue = (float) (neutralHsbVals[0] + maxHsbVals[0]);
			float sat = (float) (neutralHsbVals[1] + (maxHsbVals[1] - neutralHsbVals[1])*value);
			float bri = (float) (neutralHsbVals[2] + (maxHsbVals[2] - neutralHsbVals[2])*value);
			return Color.getHSBColor(hue, sat, bri);
		} else {
			//map to negative color. hue should be Constant
			value = Math.abs( v/(neutralValue-minValue) );
			float hue = (float) (neutralHsbVals[0] + minHsbVals[0]);
			float sat = (float) (neutralHsbVals[1] + (minHsbVals[1] - neutralHsbVals[1])*value);
			float bri = (float) (neutralHsbVals[2] + (minHsbVals[2] - neutralHsbVals[2])*value);
			return Color.getHSBColor(hue, sat, bri);
		}
	}

	//ignore this value. This mapping is absolute
	public void setMinMax( Double min, Double max ){
//		this.minValue = min.floatValue() ; 
//		this.maxValue = max.floatValue() ;
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