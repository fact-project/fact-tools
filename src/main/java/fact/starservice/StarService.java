package fact.starservice;


import fact.Constants;
import fact.coordinates.EquatorialCoordinate;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITS;
import fact.io.hdureader.OptionalTypesMap;
import stream.service.Service;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This service provides access to the Yale Bright Star catalog
 * and can find stars around a position in the sky
 */
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
        FITS fits = new FITS(StarService.class.getResource("/yale_bright_stars.fits.gz"));
        BinTable table = fits.getBinTableByName("V_50_catalog").orElseThrow(IOException::new);
        BinTableReader reader = BinTableReader.forBinTable(table);

        ArrayList<Star> stars = new ArrayList<>();
        for (OptionalTypesMap<String, Serializable> row: reader) {
            double ra = row.getDouble("_RAJ2000").orElseThrow(IOException::new);
            double dec = row.getDouble("_DEJ2000").orElseThrow(IOException::new);
            double vmag = row.getFloat("Vmag").orElseThrow(IOException::new);
            int id = row.getShort("HR").orElseThrow(IOException::new);
            String name = row.getString("Name").orElseThrow(IOException::new);
            if (name.equals("")) {
                name = "HR " + id;
            }
            EquatorialCoordinate coord = EquatorialCoordinate.fromDegrees(ra, dec);
            stars.add(new Star(coord, vmag, id, name));
        }

        return stars.toArray(new Star[]{});
    }


    @Override
    public void reset() {}
}
