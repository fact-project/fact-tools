package fact.gainservice;

import fact.io.hdureader.BinTable;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITS;
import fact.io.hdureader.OptionalTypesMap;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import stream.service.Service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TreeMap;

public class GainService implements Service{

    @Parameter(defaultValue = "")
    public URL gainFile = GainService.class.getResource("/gains_20130825-20170917.fits.gz");

    private TreeMap<ZonedDateTime, double[]> gains;

    public double[] getGains(ZonedDateTime timestamp) {
        if (gains == null) {
            try {
                loadGains();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return gains.get(timestamp);
    }

    private void loadGains() throws Exception {
        gainFile = new URL("file:test.fits.gz");
        FITS fits = new FITS(gainFile);

        BinTable table = fits.getBinTableByName("Gain").orElseThrow(
                () -> new IOException("Gain file has no BinTable 'Gain'")
        );
        BinTableReader reader = BinTableReader.forBinTable(table);

        gains = new TreeMap<>();
        OptionalTypesMap<String, Serializable> row;
        int i = 0;
        while (reader.hasNext()) {
            row = reader.getNextRow();
            System.out.println(i++);
            String timestampString = row.getString("timestamp").orElseThrow(
                    () -> new RuntimeException("Column 'timetstamp' not in row")
            );

            ZonedDateTime timestamp = ZonedDateTime.parse(timestampString + "Z");
            double[] gain = row.getDoubleArray("gain").orElseThrow(
                    () -> new RuntimeException("Column 'Gain' not in row")
            );
            gains.put(timestamp, gain);
        }
    }


    @Override
    public void reset() throws Exception {
        gains = null;
    }
}
