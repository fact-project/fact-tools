/**
 * 
 */
package fact.image;


/**
 * @author chris
 *
 */
public class Peak implements Transformation {
	

	/**
	 * @see fact.image.Transformation#transform(float[])
	 */
	@Override
	public float transform(float[] slices) {
		
		float max = slices[0];
		
		for( int i = 1; i < slices.length; i++ ){
			if( max > slices[i] ){
				max = slices[i];
			}
		}
		
		return max;
	}
}