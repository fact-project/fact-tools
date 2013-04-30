package fact.processors;

import java.util.ArrayList;
import java.util.LinkedList;

import fact.Constants;

import stream.Data;
import stream.Processor;

public class RemoveJumps implements Processor{

	LinkedList<short[]> previousStartCells = new LinkedList<short[]>();
	LinkedList<short[]> previousStopCells = new LinkedList<short[]>();
	
	protected String key = "DataCalibrated";
	protected String outputKey = "DataCalibrated";

	private double jumpThreshold = 8.0;
	int window = 3;
	
	@Override
	public Data process(Data input) {

		

		short[] startCellArray = (short[])input.get("StartCellData");
		short[] stopCellArray = new short[startCellArray.length];

		float[] data = (float[]) input.get(key);
		float[] result = new float[data.length];
		System.arraycopy(data, 0, result, 0, data.length);
		int roi = data.length/Constants.NUMBEROFPIXEL;
		
		//this is the case for the very first event we read
		if(previousStartCells.isEmpty()){
			previousStartCells.addFirst(startCellArray);
			previousStopCells.addFirst(stopCellArray);
			input.put(outputKey, data);
			return input;
		}

		
		for (int i = 0; i < startCellArray.length; ++i){
			//there are 1024 capacitors in the ringbuffer
			stopCellArray[i] = (short) ((startCellArray[i] + roi)% 1024);
		}
		
		
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			
			ArrayList<Short> startCandidates = new ArrayList<Short>();
			ArrayList<Short> stopCandidates = new ArrayList<Short>();
			
			short currentStartCell = startCellArray[pix];
			short currentStopCell = stopCellArray[pix];
			
			for(short[] c: previousStartCells){
				if (c[pix] >= currentStartCell && c[pix] <= currentStopCell){
					startCandidates.add(c[pix]);
				}
			}
			
			for(short[] c: previousStopCells){
				if (c[pix] >= startCellArray[pix] && c[pix] <= currentStopCell) {
					stopCandidates.add(c[pix]);
				}
			}
			
			for(short c : startCandidates){
				double h = jumpHeight(c , data);
				if(Math.abs(h) > jumpThreshold){
					int pos = pix * roi;
					//rechts an links Anpassen
					// c is any number between currentStart and currentStopCell. 0< c < 1024 
					for (int i =  (c-currentStartCell + pos) ; i < roi + pos ; ++i){
						result[i] += h;
					}
				}
			}
			
			
			for(short c : stopCandidates){
				double h = jumpHeight(c , data);
				if(Math.abs(h) > jumpThreshold){
					int pos = pix * roi;
					//links an rechts Anpassen
					// c is any number between currentStart and currentStopCell. 0< c < 1024 
					for (int i = (c-currentStartCell + pos) ; i > 0 + pos ; --i){
						result[i] += h;
					}
				}
			}
			


			
		}		
		previousStartCells.addFirst(startCellArray);
		previousStopCells.addFirst(stopCellArray);
		
		if(previousStartCells.size() > 50){
			previousStartCells.removeLast();
		}
		if(previousStopCells.size() > 50){
			previousStopCells.removeLast();
		}	
		
		input.put(outputKey, result);
		return input;
	}

	private double jumpHeight(short c, float[] data) {
		int roi = data.length/Constants.NUMBEROFPIXEL;
		double baseLeft = 0;
		for (int i = Math.max(0, c-window) ; i < Math.min(c, roi) ; ++i){
			baseLeft += data[i];
		}
		baseLeft  /= window;
		
		double baseRight = 0;
		for (int i = c ; i < Math.min(c+window, roi) ; ++i){
			baseRight += data[i];
		}
		baseRight  /= window;
		return baseRight - baseLeft;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

}
