package fact.features.snake.video;

import static org.junit.Assert.*;

import org.junit.Test;

import cern.colt.Arrays;
import stream.Data;
import stream.data.DataFactory;


public class VideoArrayTest
{
	@Test
	public void testSnakeArray()
	{
		Double[] data = new Double[200];
		Integer[] index = new Integer[200];
				
		for(int x=0; x<data.length; x++)
		{
			if(x < 15) data[x] = 0.0;
			else if(x < 22) data[x] = data[x-1] + 43.0;
			else if(data[x-1] > 0) data[x] = data[x-1] - 12.0;
			else data[x] = 0.0;
			
			index[x] = x;
		}
		
		System.out.println(Arrays.toString(data));
		
		Data item = DataFactory.create();
		
		item.put("array", data);
		item.put("index", index);
		
		VideoArray testObj = new VideoArray();
		testObj.setInkeyArray("array");
		testObj.setInkeyIndex("index");
		testObj.setOutkeyMax("Max");
		testObj.setOutkeySlope1("slope1");
		testObj.setOutkeySlope2("slope2");
		
		testObj.process(item);
		
		System.out.println(item.get("slope1"));
		System.out.println(item.get("slope1_"));
		System.out.println(item.get("slope2"));
		System.out.println(item.get("slope2_"));
		System.out.println("Max: " + item.get("Max"));
	}
}
