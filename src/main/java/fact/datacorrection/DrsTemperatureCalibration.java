package fact.datacorrection;

import com.google.common.collect.ImmutableMap;
import fact.Utils;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.auxservice.strategies.Closest;

import fact.io.hdureader.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;
import stream.io.SourceURL;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

/**
 * Created by florian on 06.12.16.
 *
 * Calibrate the data by removing the temperature dependencey of the drs4 chips.
 * Therefore the temperature of data taking is used do calculate for every drs4 cell
 * the temperature based data-offset and -gain.
 *
 * For this reason a linear model of all drs-calibration-parameter:
 * Baseline and Gain given by following relationship: amplitude = a * T + b
 * was build.
 *
 * The used ADC is a 12bit ADC, with a 2.0V range, so the
 * factor between ADC-units and mV is
 * adcCountsToMilliVolt = 2000/pow(2, 12) = 0.48828125 mV/ADC-units (numerically exact)
 *
 * Calibration:
 *   We have to subtract the data-offset from the data and
 *   to normend the difference by divide with the data-gain
 *
 *   So the total calibration scheme looks like:
 *   calibratedData = (rawData - dataOffset)/dataGain;
 *
 *  Where the gain is normed by divide with the DAC-ADC-units to a unitless value. .
 *  Given by the used 16 bit DAC with a 2.5V range and an input of 50 000 Dac-counts
 *  dacVoltage = 2500/pow(2, 16)*50000 = 1907.34863281 mV (numerically exact)
 *  DAC-ADC-units = dacVoltage/adcCountsToMilliVolt = 3906.25 ADC-units (numerically exact)
 */

// TODO fill .orElseThrow(IOException::new) with error-infos
public class DrsTemperatureCalibration implements StatefulProcessor {

    private final static Logger log = LoggerFactory.getLogger(DrsTemperatureCalibration.class);

    // TODO get this constants from a unique source
    private double adcCountsToMilliVolt = 2000.0 / 4096.0; //[mV/adc]
    private int NRCHIDS = 1440;
    private int NRCELLS = 1024;
    private int ROI = 300;

    @Parameter(description = "Key to the Data array to be calibrated", defaultValue = "Data")
    private String dataKey = "Data";

    @Parameter(description = "Key to the Calibrated-Data array", defaultValue = "DataCalibrated")
    private String outputKey = "DataCalibrated";

    @Parameter(required = true, description = "Fits file with the calibration constants",
            defaultValue = "Null. If Null process will fail in the init method")
    private SourceURL fitParameterFile = null;

    @Service(required = true, description = "Name of the service that provides aux files")
    private AuxiliaryService auxService;

    private final static AuxPointStrategy auxPointStrategy = new Closest();

    private final static String[] fitParameter = new String[]{"Slope", "Offset"};

    private int intervalNr;
    private ZonedDateTime[] intervalLimits;

    HashMap<String, float[][]> fitParameterBaseline = new HashMap<String, float[][]>();
    HashMap<String, float[]> fitParameterGain = new HashMap<String, float[]>();

    // The following keys are required to exist in the raw data
    private final static String[] dataKeys = new String[]{"UnixTimeUTC", "NPIX", "NCELLS", "NROI", "StartCellData"};

    /******************************************************************************************************************/
    @Override
    public void init(ProcessContext processContext) throws Exception {
        log.debug("----- init -----");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        if (fitParameterFile != null) {
            try {
                FITS fits = FITS.fromFile(new File(fitParameterFile.getFile()));
                HDU primaryHDU = fits.primaryHDU;

                intervalNr =  Integer.parseInt(primaryHDU.header.get("INTNR").orElseThrow(IOException::new));

                String[] dateString = {primaryHDU.header.get("LOWLIMIT").orElseThrow(IOException::new),
                        primaryHDU.header.get("UPPLIMIT").orElseThrow(IOException::new)};

                LocalDateTime[] localDateTime = {LocalDateTime.parse(dateString[0], formatter),
                        LocalDateTime.parse(dateString[1], formatter)};
                intervalLimits = new ZonedDateTime[]{ZonedDateTime.of(localDateTime[0], ZoneId.of("UTC")),
                        ZonedDateTime.of(localDateTime[1], ZoneId.of("UTC"))};

                String extName = "FitParameter";
                BinTable intervalData = fits.getHDU(extName).getBinTable();

                loadDrsFitParameterMap(intervalData);

            } catch (Exception e) {
                log.error("Could not load file '"+fitParameterFile +"' specified in the url_fitValueFile.");
                throw new RuntimeException(e.getMessage());
            }
        } else {
            log.error("fitParameterFile == null");
        }
    }

