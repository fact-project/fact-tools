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
	
	public Color map(double value,  double minValue,  double maxValue);
	//public Color map(float value);
//	public float getMinValue();
//	public float getMaxValue();
}
