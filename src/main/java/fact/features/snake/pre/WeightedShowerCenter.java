package fact.features.snake.pre;

import fact.Utils;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
//import fact.hexmap.ui.overlays.MarkerOverlay;

/**
 *	WeightedShowerCenter
 *	Bestimmt den gewichteten Mittelpunkt des Schauers aus einer liste an markierten Pixeln
 *	Sehr aehnlich zu CoG
 *	
 *  @author Dominik Baack &lt;dominik.baack@udo.edu&gt;
 *
 */
public class WeightedShowerCenter implements Processor
{
	@Parameter(required = true, description = "Input: Chid of Showerpixel ")
	private String shower = null;	
	@Parameter(required = true, description = "Input: Photoncharge ")
	private String weight = null;
	@Parameter(required = true, description = "Output: X pos ")
	private String outkeyX = null;
	@Parameter(required = true, description = "Output: Y pos ")
	private String outkeyY = null;
	
	@Parameter(required = false, description = "Input: Draw Center")
	private boolean draw = false;
	
	@Override
	public Data process(Data input) 
	{
		Utils.isKeyValid(input, shower, int[].class);
		Utils.isKeyValid(input, weight, double[].class);
		
		int[] show = (int[]) input.get(shower);
		double[] wei = (double[]) input.get(weight);		
		
		double ergX = 0;
		double ergX2 = 0;
		double ergY = 0;
		double ergY2 = 0;
		double w = 0;
		double w2 = 0;
		
		FactPixelMapping PixelMapping_  = FactPixelMapping.getInstance();			
		
		for(int i=0; i<show.length; i++)		// +500 Verschieben der Gewichte in den ausschliesslich positiven Bereich
		{			
			ergX += PixelMapping_.getPixelFromId(show[i]).getXPositionInMM() * (Math.abs(wei[show[i]]));
			ergY += PixelMapping_.getPixelFromId(show[i]).getYPositionInMM() * (Math.abs(wei[show[i]]));			
			w += Math.abs(wei[show[i]]); 	
			
			ergX2 += PixelMapping_.getPixelFromId(show[i]).getXPositionInMM() * Math.pow(wei[show[i]],2);
			ergY2 += PixelMapping_.getPixelFromId(show[i]).getYPositionInMM() * Math.pow(wei[show[i]],2);			
			w2 += Math.pow(wei[show[i]],2);
		}		
		
		ergX = ergX / w;
		ergY = ergY / w;	
		
		ergX2 = ergX2 / w2;
		ergY2 = ergY2 / w2;	
		
		//FactCameraPixel pix = PixelMapping_.getPixelBelowCoordinatesInMM(ergX, ergY);
		//FactCameraPixel[] neighbor = PixelMapping_.getNeighboursForPixelWithDirection(pix);
		
		
		/*Set<CameraPixel> set = new HashSet<CameraPixel>();
		set.add(pix);		
		
		int c=5;
		set.add(PixelMapping_.getNeighboursForPixelWithDirection(neighbor[c])[c]);
		set.add(PixelMapping_.getNeighboursForPixelWithDirection(PixelMapping_.getNeighboursForPixelWithDirection(neighbor[c])[c])[c]);
		set.add(PixelMapping_.getNeighboursForPixelWithDirection(PixelMapping_.getNeighboursForPixelWithDirection(PixelMapping_.getNeighboursForPixelWithDirection(neighbor[c])[c])[c])[c]);
		
		input.put("dingens", new PixelSetOverlay(set));*/
		
		input.put(outkeyX, ergX);
		input.put(outkeyX+"2", ergX2);
		input.put(outkeyY, ergY);
		input.put(outkeyY+"2", ergY2);
		
		if(draw)
		{
			//input.put("center", new MarkerOverlay(ergX, ergY));
			//input.put("center2", new MarkerOverlay(ergX2, ergY2));
		}
		
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


	public boolean isDraw()
	{
		return draw;
	}


	public void setDraw(boolean draw)
	{
		this.draw = draw;
	}


		
	
}
