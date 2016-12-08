package fact.io.hdureader;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by mackaiver on 18/11/16.
 */
public class Header {

    final Map<String, HeaderLine> headerMap;
    public  final String comment;
    public final String history;
    public final boolean isPrimaryHeader;

    Header(List<String> headerLines){
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


        isPrimaryHeader = headerLines.stream()
                .anyMatch(a -> a.matches("SIMPLE\\s+=\\s+T.*"));

    }

    /**
     * According to the FITS standard a header can contain a DATE keyword. This method returns a LocalDateTime
     * if a date can be found in the header.
     *
     * @return date stored in the HDU header
     */
    public Optional<LocalDateTime> date(){
        if (!headerMap.containsKey("DATE")){
            return Optional.empty();
        }

        String dateString = headerMap.get("DATE").value;
        LocalDateTime.parse(dateString);
        return Optional.of(LocalDateTime.parse(dateString));
    }

    public Optional<Integer> getInt(String key){
        try {
            HeaderLine line = headerMap.get(key);
            return Optional.of(Integer.parseInt(line.value));
        } catch (NumberFormatException| NullPointerException e){
            return Optional.empty();
        }
    }

    public Optional<Float> getFloat(String key){
        try {
            HeaderLine line = headerMap.get(key);
            return Optional.of(Float.parseFloat(line.value));
        } catch (NumberFormatException| NullPointerException e){
            return Optional.empty();
        }
    }

    public Optional<Double> getDouble(String key){
        try {
            HeaderLine line = headerMap.get(key);
            return Optional.of(Double.parseDouble(line.value));
        } catch (NumberFormatException| NullPointerException e){
            return Optional.empty();
        }
    }

    public Optional<String> get(String key){
        try {
            HeaderLine line = headerMap.get(key);
            return Optional.ofNullable(line.value);
        } catch (NullPointerException e){
            return Optional.empty();
        }
    }

}
