package fact.gainservice;

import fact.Constants;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITS;
import fact.io.hdureader.OptionalTypesMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.CsvStream;
import stream.io.SourceURL;
import stream.service.Service;


import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TreeMap;

/**
 * This Service provides gains for each pixel for a given timestamp
 * It needs a fits file with a BinTable "Gain" which contains the columns "timestamp" and "gain"
 * timestamp needs to be an ISO-8601 datetime string and "gain" needs to be a length 1440 double array
 * with the gain values for each pixel.
 *
 * You can create this file using https://github.com/fact-project/spe_analysis
 */
public class GainService implements Service{

    @Parameter(defaultValue = "classpath:/gains_20120503-20171103.fits.gz")
    public URL gainFile = GainService.class.getResource("/gains_20120503-20171103.fits.gz");

    @Parameter(defaultValue = "classpath:/mc_gain_ceres_12.csv")
    public URL simulationGainFile = GainService.class.getResource("/mc_gain_ceres_12.csv");

    TreeMap<ZonedDateTime, double[]> gains;
    private double[] gainsSimulations = null;

    private static final Logger log = LoggerFactory.getLogger(GainService.class);

    /**
     * Get the closest gain measurement for a given timestamp
     * @param timestamp
     * @return gain array for given timestamp
     */
    public double[] getGains(ZonedDateTime timestamp) {
        if (gains == null) {
            try {
                loadGains();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Map.Entry<ZonedDateTime, double[]> ceiling = gains.ceilingEntry(timestamp);
        Map.Entry<ZonedDateTime, double[]> floor = gains.floorEntry(timestamp);

        Duration diff;
        double[] gain;

        if (ceiling == null) {
            diff = Duration.between(floor.getKey(), timestamp).abs();
            gain = floor.getValue();
        } else {
            if (floor == null) {
                diff = Duration.between(ceiling.getKey(), timestamp).abs();
                gain = ceiling.getValue();
            } else {
                Duration ceilingDiff = Duration.between(timestamp, ceiling.getKey()).abs();
                Duration floorDiff = Duration.between(timestamp, floor.getKey()).abs();

                if (ceilingDiff.compareTo(floorDiff) < 0) {
                    diff = ceilingDiff;
                    gain = ceiling.getValue();
                } else {
                    diff = floorDiff;
                    gain = floor.getValue();
                }
            }
        }

        if (diff.toDays() >= 5) {
            log.warn("Time difference to closest gain measurement is more than 5 days");
        }
        return gain;
    }


    public double[] getSimulationGains() {
        if (gainsSimulations == null) {
            loadGainsSimulations();
        }
        return gainsSimulations;
    }

    private void loadGainsSimulations() {
        double[] integralGains = new double[Constants.N_PIXELS];

        SourceURL url = new SourceURL(simulationGainFile);
        try {
            CsvStream stream = new CsvStream(url, " ");
            stream.setHeader(false);
            stream.init();
            Data integralGainData = stream.readNext();
            for (int i = 0; i < Constants.N_PIXELS; i++) {
                String key = "column:" + (i);
                integralGains[i] = (double) integralGainData.get(key);
            }
            gainsSimulations =  integralGains;
        } catch (Exception e) {
            log.error("Failed to load integral Gain data: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    void loadGains() throws Exception {
        FITS fits = new FITS(gainFile);

        BinTable table = fits.getBinTableByName("Gain").orElseThrow(
                () -> new IOException("Gain file has no BinTable 'Gain'")
        );
        BinTableReader reader = BinTableReader.forBinTable(table);

        gains = new TreeMap<>();
        OptionalTypesMap<String, Serializable> row;

        while (reader.hasNext()) {
            row = reader.getNextRow();
            String timestampString = row.getString("timestamp").orElseThrow(
                    () -> new RuntimeException("Column 'timestamp' not in row")
            );

            ZonedDateTime timestamp = LocalDateTime.parse(timestampString).atZone(ZoneOffset.UTC);
            double[] gain = row.getDoubleArray("gain").orElseThrow(
                    () -> new RuntimeException("Column 'Gain' not in row")
            );
            gains.put(timestamp, gain);
        }
    }


    @Override
    public void reset() {
        gains = null;
    }
}
