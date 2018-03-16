package fact.io.hdureader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * A BinTable representation. The rows and column in BinTables are described by the header keys in the HDU.
 * The name of this table and the number of rows and columns in this table are accessible via public members
 * name, numberOfRowsInTable and numberOfColumnsInTable.
 * <p>
 * The data in this table can be read using a TableReader instance.
 *
 * @see BinTableReader
 * <p>
 * A BinTable is created from a Header object, the offset of the HDU this table belongs to and the URL of the file
 * so new inputStreams can be created.
 * <p>
 * Note: Not all things in the FITS standard are supported e.g. complex numbers !
 * <p>
 * Created by kai on 04.11.16.
 */
public class BinTable {

    private static Logger log = LoggerFactory.getLogger(BinTable.class);

    final DataInputStream tableDataStream;
    DataInputStream heapDataStream = null;

    public final Header header;

    /**
     * This enum maps the type characters in the header to the fits types.
     * Not all types from the fits standard are supported here.
     * The first letter denotes the datatype according to the fits standard
     * The second entry maps these types to java types
     * The third entry gives the number of bytes the data type uses in the FITS standard.
     * <p>
     * Note: variable length arrays are not supported.
     */
    enum ColumnType {
        BOOLEAN("L", Boolean.class, 1),
        CHAR("A", Character.class, 1),// this is one ASCII byte in FITS. A char however is two bytes in java.
        BYTE("B", Byte.class, 1),
        SHORT("I", Short.class, 2),
        INT("J", Integer.class, 4),
        LONG("K", Long.class, 8),
        FLOAT("E", Float.class, 4),
        DOUBLE("D", Double.class, 8),
        NONE("", Void.class, 4);

        String typeString;
        Class typeClass;
        int byteSize;

        ColumnType(String typeString, Class typeClass, int byteSize) {
            this.typeString = typeString;
            this.typeClass = typeClass;
            this.byteSize = byteSize;
        }

        public static ColumnType typeForChar(String type) {
            for (ColumnType ct : ColumnType.values()) {
                if (type.equals(ct.typeString)) {
                    return ct;
                }
            }
            return ColumnType.NONE;
        }

    }


    /**
     * Representation of column meta data for a bintable.
     * Keeps information like the repeatCount, type and name of the column.
     */
    class TableColumn {

        int repeatCount = 0;
        ColumnType type;
        String name;

        /**
         * Create a TableColumn from the tform and ttype header information.
         * In FITS files that are compressed according to the TILE compression convention
         * the compressed columns have their type stored in a key called ZFORM.
         * The ZFits file format also uses these keys. Since this implementation only supports
         * plain old Binary Tables and ZFITs files (so far) we use the type stored in the ZFORM key
         * if it exits.
         *
         * @param tform the headerline specifying the type of the data.
         * @param ttype the headerline specifying the name of the column.
         */
        TableColumn(HeaderLine zform, HeaderLine tform, HeaderLine ttype) throws IOException{
            if (zform != null) {
                setTypeAndCount(zform);
            } else {
                setTypeAndCount(tform);
            }
            this.name = ttype.value;
        }

        private void setTypeAndCount(HeaderLine form) throws IOException{
            Matcher matcher = Pattern.compile("(\\d*)([LABIJKED])").matcher(form.value);
            if (matcher.matches()) {
                if (matcher.group(1).equals("")) {
                    this.repeatCount = 1;
                } else {
                    this.repeatCount = Integer.parseInt(matcher.group(1));
                }
                this.type = ColumnType.typeForChar(matcher.group(2));
            } else {
                throw new IOException("Could not parse column type '" + form.toString() + "'");
            }
        }


    }

    List<TableColumn> columns = new ArrayList<>();

    public Integer numberOfRowsInTable = 0;
    public Integer numberOfColumnsInTable = 0;
    public final String name;
    public Integer numberOfBytesPerRow = 0; // in header value: naxis1

