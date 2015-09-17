package fact.io.gsonTypeAdapter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class DoubleAdapter extends TypeAdapter<Double> {
	
	private int signDigits;
	
	public Double read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL)
		{
			reader.nextNull();
			return null;
		}
		double x = reader.nextDouble();
		return x;
	}

	public void write(JsonWriter writer, Double value) throws IOException {
		BigDecimal bValue = new BigDecimal(value);
		bValue = bValue.round(new MathContext(signDigits));
		writer.value(bValue.doubleValue());
	}
	
	public void setSignDigits(int sD){
		signDigits = sD;
	}
	
}