    /******************************************************************************************************************/
    private  void loadDrsFitParameterMap(BinTable intervalData)
    {
        log.debug("----- loadDrsFitParameterMap -----");
        try {
            BinTableReader reader = BinTableReader.forBinTable(intervalData);

            int totalNrOfCells = this.NRCHIDS * this.NRCELLS;
            for (String key: fitParameter) {
                fitParameterBaseline.put(key, new float[totalNrOfCells][this.ROI]);
                fitParameterGain.put(key, new float[totalNrOfCells]);
            }
            OptionalTypesMap<String, Serializable> row;
            for (int cellIdx = 0; cellIdx < totalNrOfCells; cellIdx++) {
                row = reader.getNextRow();
                for (String key: fitParameter) {
                    fitParameterBaseline.get(key)[cellIdx] = row.getFloatArray("Baseline"+key).orElseThrow(IOException::new);
                    fitParameterGain.get(key)[cellIdx] = row.getFloat("Gain"+key).orElseThrow(IOException::new);
                }
            }

            if (reader.hasNext()){
                throw new RuntimeException("FitParameter array is larger than expected");
            }

        } catch (Exception e) {

            log.error("Failed to load DRS data: {}", e.getMessage());
            if (log.isDebugEnabled())
                e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /******************************************************************************************************************/

    @Override
    public Data process(Data data) {
        log.debug("----- process -----");

        Utils.mapContainsKeys(data, dataKeys);
        Utils.mapContainsKeys(data, dataKey);

        int[] unixTimeUTC = (int[]) data.get("UnixTimeUTC");
        Instant instant = Instant.ofEpochSecond(unixTimeUTC[0],unixTimeUTC[1] * 1000);
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));

        if ((dateTime.compareTo(intervalLimits[0]) < 0) ||
                (dateTime.compareTo(intervalLimits[1]) > 0)) {
            String string = "Cant handle File: No drs fit parameters available for the given drs-file. " +
                    "The datetime '"+dateTime.toString()+"' of the drs-file is out of range [" +
                    intervalLimits[0]+","+intervalLimits[1]+"] of interval "+intervalNr;
            throw new IllegalArgumentException(string);
        }

        log.debug("Get patchTemperaure from AuxiliaryService with AuxPointStrategy {}", auxPointStrategy );
        AuxPoint auxPoint;
        try {
            auxPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.FAD_CONTROL_TEMPERATURE, dateTime, auxPointStrategy);
        } catch (IOException e) {
            log.error("Could not read aux data {}", e.toString());
            throw new RuntimeException(e);
        }

        ImmutableMap<String, Serializable> temperatureData = auxPoint.getData();
        float[] patchTemperatures = (float[]) temperatureData.get("temp");
        if(patchTemperatures.length != 160) { //TODO try to handle 82 Temperatures also
            throw new IllegalArgumentException("Cant handle File: There should be for every patch a Temperature. instant of '"+
                    String.valueOf(patchTemperatures.length)+"'");
        }

        double[] calibrated = applyDrsCalibration(data, patchTemperatures);
        data.put(outputKey, calibrated);

        return data;
    }

    private double[] applyDrsCalibration(Data data,  float[] patchTemperatures){
        log.debug("----- applyDrsCalibration -----");

        // TODO check needed (safety stuff)
        if((this.NRCHIDS != (int) data.get("NPIX")) ||
           (this.NRCELLS != (int) data.get("NCELLS")) ||
           (this.ROI != (int) data.get("NROI"))){
            log.error(" Unexpected data dimensions for NRCHIDS {}, NRCELLS {} or ROI {}. " +
                      "Expected: NRCHIDS {}, NRCELLS {} or ROI {}",
                      data.get("NPIX"), data.get("NCELLS"), data.get("NROI"),
                      this.NRCHIDS, this.NRCELLS, this.ROI);
            return null;
        }

        short[] startCellVector = (short[]) data.get("StartCellData");
        // TODO check needed (safety stuff)
        if (startCellVector == null) {
            log.error(" data .fits file did not contain startcell data. cannot apply drscalibration");
            return null;
        }

        double[] rawData = Utils.toDoubleArray(data.get(dataKey));
        // TODO check needed (safety stuff)
        if (rawData == null) {
            String errString = " data .fits file did not contain the value for the key "
                               + dataKey + ". cannot apply drsCalibration";
            log.error(errString);
            throw new RuntimeException(errString);
        }

        double vraw, offset, gain = Double.NaN;
        double[] calibrated = new double[rawData.length];
        int startCell_idx, sample_cell, sample_cell_idx, data_idx;
        for (int chid = 0; chid < NRCHIDS; chid++) {
            startCell_idx = startCellVector[chid]+ chid*NRCELLS;
            for (int sample = 0; sample < ROI; sample++) {

                sample_cell = Utils.sampleToCell(sample, startCellVector[chid], NRCELLS);
                sample_cell_idx = chid * NRCELLS + sample_cell;
                data_idx = chid * ROI + sample;

                offset  = fitParameterBaseline.get("Slope")[startCell_idx][sample] * patchTemperatures[(int)chid/9];
                offset += fitParameterBaseline.get("Offset")[startCell_idx][sample];

                gain  = fitParameterGain.get("Slope")[sample_cell_idx] * patchTemperatures[(int)chid/9];
                gain += fitParameterGain.get("Offset")[sample_cell_idx];

                vraw = rawData[data_idx];
                vraw -= offset;
                vraw /= gain;
                vraw *= adcCountsToMilliVolt;

                calibrated[data_idx] = vraw;
            }
        }
        return calibrated;
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }


    public void setDataKey(String dataKey_) {
        this.dataKey = dataKey_;
    }

    public void setOutputKey(String outputKey_) {
        this.outputKey = outputKey_;
    }

    public void setFitParameterFile(SourceURL fitParameterFile_) { this.fitParameterFile = fitParameterFile_; }

    public void setAuxService(AuxiliaryService auxService_) { this.auxService = auxService_; }
}
