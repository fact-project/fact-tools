package fact.io;


import fact.Utils;
import org.apache.commons.lang3.StringUtils;
import stream.Data;
import stream.Keys;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generalized Class for the writers with some common methods
 */
public abstract class Writer {
    private Set<String> previousKeySet;

    public void testKeys(Data item, Keys keys, boolean allowNullKeys) {
        if (!allowNullKeys) {
            List<String> keysList = Arrays.stream(keys.getKeyValues())
                    .filter(p -> !p.contains("*"))
                    .filter(p -> !p.startsWith("!"))
                    .collect(Collectors.toList());
            Utils.mapContainsKeys(item, keysList);
        }

        if (previousKeySet==null) {
            previousKeySet = item.keySet();
        } else {
            if (!previousKeySet.equals(item.keySet())) {
                Set<String> diff1 = item.keySet();
                diff1.removeAll(previousKeySet);
                Set<String> diff2 = previousKeySet;
                diff2.removeAll(item.keySet());
                diff1.addAll(diff2);
                String missingKeys = StringUtils.join(diff1, ",");
                throw new RuntimeException("The Keyset changed, missing keys: '"+missingKeys+"'");
            }
        }
    }
}
