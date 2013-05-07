package fact.processors;

import java.util.ArrayList;
import java.util.LinkedList;

import fact.Constants;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class RemoveJumps implements Processor{

	LinkedList<short[]> previousStartCells = new LinkedList<short[]>();
	LinkedList<short[]> previousStopCells = new LinkedList<short[]>();
	
	protected String key = "DataCalibrated";
	protected String outputKey = "DataCalibrated";
	private String color = "#A5D417";

	private double jumpThreshold = 8.0;
	int window = 3;
	
	/**
	 * Each event contains the StartCellData array which contains the current starcell for each pixel.
	 * We save the previous 50 events in the previousStartCells previousStartCells. Which is a linked list containing 50 startcelldata arrays
	 */
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
			input.put("@" + Constants.KEY_COLOR + "_"+outputKey, color);
			input.put(outputKey, data);
			return input;
		}

		//calculate the stopcellArray for the current event
		//thius wont be necessary
		for (int i = 0; i < startCellArray.length; ++i){
			//there are 1024 capacitors in the ringbuffer
			stopCellArray[i] = (short) ((startCellArray[i] + roi)% 1024);
		}
		
		
		
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			
			ArrayList<Integer> startCandidateSlices = new ArrayList<Integer>();
			ArrayList<Integer> stopCandidateSlices = new ArrayList<Integer>();
			
			
			
			//create the list of possible start and stopcandidates
			//these are all previuous start-orstopcells that can be found between the current start- and stopcell
			
			for(short[] c: previousStartCells){
				//for some reason there seems to be an offset of + 3 slices between the stopcell and the real jump
				int slice = getSliceFromCell(c[pix] + 3, roi, startCellArray[pix]) ;
				if(slice > 0 && slice < roi) {
					startCandidateSlices.add(slice);
				}
			}
			
			for(short[] c: previousStopCells){
				//for some reason there seems to be an offset of +9 slices between the stopcell and the real jump
				int slice = getSliceFromCell(c[pix] + 9, roi, startCellArray[pix]) ;
				if(slice > 0 && slice < roi) {
					stopCandidateSlices.add(slice);
				}
			}
			
			
			for(int candidateSlice : startCandidateSlices){
				if(candidateSlice > roi || candidateSlice <0){
					System.out.println("WTF");
				}
				double h = jumpHeight(pix, candidateSlice, data);
				if(Math.abs(h) > jumpThreshold){
					//rechts an links Anpassen
					// c is any number between currentStart and currentStopCell. 0< c < 1024 
					for (int i =  candidateSlice ; i < roi ; ++i){
						int pos = pix*roi + i;
						result[pos] -= h;
					}
				}
			}
			
			
			for(int candidateSlice : stopCandidateSlices){
				double h = jumpHeight(pix, candidateSlice, data);
				if(Math.abs(h) > jumpThreshold){
					//links an rechts Anpassen
					// c is any number between currentStart and currentStopCell. 0< c < 1024
					for (int i = candidateSlice; i >= 0 ; --i){
						int pos = pix*roi + i ;
						result[pos] += h;
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
		input.put("@" + Constants.KEY_COLOR + "_"+outputKey, color);
		input.put(outputKey, result);
		return input;
	}
	
	private int getSliceFromCell(int c, int roi, int currentStartCell){
		return (c+roi) % 1024 - (currentStartCell + roi)%1024;
	}
	

	/**
	 * returns the jumpheight in the data array at position c; No conversion between slicenumber and position in the array takes place here. the caller has to handle that
	 * 
	 * @param c the position in the data array at which to calculate the jumpheight. 
	 * @param data the array containgin the series
	 * @return the jumpheight
	 */
	private double jumpHeight(int pix, int slice, float[] data) {
		int roi = data.length/Constants.NUMBEROFPIXEL;
		
		double baseLeft = 0;
		try{
		for (int i = Math.max(0, slice-window) ; i < slice ; ++i){
			int pos = pix*roi + i;
			baseLeft += data[pos];
		}
		} catch (ArrayIndexOutOfBoundsException e){
			System.out.println(" BaseLeft <---- Array out of bounds: slice: " + slice + " pix: " + pix + " roi: " + roi );
		}
		baseLeft  /= window;
		
		double baseRight = 0;
		try{
		for (int i = slice ; i < Math.min(slice+window, roi) ; ++i){
			int pos = pix*roi + i;
			baseRight += data[pos];
		}
		
	} catch (ArrayIndexOutOfBoundsException e){
		System.out.println("Baseright : --- >Array out of bounds: slice: " + slice + " pix: " + pix + " roi: " + roi );
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
	
	
	//brownish
	public String getColor() {
		return color;
	}
	@Parameter(required = false, description = "RGB/Hex description String for the color that will be drawn in the FactViewer ChartPanel")
	public void setColor(String color) {
		this.color = color;
	}

}
