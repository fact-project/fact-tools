/**
 * 
 */
package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author chris
 * 
 */
public class SerializedEventStream extends AbstractStream {

	static Logger log = LoggerFactory.getLogger(SerializedEventStream.class);
	Map<String, Class<?>> header;
	File fitsFile;
	ObjectInputStream ois;
	List<Processor> preProcessors = new ArrayList<Processor>();
	List<String> cols = new ArrayList<String>();
	String id;

	/**
	 * @param url
	 * @throws Exception
	 */
	public SerializedEventStream(File file) throws Exception {
		this.fitsFile = file;

		InputStream in;
		if (file.getName().endsWith(".gz")) {
			in = new GZIPInputStream(new FileInputStream(file));
		} else {
			in = new FileInputStream(file);
		}

		ois = new ObjectInputStream(in);
	}

	public SerializedEventStream(SourceURL sUrl) throws Exception {
		this(new File(sUrl.getFile()));
		// super(sUrl);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/**
	 * @see stream.io.DataStream#getAttributes()
	 */
	// @Override
	// public Map<String, Class<?>> getAttributes() {
	// return this.header;
	// }

	/**
	 * @see stream.io.DataStream#readNext()
	 */
	@Override
	public Data readNext() throws Exception {
		return readNext(DataFactory.create());
	}

	/**
	 * @see stream.io.DataStream#readNext(stream.Data)
	 */
	public Data readNext(Data datum) throws Exception {

		Data item = (Data) ois.readObject();
		if (item == null) {
			return null;
		}

		datum.putAll(item);

		for (Processor proc : preProcessors) {
			datum = proc.process(datum);
		}

		return datum;
	}

	/**
	 * @see stream.io.DataStream#getPreprocessors()
	 */
	// @Override
	// public List<Processor> getPreprocessors() {
	// return this.preProcessors;
	// }

	// public static void main(String[] args) throws Exception {
	//
	// File testFile = new File("20111120.FSC_CONTROL_TEMPERATURE.fits");
	// testFile = new File("/Users/chris/ISDC-Workshop/20111122_059.fits");
	// testFile = new File("/Volumes/chris/FACT_DATA/20111122_059.fits");
	// SerializedEventStream stream = new SerializedEventStream(testFile);
	//
	// stream.getPreprocessors().add(new MapKeys("Time", "@time"));
	// stream.getPreprocessors().add(new MJDMapper("@time", "@unixtime"));
	//
	// Data item = stream.readNext();
	// int cnt = 0;
	// CsvWriter dsw = new CsvWriter(new File("events.dat"));
	//
	// while (item != null && cnt++ < 3) {
	// log.info("item: {}", item);
	// item = stream.readNext();
	//
	// short[] slices = (short[]) item.get("Data");
	// short[] startTimes = (short[]) item.get("StartCellData");
	//
	// for (int row = 0; row < 1440; row++) {
	// Data event = DataFactory.create();
	// event.put("eventNum", item.get("EventNum"));
	// event.put("pixel", row + "");
	// // log.info( "Row {} => pixel {}", row, item.get( "pixel" ) );
	// event.put("startCellData", startTimes[row]);
	// for (int s = 0; s < 300; s++) {
	// event.put("slice" + s, slices[row * 300 + s]);
	// }
	// dsw.process(event);
	// }
	// }
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see stream.io.DataStream#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see stream.io.DataStream#init()
	 */
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub

	}
}
