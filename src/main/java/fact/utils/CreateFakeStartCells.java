package fact.utils;

import fact.Constants;
import stream.Data;
import stream.Processor;

import java.util.Random;

public class CreateFakeStartCells implements Processor {

	String outputKey = null;
	
	long seed = 0;
	
	Random random = new Random(seed);
	
	@Override
	public Data process(Data input) {
		
		short[] fakeStartCells = new short[Constants.NUMBEROFPIXEL];
		
		for (int px=0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			fakeStartCells[px] = (short) random.nextInt(1023);
		}
		
		input.put(outputKey, fakeStartCells);
		
		// TODO Auto-generated method stub
		return input;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

}
