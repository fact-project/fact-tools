package fact.io.hdureader;

import com.google.common.collect.ForwardingMap;

import java.util.*;

/**
 * Created by mackaiver on 10/11/16.
 */
public class MapMapper<K, V> extends ForwardingMap<K, V>{
    private  final Map<K, V> delegateMap = new HashMap<K, V>();

    public Optional<Short> getShort(K key){
        try {
            return Optional.of((Short) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }
    public Optional<short[]> getShortArray(K key){
        try {
            return Optional.of((short[]) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }

    public Optional<Integer> getInt(K key){
        try {
            return Optional.of((Integer) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }
    public Optional<int[]> getIntArray(K key){
        try {
            return Optional.of((int[]) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }


    public Optional<Double> getDouble(K key){
        try {
            return Optional.of((Double) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }

    public Optional<double[]> getDoubleArray(K key){
        try {
            return Optional.of((double[]) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }


    public Optional<Float> getFloat(K key){
        try {
            return Optional.of((Float) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }

    public Optional<Long> getLong(K key){
        try {
            return Optional.of((Long) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }


    @Override
    protected Map<K, V> delegate() {
        return delegateMap;
    }

}
