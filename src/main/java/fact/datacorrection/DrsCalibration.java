/**
 *
 */
package fact.datacorrection;

import fact.DrsFileService;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITS;
import fact.io.hdureader.OptionalTypesMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

/**
 *
 * This processor handles the DRS calibration. It requires a DRS data source
 * either as File or URL and will read the DRS data from that. This data is then
 * applied to all FactEvents processed by this class.
 *
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 */
public class DrsCalibration implements StatefulProcessor {
	static Logger log = LoggerFactory.getLogger(DrsCalibration.class);

	@Parameter
	public String outputKey = "DataCalibrated";

	@Parameter(required = false, description = "Data array to be calibrated", defaultValue = "Data")
	private String key = "Data";

	@Parameter(required =  false, description = "A URL to the DRS calibration data (in FITS formats)",
			defaultValue = "Null. Will try to find path to drsFile from the stream.")
	public URL url = null;

	@Service(description = "A DrsFileService which helps to find drs files automagically." +
			" Either this or the url must be set.", required = false)
	private DrsFileService drsService;


	private File currentDrsFile = new File("");

	float[] drsBaselineMean;
	float[] drsBaselineRms;
	float[] drsGainMean;
	float[] drsGainRms;
	float[] drsTriggerOffsetMean;
	float[] drsTriggerOffsetRms;

	// The following keys are required to exist in the DRS data
	final static String[] drsKeys = new String[] { "RunNumberBaseline",
			"RunNumberGain", "RunNumberTriggerOffset", "BaselineMean",
			"BaselineRms", "GainMean", "GainRms", "TriggerOffsetMean",
			"TriggerOffsetRms" };

	/**
	 * This method reads the DRS calibration values from the given data source.
	 * The datasource is expected to be a FITS file that provides at least one
	 * data item/row (only the first will be read).
	 *
	 * That item/row in turn is expected to contain a set of variables, e.g. the
	 * BaselineMean, BaselineRms,...
	 *
	 * @param in
	 *            sourceurl to be loaded
	 */
	protected void loadDrsData(URL in) {
		try {
			FITS fits = new FITS(in);
			BinTable calibrationTable = fits.getBinTableByName("DrsCalibration").orElseThrow(() -> new RuntimeException("No Bintable with \"DrsCalibration\""));

			BinTableReader reader = BinTableReader.forBinTable(calibrationTable);


			OptionalTypesMap<String, Serializable> row = reader.getNextRow();

			this.drsBaselineMean = row.getFloatArray("BaselineMean").orElseThrow( ()-> new RuntimeException("File does not contain keyBaselineMean"));
			this.drsBaselineRms = row.getFloatArray("BaselineRms").orElseThrow( ()-> new RuntimeException("File does not contain key BaselineRms"));

			this.drsTriggerOffsetMean = row.getFloatArray("TriggerOffsetMean").orElseThrow( ()-> new RuntimeException("File does not contain key TriggerOffsetMean"));
			this.drsTriggerOffsetRms = row.getFloatArray("TriggerOffsetRms").orElseThrow( ()-> new RuntimeException("File does not contain key TriggerOffsetRms"));
			this.drsGainMean = row.getFloatArray("GainMean").orElseThrow( ()-> new RuntimeException("File does not contain key GainMean"));
			this.drsGainRms = row.getFloatArray("GainRms").orElseThrow( ()-> new RuntimeException("File does not contain key GainRms"));

		}catch(IOException e)
		{
			new IOException(e.getMessage());
		}

	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {

		if( this.url == null && this.drsService != null){
			//file not loaded yet. try to find through service
			try {

				DrsFileService.CalibrationInfo calibrationInfo = drsService.getCalibrationConstantsForDataItem(data);
				this.drsBaselineMean = calibrationInfo.drsBaselineMean;
				this.drsGainMean = calibrationInfo.drsGainMean;
				this.drsTriggerOffsetMean = calibrationInfo.drsTriggerOffsetMean;

			} catch (NullPointerException | IOException e) {
				throw new RuntimeException("Could not get calibration info from service. \n" + e.getMessage());
			}
		}

		log.debug("Processing Data item by applying DRS calibration...");
		short[] rawData = (short[]) data.get(key);
		if (rawData == null) {
			log.error(" data .fits file did not contain the value for the key "
					+ key + ". cannot apply drscalibration");
			throw new RuntimeException(
					" data .fits file did not contain the value for the key \"" + key + "\". Cannot apply drs calibration)");
		}

		double[] rawfloatData = new double[rawData.length];
		// System.arraycopy(rawData, 0, rawfloatData, 0, rawfloatData.length);
		for (int i = 0; i < rawData.length; i++) {
			rawfloatData[i] = rawData[i];
		}

		short[] startCell = (short[]) data.get("StartCellData");
		if (startCell == null) {
			log.error(" data .fits file did not contain startcell data. cannot apply drscalibration");
			return null;
		}
		log.debug("raw data has {} elements", rawData.length);
		log.debug("StartCellData has {} elements", startCell.length);

		double[] output = rawfloatData;
		if (!key.equals(outputKey)) {
			output = new double[rawData.length];
		}

		double[] calibrated = applyDrsCalibration(rawfloatData, output,	startCell);

		data.put(outputKey, calibrated);

		// add color value if set

		return data;
	}

	public double[] applyDrsCalibration(double[] data, double[] destination,
										short[] startCellVector) {

		if (destination == null || destination.length != data.length)
			destination = new double[data.length];
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

		double dconv = 2000.0f / 4096.0f;
		double vraw;

		int pos, offsetPos, triggerOffsetPos;
		for (int pixel = 0; pixel < 1440; pixel++) {
			for (int slice = 0; slice < roi; slice++) {

				pos = pixel * roi + slice;
				// Offset and Gain vector *should look the same
				int start = startCellVector[pixel] != -1 ? startCellVector[pixel]
						: 0;

				offsetPos = pixel * drsBaselineMean.length / 1440
						+ ((slice + start) % (drsBaselineMean.length / 1440));

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


	@Override
	public void init(ProcessContext processContext) throws Exception {
//        if(url == null && drsService == null){
//            log.error("Url and Service are not set. You need to set one of those");
//            throw new IllegalArgumentException("Wrong parameter");
//        }
		if (url != null) {
			try {
				loadDrsData(url);
			} catch (Exception e) {
				log.error("Could not load .drs file specified in the url.");
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	@Override
	public void resetState() throws Exception {

	}

	@Override
	public void finish() throws Exception {

	}




}