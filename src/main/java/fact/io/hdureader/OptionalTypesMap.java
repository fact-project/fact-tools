package fact.io.hdureader;

import com.google.common.collect.ForwardingMap;

import java.util.*;

/**
 * A map which can returns values of the appropriate type or an empty optional.
 * Created by mackaiver on 10/11/16.
 */
public class OptionalTypesMap<K, V> extends ForwardingMap<K, V>{
    private  final Map<K, V> delegateMap = new HashMap<K, V>();

    public Optional<Short> getShort(K key){
        try {
            return Optional.ofNullable((Short) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }
    public Optional<short[]> getShortArray(K key){
        try {
            return Optional.ofNullable((short[]) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }

    public Optional<Integer> getInt(K key){
        try {
            return Optional.ofNullable((Integer) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }
    public Optional<int[]> getIntArray(K key){
        try {
            return Optional.ofNullable((int[]) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }


    public Optional<Double> getDouble(K key){
        try {
            return Optional.ofNullable((Double) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }

    public Optional<double[]> getDoubleArray(K key){
        try {
            return Optional.ofNullable((double[]) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }


    public Optional<Float> getFloat(K key){
        try {
            return Optional.ofNullable((Float) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }

    public Optional<float[]> getFloatArray(K key){
        try {
            return Optional.ofNullable((float[]) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }

    public Optional<Long> getLong(K key){
        try {
            return Optional.ofNullable((Long) delegate().get(key));
        } catch (ClassCastException e){
            return Optional.empty();
        }
    }


    @Override
    protected Map<K, V> delegate() {
        return delegateMap;
    }

}
