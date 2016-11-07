package fact.io.hdureader;

import fact.io.FitsStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A BinTable representation.
 * Created by kai on 04.11.16.
 */
public class BinTable {

    static Logger log = LoggerFactory.getLogger(BinTable.class);


    /**
     * This enum maps the type characters in the header to the fits types.
     * Not all types from the fits standard are supported here.
     *
     * TODO: support variable length arrays.
     *
     */
    public enum ColumnType {
        BOOLEAN("L"),
        STRING("A"),
        BYTE("B"),
        SHORT("I"),
        INT("J"),
        LONG("K"),
        FLOAT("E"),
        DOUBLE("D"),
        NONE("");

        String typeString;
        ColumnType(String typeString) {
            this.typeString = typeString;
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
            this.name = ttype.key;

        }
        public int repeatCount = 0;
        public ColumnType type;
        public String name;
    }

    private List<TableColumn> columns = new ArrayList<>();

    public Integer numberOfRowsInTable = 0;

    public BinTable(HDU hdu, DataInputStream inputStream) throws IllegalArgumentException{

        Iterator<HDULine> tforms = hdu.header
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches("TFORM\\d+"))
                .map(Map.Entry::getValue)
                .iterator();


        Iterator<HDULine> ttypes = hdu.header
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches("TTYPE\\d+"))
                .map(Map.Entry::getValue)
                .iterator();



        //no zipping in java yet. this make me sad :(
        while (ttypes.hasNext() && tforms.hasNext()){
            columns.add(new TableColumn(tforms.next(), ttypes.next()));
        }

        numberOfRowsInTable = hdu.getInt("NAXIS2").orElse(0);

        int tfields = hdu.getInt("TFIELDS").orElseThrow(() -> {
            log.error("The TFIELDS keyword cannot be found in the BinTable. " +
                    "Its mandatory. See section 7.2.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing TFIELD keyword");
        });

        if(columns.size() != tfields){
            log.error("The value of TFIELDS: {} does not match the number of TTYPEn,TBCOLn,TFORMn tuples {}" +
                    "\n See section 7.2.1 of the Fits 3.0 standard", tfields, columns.size());
            throw new IllegalArgumentException("Number of TFIELDS does not match number of TFORMn entries.");
        }

    }

    public final class Reader{
        final DataInputStream stream;

        public Reader(DataInputStream stream) {
            this.stream = stream;
        }

//        public Map getNextRow() throws IOException {
//            HashMap<String, Serializable> map = new HashMap<>();
//            for(TableColumn c : l){
//                switch (c.e){
//                    case SHORT:
//
//                        for (int r = 0; r < c.repeatCount; r++) {
//                            short s = stream.readShort();
//                        }
//
//
//                        break;
//                }
//            }
//        }
    }
}
