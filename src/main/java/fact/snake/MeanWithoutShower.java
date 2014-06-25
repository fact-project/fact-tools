package fact.snake;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class MeanWithoutShower implements Processor
{

	private String key = null;
	private String shower = null;
	private String outkey = null;
	
	
	@Override
	public Data process(Data input) 
	{
		if(outkey == null) throw new RuntimeException("Key \"outkey\" not set");
		
		EventUtils.mapContainsKeys(getClass(), input, key, shower);
		

		double[] arr = ((double[]) input.get(key)).clone();		
	
		int[] sho = (int[]) input.get(shower);
		
		for(int i=0; i<sho.length; i++)
		{
			arr[sho[i]] = 0;
		}
		
		double erg = 0;
		for(int i=0; i<arr.length; i++)
		{
			erg += arr[i];
		}
		
		erg = erg / (arr.length - sho.length);
		
		input.put(outkey, erg);
		
		return input;
	}


	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	public String getShower() {
		return shower;
	}


	public void setShower(String shower) {
		this.shower = shower;
	}


	public String getOutkey() {
		return outkey;
	}


	public void setOutkey(String outkey) {
		this.outkey = outkey;
	}

	
	
}
