/**
 * 
 */
package fact.io;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;
import fact.tools.FitsExplore;
import fact.viewer.ui.CameraPixelMap;

/**
 * @author chris
 *
 */
public class CreateAnimatedGif {

	static Logger log = LoggerFactory.getLogger( CreateAnimatedGif.class );


	public static File createAnimatedGif( File out, Date date, Integer run, Data event, int start, int end, int step ){
		try {
			CameraPixelMap map = new CameraPixelMap( 4.0d );
			map.setDate( date );
			map.setRun( run );
			map.setEvent( event );
			//map.saveAnimatedGif(); // new File( "/Users/chris/test.gif"), 0, 125, 2 );
			map.saveAnimagedGif( out, start, end, step );
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		int start = 0;
		int end = 200;
		int step = 2;
		File outDir = new File( "." );

		if( args.length < 2 ){
			System.err.println( "Usage:\n" );
			System.err.println( "\tjava fact.io.CreateAnimatedGif  /path/to/raw/data.fits.gz  EVENT_NUM" );
			System.err.println( "" );
			System.exit( -1 );
			return;
		}

		try {
			start = new Integer( System.getProperty( "start" ) );
			start = Math.max( 0, start );
		} catch (Exception e) {
			start = 0;
		}

		try {
			end = new Integer( System.getProperty( "end" ) );
			end = Math.max( start + 1, end );
		} catch (Exception e) {
			end = 200;
		}

		try {
			step = new Integer( System.getProperty( "step" ) );
			step = Math.max( 1, step );
		} catch (Exception e) {
			step = 2;
		}

		try {
			outDir = new File( System.getProperty( "output.directory" ) );
			if( !outDir.isDirectory() )
				outDir.mkdirs();
		} catch (Exception e) {
			outDir = new File( "." );
		}

		File file = new File( args[0] );
		Integer eventId = new Integer( args[1] );
		Integer run = FitsExplore.extractRun( file.getName() );

		FitsStream stream = new FitsStream( new SourceURL(file.getAbsolutePath()) );
		Data event = stream.readNext();
		int i = 1;
		while( event != null && i < eventId ){
			event = stream.readNext();
			i++;
		}

		if( event == null ){
			System.err.println( "No event found for ID " + eventId );
			return;
		}


		log.info( "Event found...", event );

		int[] time = (int[]) event.get( "UnixTimeUTC" );
		Date date = new Date( time[0] * 1000L );
		log.info( "timestamp of event is {}", date );


		DecimalFormat df = new DecimalFormat( "000" );
		SimpleDateFormat fmt = new SimpleDateFormat( "yyyyMMdd_" );
		String runString = "???";
		if( run != null )
			runString = df.format( run );

		File out = new File( outDir.getAbsolutePath() + File.separator + fmt.format( date ) + runString + "_" + eventId + ".gif" );
		//System.out.println( "Writing GIF to " + out.getAbsolutePath() );

		CameraPixelMap map = new CameraPixelMap( 4.0d );
		map.setDate( date );
		map.setRun( run );
		map.setEvent( event );
		//map.saveAnimatedGif(); // new File( "/Users/chris/test.gif"), 0, 125, 2 );
		map.saveAnimagedGif( out, start, end, step );
	}
}
