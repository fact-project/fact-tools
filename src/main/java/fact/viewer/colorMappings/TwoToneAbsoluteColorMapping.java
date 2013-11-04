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
	float minValue = -20,maxValue = 500;

	public TwoToneAbsoluteColorMapping(){
		Color.RGBtoHSB(neutralColor.getRed(), neutralColor.getGreen(), neutralColor.getBlue(), neutralHsbVals);
		Color.RGBtoHSB(maxColor.getRed(), maxColor.getGreen(), maxColor.getBlue(), maxHsbVals);
		Color.RGBtoHSB(minColor.getRed(), minColor.getGreen(), minColor.getBlue(), minHsbVals);
	}
	

	/**
	 * @see fact.viewer.colorMappings.ColorMapping#map(java.lang.Double)
	 */
	@Override
	public Color map(double value, double minValue, double maxValue) {
		if(Double.isNaN(value)){
			value = 0.0f;
		}
		if(value >= maxValue){
			return Color.red;
		}
		if(value <= minValue){
			return Color.blue;
		}

		//log.info( "Scaling {} with {}", v, scale );
		if (value >= neutralValue){
			//map to positive color
			value = (float) Math.abs( value/(maxValue-neutralValue) );
			float hue = (float) (neutralHsbVals[0] + maxHsbVals[0]);
			float sat = (float) (neutralHsbVals[1] + (maxHsbVals[1] - neutralHsbVals[1])*value);
			float bri = (float) (neutralHsbVals[2] + (maxHsbVals[2] - neutralHsbVals[2])*value);
			return Color.getHSBColor(hue, sat, bri);
		} else {
			//map to negative color. hue should be Constant
			value = (float) Math.abs( value/(neutralValue-minValue) );
			float hue = (float) (neutralHsbVals[0] + minHsbVals[0]);
			float sat = (float) (neutralHsbVals[1] + (minHsbVals[1] - neutralHsbVals[1])*value);
			float bri = (float) (neutralHsbVals[2] + (minHsbVals[2] - neutralHsbVals[2])*value);
			return Color.getHSBColor(hue, sat, bri);
		}
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