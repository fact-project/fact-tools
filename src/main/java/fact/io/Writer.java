package fact.io;


import fact.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;
import stream.util.Variables;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generalized Class for the writers with some common methods
 */
public abstract class Writer {
    private Set<String> previousKeySet;
    private static Logger log = LoggerFactory.getLogger(Writer.class);

    public static boolean isSimulated(Data item) {
        String creator = (String) item.get("CREATOR");
        if (creator == null) {
            creator = (String) item.get("creator");
        }
        if (creator == null) {
            throw new RuntimeException("Data item did not contain key 'CREATOR' or 'creator', cannot guess if simulations or observations");
        }
        return creator.toLowerCase().equals("ceres");
    }

    public static Keys getDefaultKeys(boolean simulations) {
        Properties prop = new Properties();
        InputStream stream = Writer.class.getResourceAsStream("/default/settings.properties");
        try {
            prop.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HashMap<String, String> propertyMap = new HashMap<>();
        prop.forEach((k, v) -> propertyMap.put((String) k, (String) v));
        Variables variables = new Variables(propertyMap);

        variables.expand("outputKeysSimulations");

        String keys;
        if (simulations) {
            keys = variables.expand(variables.get("outputKeysSimulations"));
            log.info("Using default keys for simulations");
        } else {
            keys = variables.expand(variables.get("outputKeysObservations"));
            log.info("Using default keys for observations");
        }
        log.debug("{}", keys);
        return new Keys(keys);
    }

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
                Set<String> diff1 = new HashSet<String>();
                diff1.addAll(item.keySet());
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
