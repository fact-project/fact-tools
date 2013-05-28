package fact.processors;

import stream.Data;
import stream.Processor;

public  class Short2Float implements Processor {
	@Override
	public Data process(Data item) {
		short[] dat = (short[]) item.get("Data");
		if (dat != null) {
			float[] values = new float[dat.length];
			for (int i = 0; i < dat.length; i++) {
				values[i] = dat[i];
			}
			item.put("Data", values);
		}
		return item;
	}
}
