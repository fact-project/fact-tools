package fact.io.hdureader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
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
    public final TableReader tableReader;
    public final ZFitsReader zFitsReader;
    public final String name;


    BinTable(Map<String, HDULine> header,
             String tableName,
             DataInputStream inputStreamForHDUData,
             DataInputStream heapDataStream
    ) throws IllegalArgumentException{

        this.name = tableName;

        Iterator<HDULine> tforms = header
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches("TFORM\\d+"))
                .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey().substring(5))))
                .map(Map.Entry::getValue)
                .iterator();


        Iterator<HDULine> ttypes = header
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

        numberOfRowsInTable = header.get("NAXIS2").getInt().orElse(0);
        numberOfColumnsInTable = columns.size();

        tableReader = new TableReader(inputStreamForHDUData);
        zFitsReader = new ZFitsReader(heapDataStream);
    }



    public final class ZFitsReader {
        final DataInputStream stream;

        private ZFitsReader(DataInputStream stream) {
            this.stream = stream;
        }


        private void readTileHeader(byte[] bytes){
            String s = new String(bytes,0, 4, StandardCharsets.US_ASCII);
            ByteBuffer buffer = ByteBuffer.wrap(bytes, 4, 16-4).order(ByteOrder.LITTLE_ENDIAN);
            int numberOfRows= buffer.getInt();
            long size= buffer.getLong();
            return;
        }


        private void readBlockHeader(byte[] bytes) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            long size = buffer.getLong();
            String ordering = new String(new byte[]{buffer.get()}, StandardCharsets.US_ASCII);
            byte numProcs = buffer.get();
            return;
        }

        public MapMapper<String, Serializable> getNextRow() throws IOException {
            MapMapper<String, Serializable> map = new MapMapper<>();
            //read first tile header
            //see figure 6 of zfits paper
            byte[] tileHeader = new byte[16];
            stream.readFully(tileHeader);

            readTileHeader(tileHeader);

            byte[] blockHeader = new byte[16];
            stream.readFully(blockHeader);

            readBlockHeader(blockHeader);
//            stream.skipBytes((int) size);
            return  map;
        }


    }


    public final class TableReader{
        final DataInputStream stream;

        private TableReader(DataInputStream stream) {
            this.stream = stream;
        }


        public MapMapper<String, Serializable> getNextRow() throws IOException {
            MapMapper<String, Serializable> map = new MapMapper<>();
            for(TableColumn c : columns){
                if(c.repeatCount > 1){
                    map.put(c.name, readArrayFromStream(c, stream));
                } else if(c.repeatCount == 1) {
                    map.put(c.name, readSingleValueFromStream(c, stream));
                } else if(c.repeatCount == 0){
                    map.put(c.name, null);
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
