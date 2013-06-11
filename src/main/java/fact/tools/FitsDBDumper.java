package fact.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.data.DataFactory;
import uk.ac.starlink.fits.FitsTableBuilder;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.util.FileDataSource;

/**
 * @author chris
 * 
 */
public class FitsDBDumper {

	static Logger log = LoggerFactory.getLogger(FitsDBDumper.class);

	private static Date enddate;
	private static SimpleDateFormat parserSDF  = new SimpleDateFormat("yyyy-MM-dd");
	private static Date startdate;
	private static String filelist = "./fileList";
	private static boolean verbose =  false;
	private static ArrayList<String> fileNameList = new ArrayList<String>();
	private static boolean printFileList = false;
	private static boolean vv = false;
	private static boolean dryRun =  false;
	private int errCount = 0;

	File fitsFile;
	String tableName;
	Date date;
	String dateString = "";
	Integer run;
	Map<String, Class<?>> columns;
	Connection con;
	String driver = "com.mysql.jdbc.Driver";
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public FitsDBDumper(File file, String jdbc, String user, String password)
			throws Exception {
		
		fitsFile = file;
//		Class.forName(driver);
//		DriverManager.registerDriver (new com.mysql.jdbc.Driver()); 
		try {
			con = DriverManager.getConnection(jdbc, user, password);
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		columns = FitsExplore.explore(fitsFile);

		int idx = file.getName().indexOf(".");
		int end = file.getName().indexOf(".", idx + 1);
		date = FitsExplore.extractDate(file.getName());
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		if (date != null) {
			dateString = fmt.format(date);
		}

		run = FitsExplore.extractRun(file.getName());
		log.info("run is: {}", run);
		tableName = file.getName().substring(idx + 1, end);
		log.info("Table name should be '{}'", tableName);
	}

	public String getTableName() {
		return tableName;
	}

	public boolean createTable(String name, Map<String, Class<?>> types, ArrayList<ArrayList<String>> arrayNames) {
		
		StringBuffer s = new StringBuffer();
		s.append("CREATE TABLE " + name + " (\n");
		s.append(" _TIMESTAMP_ BIGINT, \n");
		s.append(" _DATE_ DATE, \n");
		s.append(" _RUN_ INT, \n");
		s.append("_DATETIME_ DATETIME, \n");
		log.info("Types for table:\n{}", types);
		Iterator<String> it = types.keySet().iterator();
		while (it.hasNext()) {

			String key = it.next();

			if (types.get(key) == Float.class || types.get(key) == float.class) {
				s.append(" " + key + " REAL");
			}

			if (types.get(key) == Double.class
					|| types.get(key) == double.class) {
				s.append(" " + key + " REAL");
			}
			if (types.get(key) == Short.class
					|| types.get(key) == short.class) {
				s.append(" " + key + " SMALLINT");
			}
			if (types.get(key) == Integer.class || types.get(key) == int.class) {
				s.append(" " + key + " INT");
			}

			if (types.get(key) == Long.class || types.get(key) == long.class) {
				s.append(" " + key + " BIGINT");
			}

			if (it.hasNext()) {
				s.append(",\n");
			}
		}

		s.append(" )\n");
		log.info("Trying to create table '{}':\n{}", name, s.toString());

		try {
			Statement stmt = con.createStatement();
			int rc = stmt.executeUpdate(s.toString());
			log.info("create returned: {}", rc);
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if(arrayNames!= null && !arrayNames.isEmpty()){

			for(ArrayList<String> list: arrayNames){
				StringBuffer c = new StringBuffer();
				String[] arNames = list.get(0).split("_");
				String arName = "";
				for(int i = 0; i < arNames.length-1; i++){
					arName = arName + arNames[i];
				}
				c.append("CREATE VIEW " + name + "_" + arName+ " ");
				c.append("AS SELECT  " );
				for (String ar : list){
					c.append(ar + ", ");
				}
				c.append(" _TIMESTAMP_ ,");
				c.append(" _DATE_  ,");
				c.append(" _RUN_ ,");
				c.append("_DATETIME_  ");
				c.append(" FROM " + name);
	
				try {
					Statement stmt = con.createStatement();
					int viewc = stmt.executeUpdate(c.toString());
					log.info("create view returned: {}", viewc);
					stmt.close();
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return false;

	}

	public boolean hasTable(String name) {
		Statement stmt = null;
		boolean found = false;
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM "
					+ name );
			if (rs.next()) {
				found = true;
			}
			rs.close();
		} catch (Exception e) {
			log.error("Error: {}", e.getMessage());
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}
		return found;
	}

	public void importData(Processor writer) throws Exception {
		FileDataSource ds = new FileDataSource(fitsFile);
		FitsTableBuilder ftb = new FitsTableBuilder();
		StarTable table = ftb.makeStarTable(ds, false,
				StoragePolicy.PREFER_MEMORY);
		ArrayList<String> cols = new ArrayList<String>();

		for (int c = 0; c < table.getColumnCount(); c++) {
			ColumnInfo col = table.getColumnInfo(c);
			// log.info( "Adding column '{}'", col.getName() );
			if (!col.getName().trim().equals("")) {
				cols.add(col.getName());
			}
		}

		Data item = DataFactory.create();

		for (long rowId = 0; rowId < table.getRowCount(); rowId++) {
			item.clear();
			Object[] row = table.getRow(rowId);
			for (int c = 0; c < row.length; c++) {
				String colName = cols.get(c);
				if (!colName.trim().equals("")) {
					Object val = row[c];
					if(val == null){
						System.out.println("val is null!  ");
						System.out.println("cols: "  + cols.toString());
						for(String ci : cols){
							System.out.println(ci);
							
						}
						System.out.println("rows: "  + row.toString());
						for(Object ci : row){
							System.out.println(ci.toString());
							
						}
						
					}
					Class<?> clazz = val.getClass();
					if (clazz.isArray()) {

						FitsConvert.dumpArray(val, colName, item);

					} else {
						item.put(colName, row[c] + "");
					}
				}
			}

			if (writer != null) {
				writer.process(item);
			} else {
				insert(this.tableName, item);
			}
		}

	}


	public Long mjd2unixtime(Double mjd) {
		double JD_MJD_DIFF = 2400000.5d;
		double UNIX_JD_OFFSET = 2440587.5;
		double jd = mjd + JD_MJD_DIFF;
		Double unix = (jd - UNIX_JD_OFFSET) * 86400 * 1000;
		return unix.longValue();
	}


	public void insert(String table, Data item) {
		StringBuffer cols = new StringBuffer();
		StringBuffer vals = new StringBuffer();

		cols.append("_DATE_, ");
		vals.append("'" + dateString + "', ");
		cols.append("_RUN_, ");
		vals.append(run + ", ");
		cols.append("_TIMESTAMP_, ");
		if (item.containsKey("Time")) {
			//the time format stored in the fits files is different from any other known time format. Why?? WHO THE FUCK KNOWS!
			//so better add 40587.5 for some reason. IDIOTS!
			Long unix = mjd2unixtime(new Double(item.get("Time")
					.toString())  + 40587.5);
			vals.append(unix + ", ");
			cols.append("_DATETIME_, ");
			vals.append("'"+dateFormat.format(new Date(unix)) + "', ");
		} else {
			vals.append(date.getTime() + ", ");
		}

		Iterator<String> it = item.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			log.trace("Adding key '{}'", key);
			if (!key.trim().equals("")) {
				cols.append(key);
				vals.append(item.get(key).toString());

				if (it.hasNext()) {
					cols.append(",");
					vals.append(",");
				}
			}
		}
		
		String insert = "INSERT INTO " + table + " (" + cols.toString()
				+ ") VALUES (" + vals.toString() + ")";
		
//		log.info("insert: {}", insert);

		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate(insert);
			stmt.close();
		} catch (Exception e) {
			errCount++;
			e.printStackTrace();
			if(errCount > 10){
				System.out.println("More than 10 SQL insert Statements failed. Exiting");
				System.exit(1);
			}
		}
	}

	/*
	 *Finds all files accepted by the filter in the given directory and its subdirectory  
	 */
	public static List<File> findFilesByName(File dir) {
		FitsFileFilter filter = new FitsFileFilter();
		List<File> list = new ArrayList<File>();
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(filter);
			
			if(verbose){
				System.out.println("Descending into dir: " + dir.getAbsolutePath());
			}
			
			if (files != null) {
				for (File f : files) {
					if (f.isDirectory() && !f.getName().startsWith(".")) {
						list.addAll(findFilesByName(f));
					} else if( filter.accept(f)) {
						//check if files are within start and endate. Assumes the canonical factFile paths. which is bullshit but whatever
						if(startdate != null || enddate != null){
							String[] tokens = f.getAbsolutePath().split("\\/");
							String dateString = tokens[tokens.length-4] +"-"+ tokens[tokens.length-3] +"-"+ tokens[tokens.length-2];
							Date dirDate =  null;
							try {
								dirDate= parserSDF.parse(dateString);
							} catch (ParseException e) {
								e.printStackTrace();
							}
							//File is in a directory starting before the startdate
							if(startdate != null && dirDate.before(startdate)){
								break;
							}
							if (enddate != null && dirDate.after(enddate)){
								break;
							}
							
							
						}
						if(printFileList){
							fileNameList.add(f.getAbsolutePath());
						}
						list.add(f);
					}
				}
			}
		} else {
			if (filter.accept(dir)){
				list.add(dir);
			}
		}

		return list;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		// create the command line parser
		CommandLineParser parser = new BasicParser();
		// create the Options
		Options options = new Options();
		options.addOption("h", "help", false, "Shows this help text." );
		options.addOption("pf", "print-file-list", false, "print complete list of all files that are being parsed" );
		options.addOption("v", "verbose", false, "Print some more output to the console" );
		options.addOption("vv", "verbose-high", false, "Print quite alot of output to the console" );
		options.addOption("d", "dry-run", false, "Starts a dry run. Does not connect or write anything to a DataBase  ");
		options.addOption("s", "start-date", true, "The earliest date allowed. Date format to parse: yyyy-MM-dd");
		options.addOption("e", "end-date", true, "The latest date allowed. Date format to parse: yyyy-MM-dd");
		options.addOption("p", "path-to-filelist", true, "Path to newline seperated file containing the names of the Slow Controll files that are being parsed \n" +
				"e.g BIAS_CONTROL_STATE \n" +
				"BIAS_CONTROL_VOLTAGE\n"+
				"CHAT_CLIENT_LIST \n" +
				"DATA_LOGGER_CLIENT_LIST \n"+
				"\n"+
				"Lines starting with # will not be parsed \n"+
				"looks for a file named \"fileList\" per default.   ");
		
		
		if(verbose){
			System.out.println("Starting FitsDBDumper " );
		}
		String[] pathList = null;
		try {
		    // parse the command line arguments
		    org.apache.commons.cli.CommandLine line = parser.parse( options, args );
		    if( line.hasOption( "h" ) ) {
		    	HelpFormatter help = new HelpFormatter();
		    	help.printHelp("FitsDbDumper", options);
		    	return;
		    }
		    if(line.hasOption("d")){
		    	dryRun = true;    	
		    }
		    if(line.hasOption("d")){
		    	printFileList = true;    	
		    }
		    if(line.hasOption("v")){
		    	verbose = true;    	
		    }
		    if(line.hasOption("vv")){
		    	verbose = true;
		    	vv = true;    	
		    }
		    if (line.hasOption("s")){
		    	String dateString = line.getOptionValue("s");
		    	startdate = parserSDF.parse(dateString);
		    }
		    if (line.hasOption("e")){
		    	String dateString = line.getOptionValue("e");
		    	enddate = parserSDF.parse(dateString);
		    }
		    if (line.hasOption("p")){
		    	filelist = line.getOptionValue("p");
		    }
		    pathList = line.getArgs();
		}
		catch( org.apache.commons.cli.ParseException exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		}
		catch(java.text.ParseException e){
			System.out.println( "Can't parse date. Use yyyy-MM-dd format for start and enddate" + e.getMessage() );
		}
		if(verbose){
			System.out.println("Pathlist is: ");
			for(String p : pathList){
				System.out.println(p);
			}
			System.out.println();
		}

		for(String path : pathList){
			File source = new File(path);
			if (verbose){
				System.out.println("Starting in dir: " + source.getAbsolutePath());
			}
			if(!source.exists()){
				System.out.println("Path does not exist: " + path);
				break;
			}
			if(!source.isDirectory()){
				System.out.println("The specified path is not a directory: " + path);
				break;
			}
			if(!source.canRead()){
				System.out.println("Can't read the specified path: "+ path);
				break;
			}
			List<File> sources = findFilesByName(source);
			log.info("Importing from {} files", sources);
			if(verbose){
				System.out.println("Number of files to be parsed: " + sources.size());
			}
			for (File file : sources) {
				if(vv){
					log.info("Importing for file {}", file);
					log.info("File is: {}", file);
					log.info("file exists? {}", file.canRead());
				}
					if(!dryRun){
						if(verbose){
							System.out.println("Dumper started");
						}
						FitsDBDumper dumper = new FitsDBDumper(file,
								"jdbc:mysql://kilabdb.cs.uni-dortmund.de:3306/bruegge", "bruegge", "ls8test");
						log.info("Does table '{}' exist? {}", dumper.getTableName(),
								dumper.hasTable(dumper.getTableName()));
		
						Map<String, Class<?>> cols = FitsExplore.explore(file);
						ArrayList<ArrayList<String>>  arrayNames = FitsExplore.getTypes(file);
						log.info("Columns required:");
						for (String key : cols.keySet()) {
							log.info(" {} = {}", key, cols.get(key));
						}
		
						if (!dumper.hasTable(dumper.getTableName())) {
							log.info("Creating table '{}'", dumper.getTableName());
							dumper.createTable(dumper.getTableName(), cols, arrayNames);
						}
		
						dumper.importData(null);
					}
			}
		}
		if(printFileList){
			System.out.println("File list:     -----------");
			for(String file: fileNameList){
				System.out.println(file);
			}
		}
	}


