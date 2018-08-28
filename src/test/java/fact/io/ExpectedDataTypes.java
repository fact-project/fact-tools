/**
 *
 */
package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author chris
 */
class ExpectedDataTypes {

    private static Logger log = LoggerFactory.getLogger(ExpectedDataTypes.class);
    private Map<String, Serializable> types = new LinkedHashMap<String, Serializable>();


    void addType(String key, Serializable typeValue) {
        types.put(key, typeValue);
    }

    boolean check(Data item) {
        log.debug("Checking {} types for item {}", types.keySet().size(), item);
        for (String key : types.keySet()) {
            if (!item.containsKey(key)) {
                log.error("Missing key '{}' in item {}!", key, item);
                return false;
            }

            if (!checkValues(key, types.get(key), item.get(key))) {
                log.error("Value type mismatch for key '" + key
                                + "', expected type: {} but found type: {}",
                        types.get(key).getClass(), item.get(key).getClass());
                return false;
            }
        }

        return true;
    }

    private boolean checkValues(String key, Serializable exp,
                                Serializable found) {

        if (!valuesMatch(exp, found)) {
            log.error("Value mismatch for key '{}'", key);
            return false;
        }

        return true;
    }

    private boolean valuesMatch(Serializable val1, Serializable val2) {

        if (val1 == val2)
            return true;

        if (val1 == null || val2 == null)
            return false;

        if (val1.getClass().isArray() && val2.getClass().isArray()) {
            return arraysMatch(val1, val2);
        }

        if (!val1.getClass().equals(val2.getClass())) {
            log.error("Value types differ: {} and {}", val1.getClass(),
                    val2.getClass());
            return false;
        }

        return val1.equals(val2);
    }

    private boolean arraysMatch(Object array1, Object array2) {

        Class<?> ct1 = array1.getClass().getComponentType();
        Class<?> ct2 = array2.getClass().getComponentType();

        if (!array1.getClass().getComponentType()
                .equals(array2.getClass().getComponentType())) {
            log.error("Component type mismatch for arrays: {}[] and {}[]", ct1,
                    ct2);
            return false;
        }

        if (Array.getLength(array1) != Array.getLength(array2)) {
            log.error("Array length mismatch!");
            return false;
        }

        log.debug("Arrays equal.");
        return true;
    }
}
