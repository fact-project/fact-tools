package fact.io;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractLineStream;
import stream.io.SourceURL;
import fact.Constants;
import fact.data.EventUtils;
import fact.utils.FactEvent;

/**
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class ReadMCcsv extends AbstractLineStream {

	static Logger log = LoggerFactory.getLogger(ReadMCcsv.class);
//	CsvWriter writer = null;

	private String template = " ";
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}

	String[] keys = {"EventNumber","TriggerNum" };
	public String[] getKeys() {
		return keys;
	}
	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	
	List<Processor> preProcessors = new ArrayList<Processor>();
//	Writer fw = null;
//	Reader fr = null;
//	private boolean firstline = true;

	//system independent newline char. hopefuly
	public static String newline = System.getProperty("line.separator");
	//Default path to gnuplot

	
	private String fileUrl = null;
	public String getFileUrl() {
		return fileUrl;
	}
	@Parameter(required = true, description = "The path where this class will look for the .csv file holding the data.")
	public void setFileUrl(String gnuPlotPath) {
		this.fileUrl = gnuPlotPath;
	}
	
	private String delimiter = " ";
	public String getDelimiter() {
		return delimiter;
	}
	@Parameter(required = false, description = "The delimiter String that seperates values in the .csv File", defaultValue=" ")
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	private String commentString = "#";
	public String getCommentString() {
		return commentString;
	}
	@Parameter(required = false, description = "The String that indicates a comment line", defaultValue="###")
	public void setCommentString(String commentString) {
		this.commentString = commentString;
	}
	
//	private boolean calibrate = true;
//	public boolean isCalibrate() {
//		return calibrate;
//	}
//	@Parameter(required = false, description = "Inidcates wether or not the MC data should be calibrated with the default values", defaultValue="true")
//	public void setCalibrate(boolean calibrate) {
//		this.calibrate = calibrate;
//	}

	private LinkedHashMap<String, Serializable> lm = null;
	private LinkedList<String> mcLabelNames = new LinkedList<String>();
//	private long eventCounter = 0;
//	private BufferedReader bfrdReader;
	private double[] data;
//	private URL url = null;
	private int eventNumber;
	
	public ReadMCcsv(SourceURL url){
		super(url);
	}
	public ReadMCcsv(InputStream in){
		super(in);
	}
	
	
//	public Map<String, Class<?>> getAttributes() {
//		HashMap<String ,  Class<?> > m = new HashMap<String ,  Class<?> >();
//		m.put("data", data.getClass());
//		return null;
//	}
	@Override
	public void init() throws Exception {
//		try{
////			URI uri = new URI(fileUrl);
//			File file = null;
//			if (url != null){
//				file = new File(url.toURI());
//			}
//			
//			if(fr == null){
//				fr = new FileReader(file);
//			}
//			bfrdReader = new BufferedReader(fr);
//		} catch(URISyntaxException ue){
//			log.info("Syntax error in fileURL: " + ue.toString());
//		} catch(FileNotFoundException fe){
//			log.info("File not found: " + fe.toString());
//		} catch(NullPointerException e) {
//			log.info("The fileUrl string is empty. You have to specify the Path to the .csv in order to use this processor.");
//		}
		
		//read the header
		lm = new LinkedHashMap<String, Serializable>();
		String line;
		boolean stop = false;
		while(!stop && (line = reader.readLine()) != null  ) {
			//extract attributes
			if(line.startsWith("### [RunHeader]") ){
				//read the attributeNames from the next line
				String attLine = reader.readLine();
				StringTokenizer t = new StringTokenizer(attLine, delimiter);
				//skip the first character which is a '#'
				t.nextToken();
				
				//now read the values in the line below
				String attValueLine = reader.readLine();
				StringTokenizer tV = new StringTokenizer(attValueLine, delimiter);
				
				//put everything into a map
				while (t.hasMoreElements()){
					String valueString = tV.nextToken();
					String name = t.nextToken();
					//these are all doubles except for the sourceType String which is also in this line for some reason.
					double val;
					try{
						val = Double.parseDouble(valueString);
						lm.put(name, val);
						attributes.put(name, Double.class);
					} catch(NumberFormatException eV) {
						lm.put(name, valueString);
						attributes.put(name, valueString.getClass());
					}
				}				
			}
			else if (line.startsWith("### [EventHeader]")){
				//read the attributeNames from the next line
				String attLine = reader.readLine();
				StringTokenizer t = new StringTokenizer(attLine, delimiter);
				//put everything into the list of labelnames, the values are different for each map. 
				//skip the first character which is a '#'
				t.nextToken();
				
				while (t.hasMoreElements()){
//					lm.put(t.nextToken(),null);
					mcLabelNames.add(t.nextToken());
				}		
			
			} else if(line.startsWith("### [RawData]")) {
				//read first line of the event containing the MC values and skip the row containg names for each pixel
				reader.readLine();
				stop = true;
			}
		}

	}
	@Override
	public Data readNext() throws Exception {
			return readNext(DataFactory.create());
	}
	
	public Data readNext(Data datum) throws Exception {
		int roi = 150;
		//1-D array that holds #roi values for each pixel. 
		data = new double[Constants.NUMBEROFPIXEL*roi];
		
		//add the header information to the event data item
		datum.putAll(lm);

		//the bfrdReaders filepointer should now be at the line containing the labelValues
		
		String mcLine = reader.readLine();
		StringTokenizer tLabel = new StringTokenizer(mcLine, delimiter);
		//skip the first character which is a '#'
//		tLabel.nextToken();
		//put everything into a map, the values are different for each event. 
		for(String name : mcLabelNames){
			double labelValue;
			try{
				labelValue = Double.parseDouble(tLabel.nextToken());
			} catch (NumberFormatException e){
				log.info("Can't parse scientific double notation. Yet. to be fixed later. Value is being set to 0 for now ");
				labelValue = 0;
			} 
			datum.put(name, labelValue);
			attributes.put(name, Double.class);
		}
		
		//go through 1440 lines
		//first item will be the EventNumber. second will be the pixelID as a softID!!
		String line;
		int pixelCounter = 0;
		try {
			while(pixelCounter < Constants.NUMBEROFPIXEL && (line = reader.readLine()) != null  ) {
				pixelCounter++;
				StringTokenizer t = new StringTokenizer(line, delimiter);
				
				//check if theres a value for each pixel in the line. first 2 items are eventnumber and pixelID hence the +2
				if (t.countTokens() != roi + 2){
					//apparently not
					log.info("Unexpected number of Values in a line. Make sure the roi (Region of Interest) is set to the correct value. PixelNum  " +(pixelCounter -1) );
					return null;
				}
				//read eventnumber
				//TODO: this needs to be done only once
				eventNumber = 	Integer.parseInt(t.nextToken());
				//read pixelNumber
				int softId = Integer.parseInt(t.nextToken());
				
				int pos = FactEvent.PIXEL_MAPPING.getChidID(softId)*roi;
				while (t.hasMoreElements()){
					data[pos] =  Double.parseDouble(t.nextToken());
					pos++;
				}
			}
		} catch (NumberFormatException de){
			log.info("Could not parse the value in the csv to a Double");
			return null;
		}
//		eventCounter++;
		datum.put("EventNum", eventNumber);
		attributes.put("EventNum", int.class);
		datum.put("DataMC", EventUtils.doubleToFloatArray(data));
		attributes.put("DataMC",float[].class);
		return datum;
	}
	
	public List<Processor> getPreprocessors() {
		return preProcessors;
	}

	
}
