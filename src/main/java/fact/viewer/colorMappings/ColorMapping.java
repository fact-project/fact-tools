/**
 * 
 */
package fact.viewer.colorMappings;

import java.awt.Color;

/**
 * @author chris
 *
 */
public interface ColorMapping {

	public float getMinValue();
	public float getMaxValue();
	public Color map( Double d );
	
	public Color map( Float f );
	public void setMinMax(Float min, Float max);
}
