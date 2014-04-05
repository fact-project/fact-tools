package fact.features.snake;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class MedianWithoutShower implements Processor
{

	String inputArray = null;
	String shower = null;
	String output = null;
	
	
	@Override
	public Data process(Data input) 
	{
		EventUtils.mapContainsKeys(getClass(), input, inputArray, shower);

		double[] arr = ((double[]) input.get(inputArray)).clone();		
	
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
		
		input.put(output, erg);
		
		return input;
	}

	
	public String getInputArray() {
		return inputArray;
	}


	public void setInputArray(String inputArray) {
		this.inputArray = inputArray;
	}


	public String getShower() {
		return shower;
	}


	public void setShower(String shower) {
		this.shower = shower;
	}


	public String getOutput() {
		return output;
	}


	public void setOutput(String output) {
		this.output = output;
	}
	
}
