/**
 * 
 */
package fact.tools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.starlink.fits.FitsTableBuilder;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.util.DataSource;
import uk.ac.starlink.util.FileDataSource;

/**
 * @author chris
 *
 */
public class FitsExplore {
	static Logger log = LoggerFactory.getLogger( FitsExplore.class );

	public static Map<String,Class<?>> explore( File fitsFile ) throws Exception {
		FileDataSource ds = new FileDataSource( fitsFile );

		FitsTableBuilder ftb = new FitsTableBuilder();
		StarTable table = ftb.makeStarTable( ds, false , StoragePolicy.ADAPTIVE );
		Map<String,Class<?>> cols = new LinkedHashMap<String,Class<?>>();

		for( int c = 0; c < table.getColumnCount(); c++ ){
			ColumnInfo col = table.getColumnInfo( c );
			if( col.getName().trim().isEmpty() ){
				log.debug( "Skipping empty column-name '{}' for column {}", col.getName(), col );
				continue;
			}
			
			log.info( "Adding column '{}' of type {}", col.getName(), col.getContentClass() );

			log.info( "isArray() {} ?? {}", col.getContentClass(), col.getContentClass().isArray() );
			log.info( "array length is: {}", col.getShape() );

			if( col.getContentClass().isArray() ){
				int depth = col.getShape()[0];
				for( int i = 0; i < depth; i++ ){
					cols.put( col.getName() + "_" + i, col.getContentClass().getComponentType() );
				}
			} else {
				if( col.getName().trim().isEmpty() ){
					continue;
				}
				cols.put( col.getName(), col.getContentClass() );
			}
		}
		return cols;
	}
	
	public static ArrayList<ArrayList<String>>  getTypes(File fitsFile) throws Exception{
		FileDataSource ds = new FileDataSource( fitsFile );
		FitsTableBuilder ftb = new FitsTableBuilder();
		StarTable table = ftb.makeStarTable( ds, false , StoragePolicy.ADAPTIVE );
		ArrayList<ArrayList<String>> cols = new ArrayList<ArrayList<String>>();

		for( int c = 0; c < table.getColumnCount(); c++ ){
			ColumnInfo col = table.getColumnInfo( c );
			if( col.getName().trim().isEmpty() ){
				log.debug( "Skipping empty column-name '{}' for column {}", col.getName(), col );
				continue;
			}
			
			if( col.getContentClass().isArray() ){
				int depth = col.getShape()[0];
				ArrayList<String> tmp = new ArrayList<String>();
				for( int i = 0; i < depth; i++ ){
					tmp.add( col.getName() + "_" + i);
				}
				cols.add(tmp);
			}
		}
		return cols;
	}

	public static Map<String,Class<?>> explore( DataSource fitsFile ) throws Exception {
		DataSource ds = fitsFile;

		FitsTableBuilder ftb = new FitsTableBuilder();
		StarTable table = ftb.makeStarTable( ds, false , StoragePolicy.PREFER_MEMORY );
		Map<String,Class<?>> cols = new LinkedHashMap<String,Class<?>>();

		for( int c = 0; c < table.getColumnCount(); c++ ){
			ColumnInfo col = table.getColumnInfo( c );
			if( col.getName().trim().isEmpty() ){
				log.debug( "Skipping empty column-name '{}' for column {}", col.getName(), col );
				continue;
			}
			
			log.debug( "Adding column '{}' of type {}", col.getName(), col.getContentClass() );

			log.debug( "isArray() {} ?? {}", col.getContentClass(), col.getContentClass().isArray() );
			log.debug( "array length is: {}", col.getShape() );

			if( col.getContentClass().isArray() ){
				int depth = col.getShape()[0];
				for( int i = 0; i < depth; i++ ){
					cols.put( col.getName() + "_" + i, col.getContentClass().getComponentType() );
				}
			} else {
				if( col.getName().trim().isEmpty() ){
					continue;
				}
				cols.put( col.getName(), col.getContentClass() );
			}
		}
		return cols;
	}

	public static Date extractDate( String name ){
		try {
			SimpleDateFormat fmt = new SimpleDateFormat( "yyyyMMdd" );
			Date date = fmt.parse( name.substring( 0, 8 ) );
			log.debug( "Extracted date: {}", date );
			return date;
		} catch (Exception e) {
			log.error( "Failed to extract date: {}", e.getMessage() );
			return null;
		}
	}

	public static Integer extractRun( String name ){
		try {
			int idx = name.indexOf( "_" );
			int end = name.indexOf( ".", idx );
			log.debug( "Run-number should be {}", name.substring( idx + 1, end ) );
			Integer runNumber = new Integer( name.substring( idx + 1, end ) );
			return runNumber;
		} catch (Exception e) {
			log.error( "Failed to extract run-number from name '{}': {}", name, e.getMessage() );
			return -1;
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<File> files = new ArrayList<File>();

		if( args.length > 0 ){
			files.add( new File( args[0] ) );
		}
		
		
		files.add( new File( "/Users/chris/Downloads/20111126_042.fits" ) );
		

		try {
			for( File f : files ){
				Map<String,Class<?>> dat = explore( f );
				log.info( "Columns: {}", dat );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
