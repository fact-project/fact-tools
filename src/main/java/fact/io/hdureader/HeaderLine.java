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
        String key = line.substring(0, 8).trim();
        // values are not mandatory, if no value is present (absence of '= ' in bytes 9 & 10), everything
        // shall be treated as comment
        String value = "";
        // comments are not mandatory. in that case this string is empty.
        String comment = "";

        if (line.substring(8, 10).equals("= ")) {
            //split the line on the = and the /
            String[] split = line.substring(10, line.length()).split("/", 2);

            // header values are usually denoted by 'somevalue' if they are strings.
            // some files however contain 'somevalue____' where ___ denote spaces.
            // that's why after removing the ' we trim all whitespace.
            value = split[0].trim();
            value = value.replace("'", "").trim();

            if (split.length > 1) {
                comment = split[1].trim();
            }

        } else {
            comment = line.substring(8, line.length());
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
