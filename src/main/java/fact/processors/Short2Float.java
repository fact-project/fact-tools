package fact.processors;

import stream.Data;
import stream.Processor;

	public  class Short2Float implements Processor {
		@Override
		public Data process(Data data) {
			short[] dat = (short[]) data.get("Data");
			if (dat != null) {
				float[] values = new float[dat.length];
				for (int i = 0; i < dat.length; i++) {
					values[i] = dat[i];
				}
				data.put("Data", values);
			}
			return data;
		}
	}
