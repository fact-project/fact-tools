package fact.auxservice;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by kai on 31.03.15.
 */
public class AuxPoint implements Comparable<AuxPoint>{
    private DateTime timeStamp;
    private ImmutableMap<String, Serializable> data;

    public AuxPoint(DateTime timeStamp){
        this.timeStamp = new DateTime(timeStamp);
    }

    public AuxPoint(DateTime timeStamp, Map<String, Serializable> data){
        this.data = new ImmutableMap.Builder<String, Serializable>().putAll(data).build();
        this.timeStamp = new DateTime(timeStamp);
    }


    public DateTime getTimeStamp() {
        return timeStamp;
    }

    public ImmutableMap<String, Serializable> getData() {
        return data;
    }

    /**
     * Returns the value for the key iff it exists and its a Double. Returns null otherwise.
     * @param key
     * @return
     */
    public Double getDouble(String key){
        try {
            return (Double) data.get(key);
        } catch (ClassCastException e){
            return null;
        }
    }


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