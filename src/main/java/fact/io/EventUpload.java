/**
 * 
 */
package fact.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;
import stream.util.MD5;
import fact.data.EventKey;
import fact.tools.FitsExplore;

/**
 * @author chris
 *
 */
public class EventUpload {

	static Logger log = LoggerFactory.getLogger( EventUpload.class );

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		File file = new File( "/Volumes/ramdisk/20111122_059.fits");



		FitsStream stream = new FitsStream( new SourceURL(file.getAbsolutePath()) );
		int id = 0;
		int skip = 20;
		int limit = 100;
		Data event = stream.readNext();
		
		Integer run = FitsExplore.extractRun( file.getName() );

		
		while( event != null && id < skip + limit ){
			log.info( "Event: {}", event );

			if( id > skip ){

				int[] utc = (int[]) event.get( "UnixTimeUTC" );  
				Date date = new Date( utc[0]  * 1000L );
				Integer eventNum = new Integer( event.get( "EventNum" ).toString() );
				EventKey key = new EventKey( date, run, eventNum );
				
				/*
				 */
				URL url = new URL( "http://localhost:8080/upload/2011/11/27/001/" + id );
				url = new URL( "http://kirmes.cs.uni-dortmund.de/fact/upload/" + key.toString() );
				log.info( "Uploading event to {}", url );
				HttpURLConnection con = (HttpURLConnection) url.openConnection();

				con.setDoInput( true );
				con.setDoOutput( true );

				ObjectOutputStream oos = new ObjectOutputStream( con.getOutputStream() );
				log.info( "Uploading event, md5: {}", MD5.md5( event ) );
				oos.writeObject( event );
				oos.close();

				BufferedReader r = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
				String line = r.readLine();
				while( line != null ){
					log.info( "Response: {}", line );
					line = r.readLine();
				}
				r.close();

			}
			id++;
			event = stream.readNext();
		}
	}

}
