/**
 * 
 */
package fact.io;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import stream.Data;
import stream.Processor;
import stream.annotations.Description;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.SourceURL;
import fact.data.DrsCalibration;
import fact.data.EventKey;
import fact.data.FactEvent;
import fact.tools.FitsExplore;

/**
 * @author chris
 * 
 */
@Description(name = "FACT Event Stream", group = "Data Stream.Sources")
public class FactEventStream extends FitsDataStream {

	File drsFile = null;
	DrsCalibration drsCalibration = null;
	private String src;
	private int run;

	/**
	 * @param url
	 * @throws Exception
	 */
	public FactEventStream(SourceURL sUrl) throws Exception {
		super(sUrl);
		url = sUrl;
		src = url.getFile();
		run = -1;
		try {
			run = FitsExplore.extractRun(new File(url.getFile()).getName());
		} catch (Exception e) {
			run = -1;
		}

		// getPreprocessors().add(new Short2FloatData());
		// getPreprocessors().add(new EventKeyCreator(src, run));
	}

	/**
	 * @return the drsFile
	 */
	public File getDrsFile() {
		return drsFile;
	}

	/**
	 * @param drsFile
	 *            the drsFile to set
	 */
	@Parameter(required = false)
	public void setDrsFile(File drsFile) {

		try {

			DrsCalibration calibration = new DrsCalibration();
			calibration.setDrsFile(drsFile.getAbsolutePath());

			// if (drsCalibration != null)
			// preProcessors.remove(drsCalibration);

			drsCalibration = calibration;
			// getPreprocessors().add(drsCalibration);

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

		this.drsFile = drsFile;
	}

	/**
	 * @see fact.io.FitsDataStream#readNext()
	 */
	@Override
	public synchronized Data readNext() throws Exception {
		Data data = readNext(DataFactory.create());
		if (data == null)
			return null;

		data = (new Short2FloatData()).process(data);

		if (drsCalibration != null) {
			data = drsCalibration.process(data);
		}
		data = (new EventKeyCreator(src, run).process(data));

		return data;

	}

	public static class Short2FloatData implements Processor {
		@Override
		public Data process(Data data) {
			short[] dat = (short[]) data.get("Data");
			if (dat != null) {
				float[] values = new float[dat.length];
				for (int i = 0; i < dat.length; i++) {
					values[i] = dat[i];
				}
				log.debug("Converted short[] data to float[] data...");
				data.put("Data", values);
			}
			return data;
		}
	}

	public class EventKeyCreator implements Processor {
		final String source;
		final Integer run;

		public EventKeyCreator(String src, Integer run) {
			this.source = src;
			this.run = run;
		}

		/**
		 * @see stream.DataProcessor#process(stream.Data)
		 */
		@Override
		public Data process(Data data) {

			if (data instanceof FactEvent) {
				log.debug("Processing a real 'FactEvent' object!");
			}

			try {
				log.debug("Creating key for {}", data);
				int[] utc = (int[]) data.get("UnixTimeUTC");
				Date date = new Date(utc[0] * 1000L);
				Integer num = new Integer(data.get("EventNum").toString());

				EventKey key = new EventKey(date, run, num);
				String[] split = source.split(File.separator);
				int depth = split.length;
				String directory = "";
				int counter = 0;
				for (int i = depth - 1; i > 0; i--) {
					if (!split[i - 1].isEmpty()) {
						directory = split[i - 1] + File.separator + directory;
						counter++;
					}
					if (counter == 3)
						break;
				}
				directory = File.separator + directory;
				// String directory = split[depth-4]+ File.separator +
				// split[depth-3]+ File.separator + split[depth-2];
				log.debug("EventKey is: {}", key.toString());
				data.put("@id", key.toString());
				data.put("@day",
						(new SimpleDateFormat("yyyy/MM/dd")).format(date));
				data.put("@directory", directory);
				data.put("@date", date);
				data.put("@run", run.intValue());
				// data.put("@runId", src);

				if (source != null)
					data.put("@source", source);

			} catch (Exception e) {
				log.error("Error: " + e.getMessage());
			}

			return data;
		}
	}
}
