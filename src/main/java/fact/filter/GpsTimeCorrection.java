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
import java.io.*;

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
	private TreeMap<Integer, Integer[]> gpsTimes = new TreeMap<Integer, Integer[]>();

	/**
	 * This is the method called in the process loop.
	 * All it does is to call gpsTimes with the EventNum and get back the 
	 * teo ints corresponding to the GPS corrected unix time
	 */
	@Override
	public Data process(Data input) {		
		//Check that the eventnum exists
		Utils.isKeyValid(input, "EventNum", Integer.class); 

		Integer triggerType = new Integer( input.get( "TriggerType").toString() );

		//Check that the trigger type is 4, equivalent to the physics trigger
		if ( !triggerType.equals(4) ) {
			log.error("Non-pysics trigger type detected: "+input.get("TriggerType")+" Please cut on physics triggers for gps time correction.");
			throw new RuntimeException(
					"Non-pysics trigger type detected. Please cut on physics triggers for gps time correction.");
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
			ArrayList<int[]> fileData = getTimeDataFromFile( in.getPath() ); //make a list of event number + the different times
            this.gpsTimes = getTimeMapFromIntList( fileData ); //copy this event number list into a map that is accessible by id: event number

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
	 * the return is a list of five integers:
	 * [0] event number
	 * [1] unixTime
	 * [2] unixTimeMicroseconds
	 * [3] correctedUnixTime
	 * [4] correctedUnixTimeMicroseconds
	 */
	private ArrayList<int[]> getTimeDataFromFile(String fileName) {
		
		String lineData;
		ArrayList<int[]> fileContents = new ArrayList<int[]>();
		BufferedReader gpsFile=null;
		try{

			Integer lineIndex = 0;
			gpsFile = new BufferedReader (new FileReader (new File ( fileName )));

			while ( lineIndex < 68 ) { //jump over not needed content
				lineData = gpsFile.readLine();
				lineIndex += 1;
			}
            lineData = gpsFile.readLine();
			if ( !lineData.equals("#Bootstrapped |Mean Delta T| < 5e-6s && |Sigma Delta T| < 10e-6s ? -> YES, PASSED") ){
                log.error("linedata:  " + lineData);
                throw new RuntimeException("Bootstrapped |Mean Delta T| < 5e-6s && |Sigma Delta T| < 10e-6s test not passed.");
			}
            lineData = gpsFile.readLine();
			if( !lineData.equals("#All p-values >= 0.0001 ? -> YES, PASSED") ){
				log.error("linedata:  " + lineData);
				throw new RuntimeException("At least one p-value < 0.0001");
			}
            lineData = gpsFile.readLine();
			if( !lineData.equals("#All toothgaps found? -> YES, PASSED") ){
				log.error("linedata:  " + lineData);
				throw new RuntimeException("Not all toothgaps found");
			}
			lineIndex += 3;

			//jump some more
			while ( true )
			{
                lineData = gpsFile.readLine();
				lineIndex += 1;
                if( lineData.equals("#Reconstructed timing DATA events:") ){
                    break;
                }
			}
			lineData = gpsFile.readLine(); //jump over one last unneccessary line
            int lastEvent = 0;
			while ( true )
			{
                int[] intBuf = { 0 , 0 , 0 , 0 , 0 }; //buffer for saving unix times
                lineData = gpsFile.readLine();
				lineIndex += 1;
                if ( lineData.equals( "#" )) { //when the end of the data block is reached, escape.
                    log.info("last event num read by gpsTimeCorrection: " + lastEvent);
                    break;
                }
                String stringBuf = lineData.substring(2);
                String[] stringParts = stringBuf.split("   ");
                lastEvent = Integer.parseInt(stringParts[0]);
                intBuf[0] = Integer.parseInt(stringParts[0]); // event num
                intBuf[1] = Integer.parseInt(stringParts[1]); // unixTime
                intBuf[2] = Integer.parseInt(stringParts[2]); // unixTimeMicroseconds
                intBuf[3] = Integer.parseInt(stringParts[3]); // correctedUnixTime
                intBuf[4] = Integer.parseInt(stringParts[4]); // correctedUnixTimeMicroseconds
				fileContents.add(intBuf);
			}
			gpsFile.close();
		} 
		catch(IOException e) {
			log.error("Exception caught while reading gps event time correction data file: " + fileName + e.toString());
		}

		return fileContents;
	}

	/*
	 * this method makes a nice 3 * NumDataEvts int map from the int List
	 * not implemented so far: check of the correct unix time (before correction)
	 */
	private TreeMap<Integer, Integer[]> getTimeMapFromIntList( ArrayList<int[]> intList ) {

		TreeMap<Integer, Integer[]> timeMap = new TreeMap<Integer, Integer[]>();
        for(int i = 0; i < intList.size(); i++){

            int[] ints = intList.get(i);
            Integer key = Integer.valueOf( ints[0] ); //copy event number into Integer type
            Integer[] column = new Integer[ 2 ];
            column[0] = Integer.valueOf( ints[3] ); //copy correctedTimes into a list of Integers ( Seconds, Microseconds )
            column[1] = Integer.valueOf( ints[4] );

            timeMap.put(key,column);
        }
		return timeMap;
	}

	// ----------------- getters and setters -------------------
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
}