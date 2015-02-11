package fact.filter;

import fact.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.net.URL;
import java.util.TreeMap;
import java.util.ArrayList;

/**
 * <p>
 * This processor handles the Gps time correction. It requires a gps time 
 * corrected events file either as URL and will read the Unix time 
 * data ( unix time seconds, unix time microseconds ) from that. The corrected time 
 * datum is then applied to the corresponding FactEvent processed by the
 * process method.
 * </p>
 * 
 * @author Max Ahnen &lt;mknoetig@phys.ethz.ch&gt;
 * 
 */

public class GpsTimeCorrection implements Processor {
	
	static Logger log = LoggerFactory.getLogger(GpsTimeCorrection.class);

	@Parameter(required=true,description="Key of the GpsUnixTimeUTC[2] output")
	private String outputKey;
	
	/** 
	 * This is the structure to hold the gps times in [int,int]: [seconds microseconds]. 
	 * It is filled by the constructor and 
	 * can be accessed by the key: EventNum
	 */ 
	private TreeMap<Integer, Integer[]> gpsTimes = new TreeMap<Integer, Integer[]>;

	/**
	 * This is the method called in the process loop.
	 * All it does is to call gpsTimes with the EventNum and get back the 
	 * teo ints corresponding to the GPS corrected unix time
	 */
	@Override
	public Data process(Data input) {		
		//Check that the eventnum exists
		Utils.isKeyValid(input, "EventNum", Integer.class); 

		//Check that the trigger type is 4, equivalent to the physics trigger
		if (input.get("TriggerType") != 4 ) {
			log.error("Non-pysics trigger type detected. Please cut on physics triggers first.");
			throw new RuntimeException(
					"Non-pysics trigger type detected. Please cut on physics triggers first.");
		}

		Integer[] bufGpsTimes = gpsTimes.get( input.get("EventNum") );
		//check that the correct files were chosen
		if (bufGpsTimes==null)
		{
			log.error("Couldn't find corresponding EventNum. Be sure to have equal gpsEventTiming file and run file.");
			throw new RuntimeException(
					"Couldn't find corresponding EventNum. Be sure to have equal gpsEventTiming file and run file.");
		}

		input.put(outputKey, bufGpsTimes);
		return input;
	}

	// This method is called from the setter that is called in the beginning.
	// It loads the SourceURL file into memory for further processing.
	protected void loadGpsTimeCorrection(SourceURL  in) {
		try {
			//open file and get the data as a list
			ArrayList<String> stringData = getTimeDataFromFile( in.getUrl() );

			//make it a nice integer list


			//if the data turns out to be badly fitted ( one of the three tests failed 
			// -> throw new RuntimeException )
			

			//load the corrected values into an array of two ints: Unix timestamp[seconds, microseconds].
			//somehow similar to this: this.gpsTimes = (double[]) drsTimeData.get(drsTimeKey);

		} catch (Exception e) {

			log.error("Failed to load Gps corrected data: {}", e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();
			
			this.gpsTimes = null;
			throw new RuntimeException(e.getMessage());
		}
	}

	/*
	 * This method actually openes the data file and gets the times out from it. 
	 */
	private ArrayList<String> getTimeDataFromFile(String fileName) {
		
		String lineData;
		List<String> fileContents = new ArrayList<String>();
		BufferedReader gpsFile=null;
		try{

			Integer lineIndex = 0;
			gpsFile = new BufferedReader (new FileReader (new File ( fileName )));

			while ( lineIndex <= 68 ) { //jump over not needed content
				lineData = gpsFile.readLine();
				lineIndex += 1;
			}				
			if ( (lineData = gpsFile.readLine()) !=	
				"#Bootstrapped |Mean Delta T| < 5e-6s && |Sigma Delta T| < 10e-6s ? -> YES, PASSED\n" ){
				log.error("linedata:  " + lineData);
				throw new RuntimeException("Bootstrapped |Mean Delta T| < 5e-6s && |Sigma Delta T| < 10e-6s test not passed.");
			}
			if( (lineData = gpsFile.readLine()) != 
				"#All p-values >= 0.0001 ? -> YES, PASSED\n" ){
				log.error("linedata:  " + lineData);
				throw new RuntimeException("At least one p-value < 0.0001");
			}
			if( (lineData = gpsFile.readLine()) != 
							"#All toothgaps found? -> YES, PASSED\n" ){
				log.error("linedata:  " + lineData);
				throw new RuntimeException("Not all toothgaps found");
			}
			lineIndex += 3;

			//jump some more
			while ((lineData = gpsFile.readLine()) != "#Reconstructed timing DATA events:\n")
			{
				lineIndex += 1;
			}
			lineData = gpsFile.readLine() //jump over one last unneccessary line
			while ((lineData = moFile.readLine()) != null)
			{
				lineIndex += 1;
				fileContents.add(lineData);
			}
		} 
		catch(IOException e) {
			log.error("Exception caught while reading gps event time correction data file: " + fileName + e.toString());
		}
		finally{
			gpsFile.close();
		}
		return fileContents;
	}

	/*
	 * this method makes a nice 3 * NumDataEvts int map from the string List
	 */
	private TreeMap<Integer, Integer[]> getTimeIntsFromStringList( ArrayList<String> stringList ) {

		TreeMap<Integer, Integer[]> listContents = new TreeMap<Integer, Integer[]>;
		
		//split the strings
		listContents.add(key,column);
	}

	// ----------------- getters and setters -------------------
	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public void setUrl(URL url) {
		try 
		{
			loadGpsTimeCorrection(new SourceURL(url));
		} catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}

//#Bootstrapped |Mean Delta T| < 5e-6s && |Sigma Delta T| < 10e-6s ? -> YES, PASSED
//#All p-values >= 0.0001 ? -> YES, PASSED
//#All toothgaps found? -> YES, PASSED

//#Reconstructed timing DATA events:
