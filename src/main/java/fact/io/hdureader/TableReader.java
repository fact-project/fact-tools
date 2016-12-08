package fact.io.hdureader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by mackaiver on 18/11/16.
 */
public class TableReader {
        private final DataInputStream stream;
        private final List<BinTable.TableColumn> columns;


        private TableReader(BinTable binTable) {
                this.stream = binTable.tableDataStream;
                this.columns = binTable.columns;
        }

        public static TableReader forBinTable(BinTable binTable){
            return new TableReader(binTable);
        }


        public OptionalTypesMap<String, Serializable> getNextRow() throws IOException {
            OptionalTypesMap<String, Serializable> map = new OptionalTypesMap<>();
            for(BinTable.TableColumn c : columns){
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


        private Serializable readSingleValueFromStream(BinTable.TableColumn c, DataInputStream stream) throws IOException {

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

        private Serializable readArrayFromStream(BinTable.TableColumn c, DataInputStream stream) throws IOException {

            if(c.type == BinTable.ColumnType.BOOLEAN){
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
