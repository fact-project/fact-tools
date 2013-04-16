package fact.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class SQLtoCSVDumper {
	Connection con;
	String driver = "com.mysql.jdbc.Driver";
	static boolean verbose = false;
	private File outputFile = null;

	
	public SQLtoCSVDumper(File file, String jdbc, String user, String password) {
		this.outputFile = file;
		try {
			con = DriverManager.getConnection(jdbc, user, password);
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public void executeQuery(String query) throws Exception{
		if (outputFile==null){
			throw new FileNotFoundException("outputfiel was null");
		}
		PrintWriter out = new PrintWriter(outputFile);
        java.sql.Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(query);
        ResultSetMetaData rsmd = rs.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();
        
        //write query as the first line in the file
        out.write("#");
        out.write(query);
        out.write('\n');
        //write the returned collumnames as the second line
        out.write("#");
        //this goes from 1 to numberOfCollums
        for(int i = 1; i <= numberOfColumns; i++){
        	out.write(rsmd.getColumnName(i));
        	out.write(',');
        }
        out.write('\n');
        //write data in each following line
        while (rs.next()) {
        	for (int i = 1; i <= numberOfColumns; i++){
               	out.write(rs.getString(i));
            	out.write(',');
        	}
            out.write('\n');
        }
        //done. close the stream
        out.close();
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// create the command line parser
				CommandLineParser parser = new BasicParser();
				// create the Options
				Options options = new Options();
				options.addOption("h", "help", false, "Shows this help text." );
				options.addOption("v", "verbose", false, "Print some more output to the console" );
				options.addOption("q", "query", true, "The SQL query to execute");
				options.addOption("o", "outputfile", true, "The path to the outputfile");
				options.addOption("u", "user", true, "The username for the sqlDB");
				options.addOption("c", "connection", true, "The hostname  for the sqlDB");
				File outputFile = null;
				String queryString = "", user = null, host = null, password=null;
				try {
				    // parse the command line arguments
				    org.apache.commons.cli.CommandLine line = parser.parse( options, args );
				    if( line.hasOption( "h" ) ) {
				    	HelpFormatter help = new HelpFormatter();
				    	help.printHelp("SQLtoCSVDumper", options);
				    	return;
				    }
				    if(line.hasOption("v")){
				    	verbose = true;    	
				    }
				    if (line.hasOption("q")){
				    	queryString = line.getOptionValue("q");
				    }
				    if (line.hasOption("c")){
				    	host = line.getOptionValue("c");
				    }
				    if (line.hasOption("o")){
				    	outputFile = new File(line.getOptionValue("o"));
				    	if (!outputFile.exists()){
				    		outputFile.createNewFile();
				    		if(!outputFile.canWrite()){
				    			System.out.println( "Can't write to File " + outputFile.getAbsolutePath() + "  It's not writable");
				    			return;
				    		}
				    	}
				    	if(!outputFile.canWrite()){
			    			System.out.println( "Can't write to File " + outputFile.getAbsolutePath() + "  It's not writable");
			    			return;
			    		}
				    }
				}
				catch( org.apache.commons.cli.ParseException exp ) {
				    System.out.println( "Unexpected exception while reading commandline arguments:" + exp.getMessage() );
				    exp.printStackTrace();
				    return;
				} catch (IOException e) {
					System.out.println("Coulndt create the file");
					e.printStackTrace();
				}
				
				if(host==null){
					host = "jdbc:mysql://kilabdb.cs.uni-dortmund.de:3306/bruegge";
				} else {
					host = "jdbc:mysql//" + host;
				}
				if(user ==null){
					user = "bruegge";
				}
				if(password == null){
					password = "ls8test";
				}
				
				SQLtoCSVDumper sqlDump = new SQLtoCSVDumper(outputFile, "jdbc:mysql://kilabdb.cs.uni-dortmund.de:3306/bruegge", "bruegge", "ls8test");
				try{
					sqlDump.executeQuery(queryString);
				} catch(FileNotFoundException e){
					System.out.println("The file could not be opend");
					e.printStackTrace();
					return;
				} catch(SQLException e){
					System.out.println("SQL exception thrown. Wrong syntax?");
					e.printStackTrace();
					return;
				} catch (Exception e) {
					System.out.println("General Exception and Major Fuckup??");
					e.printStackTrace();
					return;
				} 
				System.out.println("Success. Data written to " + outputFile.getPath());
				return;
				
	}

}
