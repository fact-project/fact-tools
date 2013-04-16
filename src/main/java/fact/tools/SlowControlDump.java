/**
 * 
 */
package fact.tools;

import java.io.File;

/**
 * @author chris
 *
 */
public class SlowControlDump {
	
	static String[] controlFiles = {
		"DRIVE_CONTROLPOINTING_POSITION",
		"FAD_CONTROL_TEMPERATURE",
		"FSC_CONTROL_TEMPERATURE",
		"FSC_CONTROL_HUMIDITY"
	};
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if( args.length == 0 ){
			System.out.println( "You need to specify a directory!" );
			return;
		}
		
		File dir = new File( args[0] );
		if( ! dir.isDirectory() ){
			System.out.println( dir + " is not a directory! " );
			return;
		}

		
		
		
	}

}
