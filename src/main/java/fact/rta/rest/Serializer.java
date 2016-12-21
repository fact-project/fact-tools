package fact.rta.rest;

import com.google.common.collect.Range;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by mackaiver on 20/12/16.
 */
public class Serializer {

    public static class DateTimeAdapter extends TypeAdapter<OffsetDateTime> {

        @Override
        public void write(JsonWriter jsonWriter, OffsetDateTime dateTime) throws IOException {
            if (dateTime == null){
                jsonWriter.nullValue();
            }
            else{
                String format = dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                jsonWriter.value(format);
            }
        }

        @Override
        public OffsetDateTime read(JsonReader jsonReader) throws IOException {
            return OffsetDateTime.parse(jsonReader.toString());
        }
    }

    public static class RangeSerializer implements JsonSerializer<Range<OffsetDateTime>> {
        public JsonElement serialize(Range<OffsetDateTime> range, Type typeOfSrc, JsonSerializationContext context) {


            JsonObject obj = new JsonObject();
            obj.addProperty("start", range.lowerEndpoint().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            obj.addProperty("end", range.upperEndpoint().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            return obj;
        }
    }
}
