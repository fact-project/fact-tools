/**
 * 
 */
package fact.io;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;

/**
 * @author chris
 *
 */
public class DrsInspector {

	static Logger log = LoggerFactory.getLogger( DrsInspector.class );
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		FitsDataStream stream = new FitsDataStream( new File( "/Volumes/RamDisk/20111126_034.drs.fits" ) );
		
		Data item = stream.readNext();
		log.info( "item: {}", item );
		
	}

}
