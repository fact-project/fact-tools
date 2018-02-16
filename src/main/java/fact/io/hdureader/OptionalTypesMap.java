package fact.io.hdureader;

import com.google.common.collect.ForwardingMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A map which can returns values of the appropriate type or an empty optional.
 * Some of its accessor methods can be useful for situations where one needs values of specific type and does
 * not want to do the whole casting plus error checking business.
 * <p>
 * This simply delegates all other calls to a HashMap<K, V> instance.
 * <p>
 * Created by mackaiver on 10/11/16.
 */
public class OptionalTypesMap<K, V> extends ForwardingMap<K, V> {
    private final Map<K, V> delegateMap = new HashMap<>();

    public Optional<String> getString (K key) {
        try {
            return Optional.ofNullable((String) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the value for the given key if it exists and the value can be cast to Short.
     *
     * @return an optional holding the value for the key
     */
    public Optional<Short> getShort(K key) {
        try {
            return Optional.ofNullable((Short) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the value for the given key if it exists and the value can be cast to short[].
     *
     * @return an optional holding the value for the key
     */
    public Optional<short[]> getShortArray(K key) {
        try {
            return Optional.ofNullable((short[]) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the value for the given key if it exists and the value can be cast to Integer.
     *
     * @return an optional holding the value for the key
     */
    public Optional<Integer> getInt(K key) {
        try {
            return Optional.ofNullable((Integer) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the value for the given key if it exists and the value can be cast to int[].
     *
     * @return an optional holding the value for the key
     */
    public Optional<int[]> getIntArray(K key) {
        try {
            return Optional.ofNullable((int[]) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the value for the given key if it exists and the value can be cast to Double.
     *
     * @return an optional holding the value for the key
     */
    public Optional<Double> getDouble(K key) {
        try {
            return Optional.ofNullable((Double) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the value for the given key if it exists and the value can be cast to double[].
     *
     * @return an optional holding the value for the key
     */
    public Optional<double[]> getDoubleArray(K key) {
        try {
            return Optional.ofNullable((double[]) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }


    /**
     * Get the value for the given key if it exists and the value can be cast to Float.
     *
     * @return an optional holding the value for the key
     */
    public Optional<Float> getFloat(K key) {
        try {
            return Optional.ofNullable((Float) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the value for the given key if it exists and the value can be cast to float[].
     *
     * @return an optional holding the value for the key
     */
    public Optional<float[]> getFloatArray(K key) {
        try {
            return Optional.ofNullable((float[]) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the value for the keyword in the header as long if it exists and the value can be cast to Long.
     *
     * @return an optional holding the value for the key
     */
    public Optional<Long> getLong(K key) {
        try {
            return Optional.ofNullable((Long) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the value for the keyword in the header as long array if it exists and the value can be cast to long[].
     *
     * @return an optional holding the value for the key
     */
    public Optional<long[]> getLongArray(K key) {
        try {
            return Optional.ofNullable((long[]) delegate().get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }


    @Override
    protected Map<K, V> delegate() {
        return delegateMap;
    }

}
