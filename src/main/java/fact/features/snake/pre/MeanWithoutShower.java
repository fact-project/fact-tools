package fact.features.snake.pre;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 *	MeanWithoutShower
 *	Bestimmt die mittlere Intensitaet eines Bildes ohne Beruecksichtigung der als Schauerpixel markierten Pixel
 *	
 *  @author Dominik Baack &lt;dominik.baack@udo.edu&gt;
 *
 */
public class MeanWithoutShower implements Processor
{
	static Logger log = LoggerFactory.getLogger(MeanWithoutShower.class);

	@Parameter(required = true, description = "Input: Photoncharge ")
	private String key = null;
	
	@Parameter(required = true, description = "Input: Showerpixel")
	private String shower = null;
	
	@Parameter(required = true, description = "Output: Mean from all Pixel without showerpixel")
	private String outkey = null;
	
	
	@Override
	public Data process(Data input) 
	{
		Utils.isKeyValid(input, key, double[].class);
		Utils.isKeyValid(input, shower, int[].class);		
	
		double[] data = (double[]) input.get(key);		
		int[] sho = (int[]) input.get(shower);
		
		double[] arr = new double[1440];		
		
		for(int i=0; i<sho.length; i++)
		{
			arr[sho[i]] = data[sho[i]];
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
