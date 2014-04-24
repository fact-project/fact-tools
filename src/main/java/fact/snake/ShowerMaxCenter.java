package fact.snake;

import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;
import stream.Data;
import stream.Processor;

public class ShowerMaxCenter implements Processor
{
	String shower = null;
	String weight = null;
	String outX = null;
	String outY = null;
	
	
	@Override
	public Data process(Data input) 
	{
		if(outX == null){
			throw new RuntimeException("Missing parameter: outX");
		}
		if(outY == null){
			throw new RuntimeException("Missing parameter: outY");
		}
		
		
		EventUtils.mapContainsKeys(getClass(), input, shower, weight);
		
		
		int[] show = (int[]) input.get(shower);
		double[] wei = (double[]) input.get(weight);		
		
		double ergX = 0;
		double ergY = 0;
		double w = 0;
		
		for(int i=0; i<show.length; i++)
		{
			ergX += DefaultPixelMapping.getPosX(show[i]) * 0.5 * wei[show[i]];
			ergY += DefaultPixelMapping.getPosY(show[i]) * 0.5 * wei[show[i]];
			
			w += wei[show[i]] * 0.5; 
		}
		
		ergX = ergX / w;
		ergY = ergY / w;
		
		input.put(outX, ergX);
		input.put(outY, ergY);
		
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


	public String getOutX() {
		return outX;
	}


	public void setOutX(String outX) {
		this.outX = outX;
	}


	public String getOutY() {
		return outY;
	}


	public void setOutY(String outY) {
		this.outY = outY;
	}

	
	
}