	public static class FitsFileFilter implements FileFilter {
		/**
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		ArrayList<String> names = new ArrayList<String>();
		public FitsFileFilter(){
			try {
				File f = new File(filelist);
				if(verbose){
					System.out.println("trying to read fileList from" + f.getAbsolutePath());
				}
				if (!f.exists()){
					System.out.println("Can't read fileList. I can't work like this! ");
					System.exit(1);
				}
				if (f.isDirectory()){
					System.out.println("The path you enterd for the filelist is a directory.");
					System.out.println("exiting");
					System.exit(1);
				}
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line = in.readLine();
				while (line != null ){
					line = in.readLine();
					if(line != null && !line.startsWith("#") && !line.equals("") && !line.equals(" ")){
						names.add(line);
					}
				}
				in.close();
				
				if(verbose){
					System.out.println("Succesfully read the namelist from " + filelist);
					for(String name : names){
						System.out.println(name);
					}
					System.out.println();
				}
				
			} catch (FileNotFoundException e) {
				System.out.println("File not found");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		public boolean accept(File arg0) {
			if(arg0.getName().startsWith(".")){
				return false;
			}
			if(arg0.isDirectory()){
				return true;
			}
			for(String m : names){
				if ((arg0.getName().split("\\."))[0].contains("_")){
					return false;
				}
				
				if(arg0.getName().contains(m) && arg0.getName().endsWith(".fits")){
					return true;
				}
			}
			return false;
		}
	}
}