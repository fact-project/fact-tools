package fact.features.snake.video;

import java.util.Arrays;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.data.DataFactory;


public class PixelFrameDataSplitter implements Processor
{
	
	@Parameter(required = true, description = "Input: Slice Data stream")
	private String inkeyImageData = null;
	@Parameter(required = true, description = "Input: List of Elements to copy in the new stream")
	private String[] inkeysCopy = null;
	
	@Parameter(required = true, description = "Name of Pixel Data")
	private String outkeyImageData = null;
	@Parameter(required = true, description = "Name of Slice ")
	private String outkeyIndex = null;
	@Parameter(required = true, description = "Output: List of DataItems")
	private String outkeyLoop = null;	
	
	@Parameter(required = false, description = "Start: ShowerStart - Front")
	private int setFront = 25;
	@Parameter(required = false, description = "Start: ShowerStart + Back")
	private int setBack = 60;
	@Parameter(required = false, description = "Start")
	private String setStart = null;
	
	@Override
	public Data process(Data input) 
	{		
		Utils.isKeyValid(input, inkeyImageData, double[].class);
		Utils.isKeyValid(input, setStart, Double.class);
		
		double[] data = (double[]) input.get(inkeyImageData);
		int frames = data.length / Constants.NUMBEROFPIXEL;		
		
		double start = (Double) input.get(setStart);			
		
		int front = (int) (start - setFront);
		int back = (int) (start + setBack);
		if(front < 0) front = 0;
		if(back > frames) back = frames;
		
		Data[] item = new Data[back-front];
				
		for(int i=0; i<(back-front); i++)
		{
			int slice = front + i;
			
			item[i] = DataFactory.create();
			
			for(String itr : inkeysCopy)
			{
				if(input.containsKey(itr))
					item[i].put(itr, input.get(itr));
			}
			
			item[i].put(outkeyIndex, slice);	
			
			double[] sliceData = new double[Constants.NUMBEROFPIXEL];			
			for(int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++)
			{
				sliceData[pix] = data[pix*300 + slice];				
			}
			item[i].put(outkeyImageData, sliceData);
		}
				
		
		input.put(outkeyLoop, item);
		
		
		return input;
	}

	public String getInkeyImageData() {
		return inkeyImageData;
	}

	public void setInkeyImageData(String inkeyImageData) {
		this.inkeyImageData = inkeyImageData;
	}

	public String getOutkeyImageData() {
		return outkeyImageData;
	}

	public void setOutkeyImageData(String outkeyImageData) {
		this.outkeyImageData = outkeyImageData;
	}

	public String getOutkeyIndex() {
		return outkeyIndex;
	}

	public void setOutkeyIndex(String outkeyIndex) {
		this.outkeyIndex = outkeyIndex;
	}

	public String getOutkeyLoop() {
		return outkeyLoop;
	}

	public void setOutkeyLoop(String outkeyLoop) {
		this.outkeyLoop = outkeyLoop;
	}

	public int getSetFront() {
		return setFront;
	}

	public void setSetFront(int setFront) {
		this.setFront = setFront;
	}

	public int getSetBack() {
		return setBack;
	}

	public void setSetBack(int setBack) {
		this.setBack = setBack;
	}

	

	public String getSetStart() {
		return setStart;
	}

	public void setSetStart(String setStart) {
		this.setStart = setStart;
	}

	public String[] getInkeysCopy() {
		return inkeysCopy;
	}

	public void setInkeysCopy(String[] inkeysCopy) {
		this.inkeysCopy = inkeysCopy;
	}

	

}
