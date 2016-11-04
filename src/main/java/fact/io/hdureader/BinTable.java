package fact.io.hdureader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by kai on 04.11.16.
 */
public class BinTable {

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
//        COMPLEX('C', 8),
//        BIT(),
//        DOUBLE_COMPLEX('M', 16)


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

    public class TableColumn{

        public TableColumn(HDULine tform, HDULine ttype){
            Matcher matcher = Pattern.compile("(\\d+)([LABIJKED])").matcher(tform.value);
            if (matcher.matches()){
                repeatCount = Integer.parseInt(matcher.group(1));
                e = ColumnType.typeForChar(matcher.group(2));
            }
            this.name = ttype.key;

        }
        public int repeatCount = 0;
        public ColumnType e;
        public String name;
    }

    List<TableColumn> l = new ArrayList<>();

    public Integer numberOfRowsInTable = 0;

    public BinTable(HDU hdu, DataInputStream inputStream){

        Iterator<HDULine> tforms = hdu.header
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches("TFORM\\d?"))
                .map(Map.Entry::getValue)
                .iterator();


        Iterator<HDULine> ttypes = hdu.header
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches("TTYPE\\d?"))
                .map(Map.Entry::getValue)
                .iterator();

        //no zipping in java yet. this make me sad :(
        while (ttypes.hasNext() && tforms.hasNext()){
            l.add(new TableColumn(tforms.next(), ttypes.next()));
        }

        numberOfRowsInTable = hdu.getInt("NAXIS2").orElse(0);


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
