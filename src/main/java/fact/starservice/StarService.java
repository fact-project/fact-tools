package fact.starservice;


import fact.Constants;
import fact.coordinates.EquatorialCoordinate;
import fact.coordinates.HorizontalCoordinate;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITS;
import fact.io.hdureader.OptionalTypesMap;
import stream.annotations.Parameter;
import stream.service.Service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class StarService implements Service {

    Star[] catalog;

    public StarService() {
        try {
            catalog = loadCatalog();
        } catch (IOException e) {
            throw new RuntimeException("Error loading catalog", e);
        }
    }

    public Star[] getStarsInFov(EquatorialCoordinate pointing, double maxMagnitude) {
        return Arrays.stream(catalog)
                .filter(c -> c.equatorialCoordinate.greatCircleDistanceDeg(pointing) <= (0.5 * Constants.FOV_DEG))
                .filter(c -> c.magnitude <= maxMagnitude)
                .toArray(Star[]::new);
    }

    public static Star[] loadCatalog() throws IOException {
        FITS fits = new FITS(StarService.class.getResource("/hipparcos.fits.gz"));
        BinTable table = fits.getBinTableByName("I_239_hip_main").orElseThrow(IOException::new);
        BinTableReader reader = BinTableReader.forBinTable(table);

        ArrayList<Star> stars = new ArrayList<>();
        for (OptionalTypesMap<String, Serializable> row: reader) {
            double ra = row.getDouble("RAICRS").orElseThrow(IOException::new);
            double dec = row.getDouble("DEICRS").orElseThrow(IOException::new);
            double vmag = row.getFloat("Vmag").orElseThrow(IOException::new);
            int id = row.getInt("HIP").orElseThrow(IOException::new);
            EquatorialCoordinate coord = EquatorialCoordinate.fromDegrees(ra, dec);
            stars.add(new Star(coord, vmag, id));
        }

        return stars.toArray(new Star[]{});
    }

    public static class Star implements  Comparable<Star> {
        public final EquatorialCoordinate equatorialCoordinate;
        public final double magnitude;
        public final int id;

        public Star(EquatorialCoordinate equatorialCoordinate, double magnitude, int id) {
            this.equatorialCoordinate = equatorialCoordinate;
            this.magnitude = magnitude;
            this.id = id;
        }

        @Override
        public int compareTo(Star star) {
            return Double.compare(magnitude, star.magnitude);
        }
    }


    @Override
    public void reset() throws Exception {

    }
}