    BinTable(Header header, long hduOffset, URL url) throws IllegalArgumentException, IOException {
        this.header = header;

        binTableSanityCheck(header);

        this.name = header.get("EXTNAME").orElse("");

        Set<Map.Entry<String, HeaderLine>> entries = header.headerMap.entrySet();

        List<Map.Entry<String, HeaderLine>> ttypes = entries.stream()
                .filter(entry -> entry.getKey().matches("TTYPE\\d+"))
                .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey().substring(5))))
                .collect(Collectors.toList());


        for (Map.Entry<String, HeaderLine> ttype: ttypes) {
            final int n = Integer.parseInt(ttype.getKey().substring(5));
            HeaderLine zform = entries.stream()
                    .filter(e -> e.getKey().matches("ZFORM" + n))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(null);

            HeaderLine tform = entries.stream()
                    .filter(e -> e.getKey().matches("TFORM" + n))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(null);


            columns.add(new TableColumn(zform, tform, ttype.getValue()));
        }

        numberOfBytesPerRow = header.getInt("ZNAXIS1").orElse(header.getInt("NAXIS1").orElse(0));
        numberOfRowsInTable = header.getInt("ZNAXIS2").orElse(header.getInt("NAXIS2").orElse(0));
        numberOfColumnsInTable = columns.size();


        // create readers for the data in this table
        //create stream for data in table area of the bintable extension
        DataInputStream stream = FITS.getUnGzippedDataStream(url);

        long skippedBytes = stream.skipBytes((int) (hduOffset + header.headerSizeInBytes));
        if (skippedBytes != hduOffset + header.headerSizeInBytes) {
            throw new IOException("Tried to skip to table data in this HDU. Should have skipped to byte" +
                    (int) (hduOffset + header.headerSizeInBytes) + " but only skipped " + skippedBytes + " in total. " +
                    "Either the files has been truncated after the header of this HDU was written or something went " +
                    "wrong while reading the file from the file system.");
        }
        this.tableDataStream = stream;


        //check if this bintable has data following after the table. See section 7.3.1 in the fits standard
        long pcount = header.getLong("PCOUNT").orElse(0L);

        if (pcount > 0) {
            Integer naxis1 = header.getInt("NAXIS1").orElse(0);
            Integer naxis2 = header.getInt("NAXIS2").orElse(0);

            // In ZFITS file the heap data starts at the offset stored in the 'ZHEAPPTR' variable in the header.
            // This is of course documented nowhere. Not even in the 'official spec' https://arxiv.org/pdf/1506.06045v1.pdf
            // Its also simply wrong according to the FITS standard. It should be the 'THEAP' variable.
            // However in this case we give more priority to the mysterious ZHEAPPTR variable if its there.
            // Heres an email I got from Etienne concerning this keyword:
            //
            //      I remember that there is a mismatch between the THEAP (or ZHEAPPTR) values that we output to our .fits.fz
            //      files and the FITS standard. At the time, it was because the FTOOLS had a bug and I had implemented the
            //      version that worked with the FTOOLS rather than the version compliant with the standard.
            //
            //
            int theap = header.getInt("ZHEAPPTR").orElseGet(() -> header.getInt("THEAP").orElse(naxis1 * naxis2));

            int bytesToSkip = Math.toIntExact((hduOffset + header.headerSizeInBytes + theap));

            DataInputStream hStream = FITS.getUnGzippedDataStream(url);
            skippedBytes = hStream.skipBytes(bytesToSkip);

            if (skippedBytes != bytesToSkip) {
                throw new IOException("Could not skip all bytes to heap area of the bintable in this HDU.");
            }
            this.heapDataStream = hStream;
        }
    }


    /**
     * Perform some sanity checks on the bintable.
     * 1. Make sure the NAXIS1 and NAXIS2 keywords exists.
     * 2. NAXIS1 and NAXIS2 need to be non-negative.
     * 3. Check whether PCOUNT exists.
     * 4. If PCOUNT > 0 check whether THEAP contains the right values according to standard.
     *
     * @param header the header to check
     */
    private void binTableSanityCheck(Header header) {

        int naxis1 = header.getInt("NAXIS1").orElseThrow(() -> {
            log.error("The NAXIS1 keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.3.1 of the FITS 3.0 standard");
            return new IllegalArgumentException("Missing NAXIS2 keyword");
        });

        if (naxis1 < 0) {
            throw new IllegalArgumentException("Number of rows (NAXIS1) is negative.");
        }

        int naxis2 = header.getInt("NAXIS2").orElseThrow(() -> {
            log.error("The NAXIS2 keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.3.1 of the FITS 3.0 standard");
            return new IllegalArgumentException("Missing NAXIS2 keyword");
        });

        if (naxis2 < 0) {
            throw new IllegalArgumentException("Number of rows (NAXIS2) is negative.");
        }

        long pcount = header.getLong("PCOUNT").orElseThrow(() -> {
            log.error("The PCOUNT keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.3.1 of the FITS 3.0 standard");
            return new IllegalArgumentException("Missing PCOUNT keyword");

        });

        if (pcount < 0) {
            throw new IllegalArgumentException("Number of bytes in Heap (PCOUNT) is negative.");
        }
        /*
            See page 25 of the Fits standard:
                The zero indexed byte offset to the start of
                the heap, measured from the start of the main data table, may
                be given by the THEAP keyword in the header
                ...
                (i.e., the default value of THEAP is NAXIS1 × NAXIS2)
                This default value is the minimum allowed
                value for the THEAP keyword, because any smaller value would
                imply that the heap and the main data table overlap.
         */
        if (pcount > 0) {
            int theap = header.getInt("THEAP").orElse(naxis1 * naxis2);

            if (theap < 0) {
                throw new IllegalArgumentException("Number of bytes to skip to the heap (THEAP) is negative.");
            }

            if (theap < naxis1 * naxis2) {
                throw new IllegalArgumentException("The value of THEAP is lower than the allowed minimum of (NAXIS1 * NAXIS2).");
            }
        }
    }

}
