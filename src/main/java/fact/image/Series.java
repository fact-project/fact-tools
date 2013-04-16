/**
 * 
 */
package fact.image;

/**
 * @author chris
 *
 */
public class Series {
	
	
	public static float getAverage( float[] slices ){
		float sum = 0.0f;
		float count = 0.0f;
		for( int i = 0; i < slices.length; i++ ){
			sum += slices[i];
			count += 1.0f;
		}
		
		return sum / count;
	}
	
	
	public static int findFirstSmaller( float[] slices, float value, int start ){
		
		if( start < 0 )
			start = 0;
		
		for( int i = start; i < slices.length; i++ ){
			if( slices[i] < value ){
				return i;
			}
		}
		
		return -1;
	}

	
	
	public static int findFirstLarger( float[] slices, float value, int start ){
		
		if( start < 0 )
			start = 0;
		
		for( int i = start; i < slices.length; i++ ){
			if( slices[i] > value ){
				return i;
			}
		}
		
		return -1;
	}
	
	
	public static int findMinIndex( float[] slices, int start ){
		int index = start;
		float min = slices[start];
		for( int i = start + 1; i < slices.length; i++ ){
			if( min > slices[i] ){
				index = i;
				min = slices[i];
			}
		}
		return index;
	}


	public static int findMaxIndex( float[] slices, int start ){
		int index = start;
		float max = slices[start];
		for( int i = start + 1; i < slices.length; i++ ){
			if( max < slices[i] ){
				index = i;
				max = slices[i];
			}
		}
		return index;
	}
	
	
	public static float max( float[] slices ){
		int idx = findMaxIndex( slices, 0 );
		return slices[idx];
	}
	
	
	public static float min( float[] slices ){
		int idx = findMinIndex( slices, 0 );
		return slices[idx];
	}
}