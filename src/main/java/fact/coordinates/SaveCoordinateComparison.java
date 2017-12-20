package fact.coordinates;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SaveCoordinateComparison {

    public class Source {
        public String name;
        public String obstime;
        public double ra;
        public double dec;
        public double zd;
        public double az;
    }

    /**
     * Reads the output of scrips/create_coordinate_comparison.py and saves the transformation result
     * to json. This can be visiualised using scripts/plot_coordinate_comparison.py
     * <p>
     * Call it like this: java -cp JARFILE fact.coordinates.SaveCoordinateComparison INPUT OUTPUT
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println("Usage: SaveCoordinateComparison <inputfile> <outputfile>");
            throw new RuntimeException("Invalid arguments");
        }

        FileInputStream inputStream = new FileInputStream(args[0]);

        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
        Source[] sources = gson.fromJson(jsonReader, Source[].class);

        JsonWriter writer = new JsonWriter(new FileWriter(args[1]));

        writer.beginArray();
        for (Source source : sources) {
            ZonedDateTime obstime = ZonedDateTime.parse(source.obstime.replace(" ", "T") + "Z[UTC]");
            EquatorialCoordinate sourceEq = EquatorialCoordinate.fromHourAngleAndDegrees(source.ra, source.dec);
            HorizontalCoordinate astropyReference = HorizontalCoordinate.fromDegrees(source.zd, source.az);
            HorizontalCoordinate sourceHz = sourceEq.toHorizontal(obstime, EarthLocation.FACT);

            writer.beginObject();
            writer.name("obstime").value(obstime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            writer.name("astropy_zd").value(source.zd);
            writer.name("astropy_az").value(source.az);
            writer.name("facttools_zd").value(sourceHz.getZenithDeg());
            writer.name("facttools_az").value(sourceHz.getAzimuthDeg());
            writer.name("distance").value(sourceHz.greatCircleDistanceDeg(astropyReference));
            writer.endObject();
        }
        writer.endArray();
        writer.close();

    }
}
