package fact.io.hdureader;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class represents a header in the HDU of fits file. The values in the header can be accessed via
 * convenience 'get' methods for the main data types. HISTORY and COMMENT strings are also saved.
 * <p>
 * Created by mackaiver on 18/11/16.
 */
public class Header {

    final Map<String, HeaderLine> headerMap;

    final int headerSizeInBytes;

    public final String comment;
    public final String history;

    Header(List<String> headerLines, int headerSizeInBytes) {
        this.headerSizeInBytes = headerSizeInBytes;
        //iterate over each header string and parse all interesting information.
        headerMap = headerLines.stream()
                .filter(a -> !a.matches("COMMENT\\s.+|HISTORY\\s.+|END$"))
                .map(HeaderLine::fromString)
                .collect(Collectors.toMap(
                        hduline -> hduline.key,
                        Function.identity()
                        )
                );

        comment = headerLines.stream()
                .filter(a -> a.matches("COMMENT.+"))
                .map(a -> a.substring("COMMENT ".length()))
                .collect(Collectors.joining(" \n "));

        history = headerLines.stream()
                .filter(a -> a.matches("HISTORY.+"))
                .map(a -> a.substring("HISTORY ".length()))
                .collect(Collectors.joining(" \n "));

    }

    /**
     * This converts this Header into a map of key value pairs of type <String, Serializable>.
     *
     * @return a HashMap containing the key value pairs.
     */
    public Map<String, Serializable> asMapOfSerializables() {
        Map<String, Serializable> m = new HashMap<>();
        date().ifPresent(d -> m.put("DATE", d));

        headerMap.forEach((k, v) -> {
            getDouble(k).ifPresent(d -> m.put(k, d));
            getLong(k).ifPresent(d -> m.put(k, d));
            getInt(k).ifPresent(d -> m.put(k, d));

            if (!m.containsKey(k)) {
                m.put(k, v.value);
            }
        });
        return m;
    }

    /**
     * According to the FITS standard a header can contain a DATE keyword. This method returns a LocalDateTime
     * if a date can be found in the header.
     *
     * @return date stored in the HDU header
     */
    public Optional<ZonedDateTime> date() {
        if (!headerMap.containsKey("DATE")) {
            return Optional.empty();
        }

        String dateString = headerMap.get("DATE").value;
        ZonedDateTime dateTime = LocalDateTime.parse(dateString).atZone(ZoneOffset.UTC);
        return Optional.of(dateTime);
    }

    /**
     * Get the value for the keyword in the header as integer if it exists and the value is parseable as int.
     *
     * @return integer value for the key
     */
    public Optional<Integer> getInt(String key) {
        try {
            HeaderLine line = headerMap.get(key);
            return Optional.of(Integer.parseInt(line.value));
        } catch (NumberFormatException | NullPointerException e) {
            return Optional.empty();
        }
    }


    /**
     * Get the value for the keyword in the header as long if it exists and the value is parseable as long.
     *
     * @return long value for the key
     */
    public Optional<Long> getLong(String key) {
        try {
            HeaderLine line = headerMap.get(key);
            return Optional.of(Long.parseLong(line.value));
        } catch (NumberFormatException | NullPointerException e) {
            return Optional.empty();
        }
    }


    /**
     * Get the value for the keyword in the header as float if it exists and the value is parseable as float.
     *
     * @return float value for the key
     */
    public Optional<Float> getFloat(String key) {
        try {
            HeaderLine line = headerMap.get(key);
            return Optional.of(Float.parseFloat(line.value));
        } catch (NumberFormatException | NullPointerException e) {
            return Optional.empty();
        }
    }


    /**
     * Get the value for the keyword in the header as float if it exists and the value is parseable as float.
     *
     * @return float value for the key
     */
    public Optional<Boolean> getBoolean(String key) {
        try {
            HeaderLine line = headerMap.get(key);
            Boolean b = line.value.equals("T");
            return Optional.of(b);
        } catch (NumberFormatException | NullPointerException e) {
            return Optional.empty();
        }
    }


    /**
     * Get the value for the keyword in the header as Double if it exists and the value is parseable as double.
     *
     * @return Double value for the key
     */
    public Optional<Double> getDouble(String key) {
        try {
            HeaderLine line = headerMap.get(key);
            return Optional.of(Double.parseDouble(line.value));
        } catch (NumberFormatException | NullPointerException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the value for the keyword in the header as string if it exists.
     *
     * @return Double value for the key
     */
    public Optional<String> get(String key) {
        try {
            HeaderLine line = headerMap.get(key);
            return Optional.ofNullable(line.value);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

}
