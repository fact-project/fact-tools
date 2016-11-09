package fact.io.hdureader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A BinTable representation.
 * Created by kai on 04.11.16.
 */
public class BinTable {

    private static Logger log = LoggerFactory.getLogger(BinTable.class);


    /**
     * This enum maps the type characters in the header to the fits types.
     * Not all types from the fits standard are supported here.
     *
     * TODO: support variable length arrays.
     *
     */
    private enum ColumnType {
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

    private class TableColumn{

        TableColumn(HDULine tform, HDULine ttype){
            Matcher matcher = Pattern.compile("(\\d+)([LABIJKED])").matcher(tform.value);
            if (matcher.matches()){
                repeatCount = Integer.parseInt(matcher.group(1));
                type = ColumnType.typeForChar(matcher.group(2));
            }
            this.name = ttype.value;

        }
        public int repeatCount = 0;
        public ColumnType type;
        public String name;
    }

    private List<TableColumn> columns = new ArrayList<>();

    public Integer numberOfRowsInTable = 0;
    public Integer numberOfColumnsInTable = 0;
    public Reader reader;


    BinTable(HDU hdu, DataInputStream inputStreamForHDUData) throws IllegalArgumentException{

        Iterator<HDULine> tforms = hdu.header
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches("TFORM\\d+"))
                .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey().substring(5))))
                .map(Map.Entry::getValue)
                .iterator();


        Iterator<HDULine> ttypes = hdu.header
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches("TTYPE\\d+"))
                .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey().substring(5))))
                .map(Map.Entry::getValue)
                .iterator();
//

        //no zipping in java yet. this make me sad :(
        while (ttypes.hasNext() && tforms.hasNext()){
            columns.add(new TableColumn(tforms.next(), ttypes.next()));
        }

        sanityChecks(hdu);

        numberOfRowsInTable = hdu.getInt("NAXIS2").orElse(0);
        numberOfColumnsInTable = columns.size();

        reader = new Reader(inputStreamForHDUData);
    }

    /**
     * Perform some sanity checks on the bintable.
     *  1. The TFIELDS keyword must exist.
     *  2. The number of columns stored in the TFIELD line has to fit the number
     *     of TTPYE fields
     *  3. Make sure the NAXIS2 keyword exists. It gives the number of rows.
     *  4. NAXIS2 needs to be non-negative.
     *
     * @param hdu the hdu to check
     */
    private void sanityChecks(HDU hdu) {
        int tfields = hdu.getInt("TFIELDS").orElseThrow(() -> {
            log.error("The TFIELDS keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.2.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing TFIELD keyword");
        });

        if(columns.size() != tfields){
            log.error("The value of TFIELDS: {} does not match the number of TTYPEn,TBCOLn,TFORMn tuples {}" +
                    "\nSee section 7.2.1 of the Fits 3.0 standard", tfields, columns.size());
            throw new IllegalArgumentException("Number of TFIELDS does not match number of TFORMn entries.");
        }


        int naxis2 = hdu.getInt("NAXIS2").orElseThrow(() -> {
            log.error("The TFIELDS keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.2.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing TFIELD keyword");
        });

        if(naxis2 < 0){
            throw new IllegalArgumentException("Number of rows (NAXIS2) is negative.");
        }

    }


    public final class Reader{
        final DataInputStream stream;

        private Reader(DataInputStream stream) {
            this.stream = stream;
        }


        public Map<String, Serializable> getNextRow() throws IOException {
            HashMap<String, Serializable> map = new HashMap<>();
            for(TableColumn c : columns){
                if(c.repeatCount > 1){
                    map.put(c.name, readArrayFromStream(c, stream));
                } else {
                    map.put(c.name, readSingleValueFromStream(c, stream));
                }
            }
            return  map;
        }


        private Serializable readSingleValueFromStream(TableColumn c, DataInputStream stream) throws IOException {
            Serializable b = null;
            switch (c.type){
                case BOOLEAN:
                    b =  stream.readBoolean();
                    break;
                case CHAR:
                    b =  stream.readUTF();
                    break;
                case BYTE:
                    b = stream.readByte();
                    break;
                case SHORT:
                    b = stream.readShort();
                    break;
                case INT:
                    b = stream.readInt();
                    break;
                case LONG:
                    b = stream.readLong();
                    break;
                case FLOAT:
                    b = stream.readFloat();
                    break;
                case DOUBLE:
                    b = stream.readDouble();
                    break;
            }
            return b;
        }

        private Serializable readArrayFromStream(TableColumn c, DataInputStream stream) throws IOException {

            if(c.type == ColumnType.BOOLEAN){
                boolean[] bools = new boolean[c.repeatCount];
                for (int i = 0; i < c.repeatCount; i++) {
                    bools[i] = stream.readBoolean();
                }
                return bools;
            }

            byte[] b = new byte[c.repeatCount*c.type.byteSize];
            stream.readFully(b);

            switch (c.type){
                case CHAR:
                    char[] chars = new char[c.repeatCount];
                    ByteBuffer.wrap(b).asCharBuffer().get(chars);
                    return chars;
                case BYTE:
                    return b;
                case SHORT:
                    short[] shorts = new short[c.repeatCount];
                    ByteBuffer.wrap(b).asShortBuffer().get(shorts);
                    return shorts;
                case INT:
                    int[] ints = new int[c.repeatCount];
                    ByteBuffer.wrap(b).asIntBuffer().get(ints);
                    return ints;
                case LONG:
                    long[] longs = new long[c.repeatCount];
                    ByteBuffer.wrap(b).asLongBuffer().get(longs);
                    return longs;
                case FLOAT:
                    float[] floats = new float[c.repeatCount];
                    ByteBuffer.wrap(b).asFloatBuffer().get(floats);
                    return floats;
                case DOUBLE:
                    double[] doubles = new double[c.repeatCount];
                    ByteBuffer.wrap(b).asDoubleBuffer().get(doubles);
                    return doubles;
            }
            return null;
        }

    }
}
