package fact.io.hdureader;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A common interface for Readers for Fits and ZFitsTables.
 * Readers (at least for bintables and zfits heaps) should implement methods for getting single rows from the file.
 *
 * The Readers should be iterable so we have default methods for having nice for loops and things.
 *
 *
 * Created by mackaiver on 14/12/16.
 */
interface Reader extends
        Iterator<OptionalTypesMap<String, Serializable>>,
        Iterable<OptionalTypesMap<String, Serializable>> {

    /**
     * Get the data from the next row. The columns in the row can be accessed by their name in the resulting
     * map that is returned by this method. The resulting map comes with convenience methods for accessing data of
     * predefined types.
     *
     * This method overwrites the next() method of the iterable interface and is similar to the
     * @see Reader#getNextRow() method but throws a runtime exception when an IO error occurs.
     *
     * @return a map containing the the data from the rows columns.
     * @throws NoSuchElementException iff hasNext() is false
     */
    @Override
    default OptionalTypesMap<String, Serializable> next() throws NoSuchElementException {
        try {
            return getNextRow();
        }
        catch (IOException e) {
            throw new RuntimeException("IO Error occured. " + e.getMessage());
        }
    }

    /**
     * Get the iterator for this Reader. This is useful for iterator style for loops i.e. for(Map m : reader){...}
     *
     * @return the iterator for this reader.
     */
    @Override
    default Iterator<OptionalTypesMap<String, Serializable>> iterator() {
        return this;
    }



    /**
     * Check whether there is another row to return from this heap
     *
     * Readers implementing this interface have to overwrite this method.
     * @return true iff another row can be read.
     */
    @Override
    boolean hasNext();


    /**
     * Get the data from the next row. The columns in the row can be accessed by their name in the resulting
     * map that is returned by this method. The resulting map comes with convenience methods for accessing data of
     * predefined types.
     *
     * Readers implementing this interface have to overwrite this method.
     *
     * @return  a map containing the the data from the rows columns.
     * @throws IOException if some IO error occurs
     */
    OptionalTypesMap<String,Serializable> getNextRow() throws IOException;




}