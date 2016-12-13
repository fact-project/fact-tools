package fact.io.hdureader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A reader for a FITS binary table.
 *
 * This class implements the iterable and iterator interfaces which allow for such nice things as
 *
 *        for(OptionalTypesMap p : binTableReader){
 *              assertTrue(p.containsKey("Data"));
 *         }
 *
 *
 * The next() and the getNextRow() methods deliver a map with the data from one row in the table.
 *
 * Created by mackaiver on 18/11/16.
 */
public class BinTableReader implements
        Iterator<OptionalTypesMap<String, Serializable>>,
        Iterable<OptionalTypesMap<String, Serializable>> {

        /**
         * Check whether there is another row to return from this heap
         * @return true iff another row can be read.
         */
        @Override
        public boolean hasNext() {
            return numberOfRowsRead < numberOfRowsInTable;
        }

        /**
         * Get the data from the next row. The columns in the row can be accessed by their name in the resulting
         * map that is returned by this method. The resulting map comes with convenience methods for accessing data of
         * predefined types.
         *
         * This method overwrites the next() method of the iterable interface and is similar to the
         * @see ZFitsHeapReader#getNextRow() method but throws a runtime exception when an IO error occurs.
         *
         * @return a map containing the the data from the rows columns.
         * @throws NoSuchElementException iff hasNext() is false
         */
        @Override
        public OptionalTypesMap<String, Serializable> next() throws NoSuchElementException {
            try {
                return getNextRow();
            }
            catch (IOException e) {
                throw new RuntimeException("IO Error occured. " + e.getMessage());
            }
        }

        /**
         * Get the iterator for this reader. This is useful for iterator style for loops i.e. for(map m : reader){...}
         *
         * @return the iterator for this reader.
         */
        @Override
        public Iterator<OptionalTypesMap<String, Serializable>> iterator() {
            return this;
        }


        private final DataInputStream stream;
        private final List<BinTable.TableColumn> columns;
        private int numberOfRowsRead = 0;
        private final int numberOfRowsInTable;



        private BinTableReader(BinTable binTable) {
            this.stream = binTable.tableDataStream;
            this.columns = binTable.columns;
            this.numberOfRowsInTable = binTable.numberOfRowsInTable;
        }

        public static BinTableReader forBinTable(BinTable binTable){
            return new BinTableReader(binTable);
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
            numberOfRowsRead++;
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
