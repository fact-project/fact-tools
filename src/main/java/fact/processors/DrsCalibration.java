/**
 * 
 */
package fact.processors;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import fact.Constants;
import fact.io.FitsStream;

/**
 * <p>
 * This processor handles the DRS calibration. It requires a DRS data souce
 * either as File or URL and will read the DRS data from that. This data is then
 * applied to all FactEvents processed by this class.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
public class DrsCalibration implements Processor {
	// conversion factor:
	// the input values are 12-bit short values representing measurements of
	// voltage
	final static float dconv = 2000 / 4096.0f;

	static Logger log = LoggerFactory.getLogger(DrsCalibration.class);

//	String drsFile = null;
	String fileName = "";
	String filePath = "";
	private String color;
	
	private String outputKey = "DataCalibrated";
	private String key="Data";

	Data drsData = null;

	
	String pathToAuxfiles = "/home/mackaiver/FactTest/aux/";

	float[] drsBaselineMean;
	float[] drsBaselineRms;
	float[] drsGainMean;
	float[] drsGainRms;
	float[] drsTriggerOffsetMean;
	float[] drsTriggerOffsetRms;
	float[] drsTriggerOffsetTMMean;
	float[] drsTriggerOffsetTMRms;

	// The following keys are required to exist in the DRS data
	final static String[] drsKeys = new String[] { "RunNumberBaseline",
			"RunNumberGain", "RunNumberTriggerOffset", "BaselineMean",
			"BaselineRms", "GainMean", "GainRms", "TriggerOffsetMean",
			"TriggerOffsetRms" };


	/**
	 * This method reads the DRS calibration values from the given data source.
	 * The datasource is expected to be a Fits file that provides at least one
	 * data item/row (only the first will be read).
	 * 
	 * That item/row in turn is expected to contain a set of variables, e.g. the
	 * BaselineMean, BaselineRms,...
	 * 
	 * @param in
	 */
	protected void loadDrsData(SourceURL  in) {
		try {

			FitsStream stream = new FitsStream(in);
			stream.init();
			drsData = stream.readNext();
			log.debug("Read DRS data: {}", drsData);

			// this for-loop is simply a check that fires an exception if any
			// of the expected keys is missing
			//
			for (String key : drsKeys) {
				if (!drsData.containsKey(key)) {
					throw new RuntimeException("DRS data is missing key '"
							+ key + "'!");
				}
			}

			this.drsBaselineMean = (float[]) drsData.get("BaselineMean");
			this.drsBaselineRms = (float[]) drsData.get("BaselineRms");
			this.drsTriggerOffsetMean = (float[]) drsData
					.get("TriggerOffsetMean");
			this.drsTriggerOffsetRms = (float[]) drsData
					.get("TriggerOffsetRms");
			this.drsGainMean = (float[]) drsData.get("GainMean");
			this.drsGainRms = (float[]) drsData.get("GainRms");

		} catch (Exception e) {

			log.error("Failed to load DRS data: {}", e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();

			this.drsData = null;
			this.drsBaselineMean = null;
			this.drsTriggerOffsetMean = null;

			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * @see fact.data.FactProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {
		if (this.drsData == null){
			//file not loaded yet. try to lookup path in map.
			log.warn("No url to drs file specified. trying to find one automaticly");
			String directory = (String)data.get("@directory");
			int run = (Integer)data.get("@run");
			setDrsFile(directory, run);
		}
		log.debug("Processing Data item by applying DRS calibration...");
		float[] rawData = (float[]) data.get("Data");
		log.debug("raw data has {} elements", rawData.length);

		short[] startCell = (short[]) data.get("StartCellData");
		log.debug("StartCellData has {} elements", startCell.length);

		float[] output = rawData;
		if (!key.equals(outputKey)) {
			output = new float[rawData.length];
		}

		float[] calibrated = applyDrsCalibration(rawData, output, startCell);
		
		
		
		

//		int roi = rawData.length / 1440;
//
//
//		float dconv = 2000.0f / 4096.0f;
//		float vraw;
//
//		// the vector drs_triggeroffsetmean is not 1440 * 1024 entries long
//		// but has hopefully the length 1440 * RegionOfInterest (or longer)
//		if (this.drsTriggerOffsetMean.length < 1440 * roi) {
//			throw new RuntimeException(
//					"Error: drs_triggeroffsetmean.size() < 1440*RegionOfInterest");
//		}
//
//		int pos, OffsetPos, TriggerOffsetPos;
//		for (int pixel = 0; pixel < 1440; pixel++) {
//			for (int slice = 0; slice < roi; slice++) {
//
//				pos = pixel * roi + slice;
//				// Offset and Gain vector *should look the same
//				int start =  startCell[pixel] != -1 ? startCell[pixel] : 0;
//				
//				OffsetPos = pixel * drsBaselineMean.length / 1440
//						+ ((slice + start)	% (drsBaselineMean.length / 1440));
//
//				TriggerOffsetPos = pixel * drsTriggerOffsetMean.length / 1440
//						+ slice;
//
//				vraw = rawData[pos] * dconv;
//				vraw -= drsBaselineMean[OffsetPos];
//				vraw -= drsTriggerOffsetMean[TriggerOffsetPos];
//				vraw /= drsGainMean[OffsetPos];
//				vraw *= 1907.35;
//
//
//				output[pos] = vraw;
//			}
//		}
//		float[] calibrated = output;
		
		
		
		
		
		
		
		
		

		data.put(outputKey, calibrated);
		
		//add color value if set
		if(color !=  null && !color.equals("")){
			data.put("@" + Constants.KEY_COLOR + "_"+key, color);
		}

		return data;
	}

	
	public float[] applyDrsCalibration(float[] data, float[] destination,
			short[] StartCellVector) {

		if (destination == null || destination.length != data.length)
			destination = new float[data.length];
		int roi = data.length / 1440;

		// We do not entirely know how the calibration constants, which are
		// saved in a filename.drs.fits file
		// were calculated, so it is not fully clear how they should be applied
		// to the raw data for calibration.
		// apparently the calibration constants were transformed to the unit mV,
		// which means we have to do the same to
		// the raw data prior to apply the calibration
		//
		// on the FAD board, there is a 12bit ADC, with a 2.0V range, so the
		// factor between ADC units and mV is
		// ADC2mV = 2000/4096. = 0.48828125 (numerically exact)
		//
		// from the schematic of the FAD we learned, that the voltage at the ADC
		// should be 1907.35 mV when the calibration DAC is set to 50000.
		//
		// One would further assume that the calibration constants are
		// calculated like this:

		// The DRS Offset of each bin in each channel is the mean value in this
		// very bin,
		// obtained from so called DRS pedestal data
		// Its value is about -1820 ADC units or -910mV

		// In order to obtain the DRS Gain of each bin of each channel
		// again data is takes, with the calibration DAC set to 50000
		// This is called DRS calibration data.
		// We assume the DRS Offset is already subtracted from the DRS
		// calibration data
		// so the typical value is assumed to be ~3600 ADC units oder ~1800mV
		// As mentioned before, the value *should* be 1907.35 mV
		// So one might assume that the Gain is a number, which actually
		// converts ~3600 ADC units into 1907.35mV for each bin of each channel.
		// So that the calibration procedure looks like this
		// TrueValue = (RawValue - Offset) * Gain
		// The numerical value of Gain would differ slightly from the
		// theoretical value of 2000/4096.
		// But this is apparently not the case.
		// The Gain, as it is stored in the DRS calibration file of FACT++ has
		// numerical values
		// around +1800.
		// So it seems one should calibrate like this:
		// TrueValue = (RawValue - Offset) / Gain * 1907.35

		// When these calibrations are done, one ends up with a quite nice
		// calibrated voltage.
		// But it turns out that, if one returns the first measurement, and
		// calculates the mean voltages
		// in each bin of the now *logical* DRS pipeline, the mean voltage is
		// not zero, but slightly varies
		// So one can store these systematical deviations from zero in the
		// logical pipeline as well, and subtract them.
		// The remaining question is, when to subtract them.
		// I assume, in the process of measuring this third calibration
		// constant, the first two
		// calibrations are already applied to the raw data.

		// So the calculation of the calibrated volatage from some raw voltage
		// works like this:
		// assume the raw voltage is the s'th sample in channel c. While the
		// Trigger made the DRS stopp in its t'th cell.
		// note, that the DRS pipeline is always 1024 bins long. This is
		// constant of the DRS4 chip.

		// TrueValue[c][s] = ( RawValue[c][s] - Offset[c][ (c+t)%1024 ] ) /
		// Gain[c][ (c+t)%1024 ] * 1907.35 - TriggerOffset[c][s]

		float dconv = 2000.0f / 4096.0f;
		float vraw;

		int pos, offsetPos, triggerOffsetPos;
		for (int pixel = 0; pixel < 1440; pixel++) {
			for (int slice = 0; slice < roi; slice++) {

				pos = pixel * roi + slice;
				// Offset and Gain vector *should look the same
				int start =  StartCellVector[pixel] != -1 ? StartCellVector[pixel] : 0;
				
				offsetPos = pixel * drsBaselineMean.length / 1440
						+ ((slice + start)	% (drsBaselineMean.length / 1440));

				triggerOffsetPos = pixel * drsTriggerOffsetMean.length / 1440
						+ slice;

				vraw = data[pos] * dconv;
				vraw -= drsBaselineMean[offsetPos];
				vraw -= drsTriggerOffsetMean[triggerOffsetPos];
				vraw /= drsGainMean[offsetPos];
				vraw *= 1907.35;

				// slice_pt = pixel_pt + sl;
				// drs_cal_offset = ( sl + StartCellVector[ pixel ] ) %
				// RegionOfInterest;
				// cal_pt = pixel_pt + drs_cal_offset;
				// vraw = AllPixelDataVector[ slice_pt ] * dconv;
				// vcal = ( vraw - drs_basemean[ cal_pt ] -
				// drs_triggeroffsetmean[ slice_pt ] ) / drs_gainmean[ cal_pt
				// ]*1907.35;
				// destination.push_back(vcal);

				destination[pos] = vraw;
			}
		}
		return destination;
	}
	
	
	//-----------getter setter---------------------
	
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getPathToAuxfiles() {
		return pathToAuxfiles;
	}
	public void setPathToAuxfiles(String pathToAuxfiles) {
		this.pathToAuxfiles = pathToAuxfiles;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	


	@Parameter(description = "A URL to the DRS calibration data (in FITS formats)")
	public void setUrl(URL url) {
		try {
			loadDrsData(new SourceURL(url));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	@Parameter(description = "A String with a valid URL to the DRS calibration data (in FITS formats)")
	public void setUrl(String urlString) {
		try {
			URL url = new URL(urlString);
			loadDrsData(new SourceURL(url));
		} catch (MalformedURLException e){
			log.error("Malformed URL. The URL parameter of this processor has to a be a valid url");
			throw new RuntimeException("Cant open drsFile");
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}


	private void setDrsFile(String directoryPath, int run) {
		String drsDirectory = pathToAuxfiles+directoryPath;
		File directory = new File(drsDirectory);
		File[] fList = directory.listFiles(new drsFileFilter());
		int currentRun = 0;
		File drsFile = null;
		for (File file : fList){
			currentRun = Integer.parseInt(((file.getName().split("[_/.]"))[1]));
			if (currentRun <= run){
				drsFile = file;
			}
		}
		
		try {
			loadDrsData(new SourceURL(drsFile.getAbsolutePath()));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	
}
class drsFileFilter implements FilenameFilter
{
  public boolean accept( File f, String s )
  {
    return s.toLowerCase().contains( "drs" );
  }
}
