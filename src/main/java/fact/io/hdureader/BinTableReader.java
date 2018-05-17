package fact.io.hdureader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A reader for a FITS binary table.
 * <p>
 * This class implements the iterable and iterator interfaces which allow for such nice things as
 * <p>
 * for(OptionalTypesMap p : binTableReader){
 * assertTrue(p.containsKey("Data"));
 * }
 * <p>
 * <p>
 * The next() and the getNextRow() methods deliver a map with the data from one row in the table.
 * <p>
 * Created by mackaiver on 18/11/16.
 */
public class BinTableReader implements Reader {

    /**
     * Check whether there is another row to return from this heap
     *
     * @return true if another row can be read.
     */
    @Override
    public boolean hasNext() {
        return numberOfRowsRead < numberOfRowsInTable;
    }


    private final DataInputStream stream;
    private final List<BinTable.TableColumn> columns;
    private int numberOfRowsRead = 0;
    private final int numberOfRowsInTable;
    private final int numberOfBytesPerRow;


    private BinTableReader(BinTable binTable) {
        this.stream = binTable.tableDataStream;
        this.columns = binTable.columns;
        this.numberOfBytesPerRow = binTable.numberOfBytesPerRow;
        this.numberOfRowsInTable = binTable.numberOfRowsInTable;
    }

    public static BinTableReader forBinTable(BinTable binTable) {
        return new BinTableReader(binTable);
    }


    public OptionalTypesMap<String, Serializable> getNextRow() throws IOException {
        OptionalTypesMap<String, Serializable> map = new OptionalTypesMap<>();
        for (BinTable.TableColumn c : columns) {
            if (c.repeatCount > 1) {
                map.put(c.name, readArrayFromStream(c, stream));
            } else if (c.repeatCount == 1) {
                map.put(c.name, readSingleValueFromStream(c, stream));
            } else if (c.repeatCount == 0) {
                map.put(c.name, null);
            }
        }
        numberOfRowsRead++;
        return map;
    }


    private Serializable readSingleValueFromStream(BinTable.TableColumn c, DataInputStream stream) throws IOException {

        Serializable b = null;
        switch (c.type) {
            case BOOLEAN:
                b = stream.readBoolean();
                break;
            case CHAR:
                byte[] s = new byte[]{stream.readByte()};
                b = new String(s, StandardCharsets.US_ASCII);
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

        if (c.type == BinTable.ColumnType.BOOLEAN) {
            boolean[] bools = new boolean[c.repeatCount];
            for (int i = 0; i < c.repeatCount; i++) {
                bools[i] = stream.readBoolean();
            }
            return bools;
        }

        byte[] b = new byte[c.repeatCount * c.type.byteSize];
        stream.readFully(b);

        switch (c.type) {
            case CHAR:
                return new String(b, StandardCharsets.US_ASCII).replace("\u0000", "");
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

    /**
     * Skips the given number of rows.
     *
     * @param num The amount of rows to skip.
     * @throws IOException
     */
    @Override
    public void skipRows(int amount) throws IOException {
        if (amount+numberOfRowsRead <= numberOfRowsInTable) {
            new IndexOutOfBoundsException("Table has not enough rows to access row num: " + (amount+numberOfRowsRead));
        }
        stream.skipBytes(amount * this.numberOfBytesPerRow);
        numberOfRowsRead += amount;
    }
}
