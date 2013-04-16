/**
 * 
 */
package fact.image;

/**
 * @author chris
 *
 */
public class HighVolumeIntegration 
	implements Transformation 
{

	/**
	 * @see fact.image.Transformation#transform(float[])
	 */
	@Override
	public float transform(float[] slices) {

		int maxIdx = Series.findMaxIndex( slices, 0 );
		float max = slices[maxIdx];
		
		int minIdx = Series.findMinIndex( slices, 0 );
		float min = slices[minIdx];
		
		float threshold = 0.5f * max;
		int start = Series.findFirstLarger( slices, threshold, 0 );
		int end = Series.findFirstSmaller( slices, threshold, start );
		if( end < 0 ){
			end = slices.length - 1;
		}
		
		if( start < 0 )
			start = 0;
		
		float sum = 0.0f;
		for( int i = start; i < end; i++ ){
			sum += slices[i];
		}
		
		return sum * (max - min);
	}
}