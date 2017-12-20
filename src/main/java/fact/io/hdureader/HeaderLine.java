package fact.io.hdureader;

import com.google.common.base.Splitter;

import java.util.List;

/**
 * One line of a FITS header. Including the comment.
 * <p>
 * Created by mackaiver on 03/11/16.
 */
class HeaderLine {
    public final String value;
    public final String key;
    public final String comment;

    /**
     * Parses one header line for its key and value.
     *
     * @param line on FITS header line
     * @return a HeaderLine object for the given string.
     */
    static HeaderLine fromString(String line) {
        //split the line on the = and the /
        List<String> split = Splitter.onPattern("=|\\/").trimResults().splitToList(line);


        String key = split.get(0);

        //header values are usually denoted by 'somevalue' if they are strings.
        //some files however contain 'somevalue____' where ___ denote spaces.
        //thats why after removing the ' we trim all whitespace.
        String value = "";
        if (split.size() > 1) {
            value = split.get(1);
            value = value.replace("'", "").trim();
        }

        //comments are not mandatory. in that case this string is empty.
        String comment = "";
        if (split.size() > 2) {
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
