/**
 * 
 */
package fact.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;

/**
 * @author chris
 *
 */
public class FitsEventSplitter {

	static Logger log = LoggerFactory.getLogger( FitsEventSplitter.class );
	
	
	public static void store( Data item, File file ) throws Exception {
		OutputStream out;
		if( file.getName().toLowerCase().endsWith( ".gz" ) ){
			out = new GZIPOutputStream( new FileOutputStream( file ) );
		} else {
			out = new FileOutputStream( file );
		}
		
		ObjectOutputStream oos = new ObjectOutputStream( out );
		oos.writeObject( item );
		oos.flush();
		oos.close();
		out.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		File file = new File( "/Volumes/ramdisk/20111122_059.fits");
		
		if( args.length > 0 ){
			file = new File( args[0] );
		}
		
		int limit = 10;
		try {
			log.info( "Parsing limit '{}'", System.getProperty( "limit" ) );
			limit = Integer.parseInt( System.getProperty( "limit" ) );
		} catch (Exception e) {
			log.error( "Error: {}", e.getMessage() );
			limit = 10;
		}
		
		
		FitsDataStream stream = new FitsDataStream( file );
		int id = 0;
		Data event = stream.readNext();
		while( event != null && id < limit ){
			log.info( "Event: {}", event );
			File f = new File( "event_" + id + ".event.gz" );
			log.info( "Storing event-{} in {}", id, f );
			store( event, f );
			id++;
		}
	}
}
