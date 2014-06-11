package fact.snake;

import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;
import stream.Data;
import stream.Processor;

public class WeightedShowerCenter implements Processor
{
	private String shower = null;
	private String weight = null;
	private String outkeyX = null;
	private String outkeyY = null;
	
	
	@Override
	public Data process(Data input) 
	{
		if(outkeyX == null) throw new RuntimeException("Key \"outkeyX\" not set");
		if(outkeyY == null) throw new RuntimeException("Key \"outkeyY\" not set");		
		
		EventUtils.mapContainsKeys(getClass(), input, shower, weight);
		
		
		int[] show = (int[]) input.get(shower);
		double[] wei = (double[]) input.get(weight);		
		
		double ergX = 0;
		double ergY = 0;
		double w = 0;
		
		for(int i=0; i<show.length; i++)
		{
			ergX += DefaultPixelMapping.getPosXinMM(show[i]) * 0.5 * wei[show[i]];
			ergY += DefaultPixelMapping.getPosYinMM(show[i]) * 0.5 * wei[show[i]];
			
			w += wei[show[i]] * 0.5; 
		}
		
		ergX = ergX / w;
		ergY = ergY / w;
		
		input.put(outkeyX, ergX);
		input.put(outkeyY, ergY);
		
		return input;
	}


	public String getShower() {
		return shower;
	}


	public void setShower(String shower) {
		this.shower = shower;
	}


	public String getWeight() {
		return weight;
	}


	public void setWeight(String weight) {
		this.weight = weight;
	}


	public String getOutkeyX() {
		return outkeyX;
	}


	public void setOutkeyX(String outkeyX) {
		this.outkeyX = outkeyX;
	}


	public String getOutkeyY() {
		return outkeyY;
	}


	public void setOutkeyY(String outkeyY) {
		this.outkeyY = outkeyY;
	}


	

	
	
}
