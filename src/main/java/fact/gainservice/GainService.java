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
import java.time.ZonedDateTime;
import java.util.TreeMap;

public class GainService implements Service{

    @Parameter(defaultValue = "classpath:/gains_20130825-20170917.fits.gz")
    public String gainFile = "classpath:/gains_20130825-20170917.fits.gz";

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

        URL url;
        if (gainFile.startsWith("classpath:")) {
            url = GainService.class.getResource(gainFile.substring(10));
            System.out.println(url);
        } else {
            url = new URL(gainFile);
        }

        FITS fits = new FITS(url);

        BinTable table = fits.getBinTableByName("Gain").orElseThrow(
                () -> new IOException("Gain file has no BinTable 'Gain'")
        );
        BinTableReader reader = BinTableReader.forBinTable(table);

        gains = new TreeMap<>();
        OptionalTypesMap<String, Serializable> row;
        while (reader.hasNext()) {
            row = reader.getNextRow();

            ZonedDateTime timestamp = ZonedDateTime.parse(row.getString("timestamp").orElseThrow(
                    () -> new RuntimeException("Column 'timetstamp' not in row"))
            );
            double[] gain = row.getDoubleArray("Gain").orElseThrow(
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
