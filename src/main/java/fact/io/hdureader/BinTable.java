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
 *
 * The data in this table can be read using a TableReader instance.
 * @see BinTableReader
 *
 * A BinTable is created from a Header object, the offset the of the HDU this table belongs to and  the URL of the file
 * so new  inputStreams can be created.
 *
 * Note: Not all things in the FITS standard are supported e.g. complex numbers !
 *
 * Created by kai on 04.11.16.
 */
public class BinTable {

    private static Logger log = LoggerFactory.getLogger(BinTable.class);

    final DataInputStream tableDataStream;
    DataInputStream heapDataStream = null;


    /**
     * This enum maps the type characters in the header to the fits types.
     * Not all types from the fits standard are supported here.
     *
     * Note: support variable length arrays.
     *
     */
    enum ColumnType {
        BOOLEAN("L", Boolean.class, 1),
        CHAR("A", Character.class, 1),
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
        public static ColumnType typeForChar(String type){
            for(ColumnType ct : ColumnType.values()){
                if (type.equals(ct.typeString)){
                    return  ct;
                }
            }
            return ColumnType.NONE;
        }

    }


    /**
     * Representation of column meta data for a bintable.
     * Keeps information like the repeatcount, type and name of the column.
     */
    class TableColumn{

        int repeatCount = 0;
        ColumnType type;
        String name;

        /**
         * Create a TableColumn from the tform and ttype header information.
         * @param tform the headerline specifying the type of the data.
         * @param ttype the headerline specifying the name of the column.
         */
        TableColumn(HeaderLine zform, HeaderLine tform, HeaderLine ttype){
            if (zform != null) {
                setTypeAndCount(zform);
            } else {
                setTypeAndCount(tform);
            }
            this.name = ttype.value;
        }
        private void setTypeAndCount(HeaderLine form){
            Matcher matcher = Pattern.compile("(\\d+)([LABIJKED])").matcher(form.value);
            if (matcher.matches()){
                this.repeatCount = Integer.parseInt(matcher.group(1));
                this.type = ColumnType.typeForChar(matcher.group(2));
            }
        }


    }

    List<TableColumn> columns = new ArrayList<>();

    public Integer numberOfRowsInTable = 0;
    public Integer numberOfColumnsInTable = 0;
    public final String name;


    BinTable(Header header, long hduOffset, URL url) throws IllegalArgumentException, IOException {

        binTableSanityCheck(header);

        this.name = header.get("EXTNAME").orElse("");

        Set<Map.Entry<String, HeaderLine>> entries = header.headerMap.entrySet();

        List<Map.Entry<String, HeaderLine>> ttypes = entries.stream()
                .filter(entry -> entry.getKey().matches("TTYPE\\d+"))
                .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey().substring(5))))
                .collect(Collectors.toList());


        ttypes.forEach(entry -> {
            final int n = Integer.parseInt(entry.getKey().substring(5));
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


            columns.add(new TableColumn(zform, tform, entry.getValue()));
        });


        numberOfRowsInTable = header.getInt("NAXIS2").orElse(0);
        numberOfColumnsInTable = columns.size();


        // create readers for the data in this table
        //create stream for data in table area of the bintable extension
        DataInputStream stream = Fits.getDecompressedDataStream(url);

        long skippedBytes = stream.skipBytes((int) (hduOffset + header.headerSizeInBytes));
        if(skippedBytes != hduOffset + header.headerSizeInBytes){
            throw new IOException("Could not skip all bytes to table data in this HDU.");
        }
        this.tableDataStream = stream;


        //check if this bintable has data following after the table. See section 7.3.1 in the fits standard
        long pcount = header.getLong("PCOUNT").orElse(0L);

        if (pcount > 0) {
            Integer naxis1 = header.getInt("NAXIS1").orElse(0);
            Integer naxis2 = header.getInt("NAXIS2").orElse(0);

            int theap = header.getInt("ZHEAPPTR").orElseGet(() -> header.getInt("THEAP").orElse(naxis1*naxis2));

            int bytesToSkip = Math.toIntExact((hduOffset + header.headerSizeInBytes + theap));

            DataInputStream hStream = Fits.getDecompressedDataStream(url);
            skippedBytes = hStream.skipBytes(bytesToSkip);

            if (skippedBytes != bytesToSkip) {
                throw new IOException("Could not skip all bytes to heap area of the bintable in this HDU.");
            }
            this.heapDataStream = hStream;
        }
    }


    /**
     * Perform some sanity checks on the bintable.
     *  1. The TFIELDS keyword must exist.
     *  2. The number of columns stored in the TFIELD line has to fit the number
     *     of TTPYE fields
     *  3. Make sure the NAXIS2 keyword exists. It gives the number of rows.
     *  4. NAXIS2 needs to be non-negative.
     *
     * @param header the header to check
     */
    private void binTableSanityCheck(Header header) {
        int tfields = header.getInt("TFIELDS").orElseThrow(() -> {
            log.error("The TFIELDS keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.2.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing TFIELD keyword");
        });

        int naxis1 = header.getInt("NAXIS1").orElseThrow(() -> {
            log.error("The NAXIS1 keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.3.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing NAXIS2 keyword");
        });

        if(naxis1 < 0){
            throw new IllegalArgumentException("Number of rows (NAXIS1) is negative.");
        }

        int naxis2 = header.getInt("NAXIS2").orElseThrow(() -> {
            log.error("The NAXIS2 keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.3.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing NAXIS2 keyword");
        });

        if(naxis2 < 0){
            throw new IllegalArgumentException("Number of rows (NAXIS2) is negative.");
        }

        long pcount = header.getLong("PCOUNT").orElseThrow(() -> {
            log.error("The PCOUNT keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.3.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing PCOUNT keyword");

        });

        if(pcount < 0){
            throw new IllegalArgumentException("Number of bytes in Heap (PCOUNT) is negative.");
        }

        if (pcount > 0){
            int theap = header.getInt("THEAP").orElseThrow(() -> {
                log.error("The THEAP keyword cannot be found in the BinTable. " +
                        "Its mandatory when PCOUNT is larger than 0.\nSee section 7.3.2 of the Fits 3.0 standard");
                return new IllegalArgumentException("Missing THEAP keyword");

            });

            if(theap < 0){
                throw new IllegalArgumentException("Number of bytes in to skip to the heap (THEAP) is negative.");
            }

            int desiredTheap = naxis1*naxis2 + (naxis1 * naxis2) % 2880;
            if(theap == desiredTheap){
                throw new IllegalArgumentException("The value for THEAP should be equal to (NAXIS1*NAXIS2 + padding to 2880 blocks)." +
                        "\nSee section 7.3.2 of the Fits 3.0 standard");
            }
        }
    }

}
