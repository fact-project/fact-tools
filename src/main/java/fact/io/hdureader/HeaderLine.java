package fact.io.hdureader;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Optional;

/**
 * One line of a FITS header. Including the comment.
 *
 * Created by mackaiver on 03/11/16.
 */
class HeaderLine {
        public final String value;
        public final String key;
        public final String comment;

    /**
     * Parses one header line for its key and value.
     * @param line on FITS header line
     * @return a HeaderLine object for the given string.
     */
    static HeaderLine fromString(String line){
        List<String> split = Splitter.onPattern("=|\\/").trimResults().splitToList(line);

        String key = split.get(0);

        String value = "";
        if(split.size() > 1){
            value = split.get(1);
            value = value.replace("'", "");
        }

        String comment = "";
        if(split.size() > 2){
            comment = split.get(2);
        }

        return new HeaderLine(key, value, comment);

    }

    private HeaderLine(String key, String value, String comment) {
        this.value = value;
        this.key = key;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Key: " + key + ", Value: " + value + ", Comment: " + comment;
    }
}