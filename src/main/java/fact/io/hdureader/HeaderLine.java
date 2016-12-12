package fact.io.hdureader;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Optional;

/**
 * Created by mackaiver on 03/11/16.
 */
public class HeaderLine {
        public final String value;
        public final String key;
        public final String comment;

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

        public Optional<Integer> getInt(){
            try {
                return Optional.of(Integer.parseInt(value));
            } catch (NumberFormatException e){
                return Optional.empty();
            }
        }

        public Optional<Long> getLong(){
            try {
                return Optional.of(Long.parseLong(value));
            } catch (NumberFormatException e){
                return Optional.empty();
            }
        }

        public Optional<Double> getDouble(){
            try {
                return Optional.of(Double.parseDouble(value));
            } catch (NumberFormatException e){
                return Optional.empty();
            }
        }

    @Override
    public String toString() {
        return "Key: " + key + ", Value: " + value + ", Comment: " + comment;
    }
}
