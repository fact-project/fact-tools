/**
 * 
 */
package fact.viewer.colorMappings;

import java.awt.Color;

/**
 * @author Kai
 *
 */
public interface ColorMapping {
	
	public Color map(float value,  float minValue,  float maxValue);
	//public Color map(float value);
//	public float getMinValue();
//	public float getMaxValue();
}
