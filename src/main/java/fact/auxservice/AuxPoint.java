package fact.auxservice;

import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * This is a class to create immutable container holding data from some source providing auxiliary data.
 * This can be a file or a database. The key passed to the getters has to be known beforehand of course.
 * Created by kai on 31.03.15.
 */
public class AuxPoint implements Comparable<AuxPoint>{
    private OffsetDateTime timeStamp;
    private ImmutableMap<String, Serializable> data;

    public AuxPoint(OffsetDateTime timeStamp){
        this.timeStamp =OffsetDateTime.of(timeStamp.toLocalDate(),timeStamp.toLocalTime(), ZoneOffset.of("+00:00"));
    }


    /**
     * Creates an AuxPoint from a Timestamp and a map containing key,value pairs as found in the slow control data.
     * @param timeStamp the timestamp specifying  when the auxiliary data was recorded.
     * @param data a map containing the names and values from the .fits files as key values pairs.
     */
    public AuxPoint(OffsetDateTime timeStamp, Map<String, Serializable> data){
        this.data = new ImmutableMap.Builder<String, Serializable>().putAll(data).build();
        this.timeStamp =OffsetDateTime.of(timeStamp.toLocalDate(),timeStamp.toLocalTime(), ZoneOffset.of("+00:00"));
    }


    /**
     * The timestamp from when sensor recorded this AuxPoint.
     * @return the timestamp for this point.
     */
    public OffsetDateTime getTimeStamp() {
        return timeStamp;
    }


    public ImmutableMap<String, Serializable> getData() {
        return data;
    }

    /**
     * Returns the value for the key iff it exists and its a Double. Returns null otherwise.
     * @param key
     * @return the value or null
     */
    public Double getDouble(String key){
        try {
            return (Double) data.get(key);
        } catch (ClassCastException e){
            return null;
        }
    }

    /**
     * Returns the value for the key iff it exists and its an Integer. Returns null otherwise.
     * @param key
     * @return the value or null
     */
    public Integer getInteger(String key){
        try {
            return (Integer) data.get(key);
        } catch (ClassCastException e){
            return null;
        }
    }

    public String getString(String key){
        try {
            return (String) data.get(key);
        } catch (ClassCastException e){
            return null;
        }
    }

    /**
     * Returns the value for the key iff it exists and its an int[]. Returns null otherwise.
     * @param key
     * @return the value or null
     */
    public int[] getIntegerArray(String key){
        try {
            return (int[]) data.get(key);
        } catch (ClassCastException e){
            return null;
        }
    }

    /**
     * Returns the value for the key iff it exists and its an int[]. Returns null otherwise.
     * @param key
     * @return the value or null
     */
    public double[] getDoubleArray(String key){
        try {
            return (double[]) data.get(key);
        } catch (ClassCastException e){
            return null;
        }
    }


    //below you'll find the auto generated code needed to make this object hashable and comparable

    @Override
    public int compareTo(AuxPoint o) {
        return this.timeStamp.compareTo(o.getTimeStamp());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuxPoint auxPoint = (AuxPoint) o;

        if (data != null ? !data.equals(auxPoint.data) : auxPoint.data != null) return false;
        if (!timeStamp.equals(auxPoint.timeStamp)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timeStamp.hashCode();
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString(){
        return "TimeStamp: " + timeStamp.toString() + data.toString();
    }
}