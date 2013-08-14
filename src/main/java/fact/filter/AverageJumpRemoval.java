package fact.filter;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.util.Log;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;

public class AverageJumpRemoval implements Processor{

	LinkedList<short[]> previousStartCells = new LinkedList<short[]>();
	LinkedList<short[]> previousStopCells = new LinkedList<short[]>();

	protected String key = "DataCalibrated";
	protected String outputKey = "DataCalibrated";
	private String color = "#A5D417";

	//	private double jumpThreshold = 8.0;
	private int limit = 2;
	private double threshold = 0.1;
	long prevTime = 0;

	/**
	 * Each event contains the StartCellData array which contains the current startcell for each pixel.
	 * We save the previous 50 events in the previousStartCells previousStartCells. Which is a linked list containing 50 startcelldata arrays
	 */
	@Override
	public Data process(Data input) {

		//save the time differnce betwwen the current and the last event nad put it in hte map
		int[] eventTime = (int[]) input.get("UnixTimeUTC");
		if(eventTime == null){
			Log.error("The key \"UnixTimeUTC \" was not found in the event.");
			return null;
		}

		long  time = ((long)eventTime[0])*1000000  + ( ((long)eventTime[1]) ) ; 
		Long deltaT =  (time - prevTime);
		prevTime = time;
		input.put("deltaT", deltaT.doubleValue());


		//check rois and wether the cutslice operator wrote the right labels into the map
		float[] data = (float[]) input.get(key);
		int roi = data.length/Constants.NUMBEROFPIXEL;
		int length = (Integer) input.get("NROI");
		int start = 0;
		int end = 300;
		if(input.containsKey("@start" + key) && input.containsKey("@end" + key)){
			start = (Integer) input.get("@start" + key);
			end = (Integer) input.get("@end" + key);
		} else if(roi != length) {
			Log.warn("The ROI from the fits file does not fit the ROI in the data. And the CutSlices perator was not properly used.");
		}

		//get the startcell array for the current event and calculate the stop cells from that
		short[] startCellArray = (short[])input.get("StartCellData");
		short[] stopCellArray = new short[startCellArray.length];
		//calculate the stopcellArray for the current event
		for (int i = 0; i < startCellArray.length; ++i){
			//there are 1024 capacitors in the ringbuffer
			stopCellArray[i] = (short) ((startCellArray[i] + length)% 1024);
		}


		float[] result = new float[data.length];
		System.arraycopy(data, 0, result, 0, data.length);

		Color co = Color.decode(color);
		int r = co.getRed();
		int g = co.getGreen();
		int b = co.getBlue();

		IntervalMarker[] m = new IntervalMarker[Constants.NUMBEROFPIXEL];
		IntervalMarker[] me = new IntervalMarker[Constants.NUMBEROFPIXEL];

		//this is the case for the very first event we read
		if( previousStartCells.isEmpty()){
			previousStartCells.addFirst(startCellArray);
			previousStopCells.addFirst(stopCellArray);
			input.put("@" + Constants.KEY_COLOR + "_"+outputKey, color);
			input.put(outputKey, data);
			return input;
		}


		//get average slice values for all pixel
		float[] average = new float[roi];
		float sum = 0;
		for(int slice = 0; slice < roi; ++slice){
			for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; ++pix){
				int pos = pix*roi + slice;
				sum += data[pos];
			}
			sum /= Constants.NUMBEROFPIXEL;
			average[slice] = sum;
		}


		HashSet<Integer> set = new HashSet<Integer>();
		for(int s : startCellArray)
			set.add(s);


		double heightStart = 0.0;
		double heightEnd= 0.0;

		short[] startCells = previousStartCells.get(0);
		short[] stopCells = previousStopCells.get(0);

		int selectedPreviousStartEvent = 0;
		int selectedPreviousStopEvent = 0;
		if(previousStartCells.size() >= limit){


			for(int i = 0; i < limit ; i++){
				startCells = previousStartCells.get(i);
				stopCells = previousStopCells.get(i);

				//calculate jumpheight at the last known stop and start cell in the average array
				int s = getSliceFromCell(startCells[0], length, startCellArray[0], start, end) +3;
				int e = getSliceFromCell(stopCells[0], length, startCellArray[0],start,end) + 9;
				//lets find the maximum jumpheight in the event
				if(s > 0  && s < roi){
					float h = jumpHeight(s, roi, 5, average);
					if(Math.abs(h) > Math.abs(heightStart) ){
						heightStart = h;
						m[0] = new IntervalMarker(s, s + 1 , new Color(r,g,b, 250));
						selectedPreviousStartEvent = i;
					}
					//					break;
				}
				if(e > 0 && e < roi){
					float h = jumpHeight(e, roi, 5, average);
					if(Math.abs(h) > Math.abs(heightEnd) ){
						heightEnd = h;
						me[0] = new IntervalMarker(e, e + 1 , new Color(0,g,b, 250));
						selectedPreviousStopEvent = i;
					}
					//					break;
				}
			}
		}

		if(Math.abs(heightStart) < threshold ) heightStart = Double.NaN;
		if(Math.abs(heightEnd) < threshold) heightEnd = Double.NaN;

		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//recht runterschieben
			startCells = previousStartCells.get(selectedPreviousStartEvent);
			int s = getSliceFromCell(startCells[pix], length, startCellArray[pix], start, end) +3;
			if(s > 0  && s < roi && heightStart  != 0){
				for (int i =  s ; i < roi ; ++i){
					int pos = pix*roi + i;
					result[pos] -= heightStart;
				}
			}
			
			//links runterschieben
			stopCells = previousStopCells.get(selectedPreviousStopEvent);
			int e = getSliceFromCell(stopCells[pix], length, startCellArray[pix], start, end) +9;
			if(e > 0  && e < roi && heightEnd  != 0){
				//jetzt links 
				for (int i =  e ; i >= 0 ; --i){
					int pos = pix*roi + i;
					result[pos] += heightEnd;
				}
			}

		}		
		
		
		previousStartCells.addFirst(startCellArray);
		previousStopCells.addFirst(stopCellArray);

		if(previousStartCells.size() > limit){
			previousStartCells.removeLast();
		}
		if(previousStopCells.size() > limit){
			previousStopCells.removeLast();
		}	

		
		input.put(outputKey + "_startJumpHeight", heightStart);
		input.put(outputKey + "_stopJumpHeight", heightEnd);

		//add color value if set
		input.put(outputKey+"Marker", m);
		if(color !=  null && !color.equals("")){
			input.put("@" + Constants.KEY_COLOR + "_"+outputKey+"Marker", color);
		}	
		input.put(outputKey+"MarkerStop", me);
		if(color !=  null && !color.equals("")){
			input.put("@" + Constants.KEY_COLOR + "_"+outputKey+"MarkerStop", color);
		}		


		input.put("@" + Constants.KEY_COLOR + "_"+outputKey, color);
		input.put(outputKey, result);
		return input;
	}

	private int getSliceFromCell(int cell, int nRoi, int currentStartCell, int start, int end){
		int s = (cell+nRoi) % 1024 - (currentStartCell + nRoi)%1024;
		s =  s - start;
		return s;
	}


	/**
	 * returns the jumpheight in the data array at position c per series!  
	 * 
	 * @param c the position in the data array at which to calculate the jumpheight. 
	 * @param data the array containgin the series
	 * @return the jumpheight
	 */
	private float jumpHeight(int slice, int roi ,int  offset,  float[] data) {
		double baseLeft = 0;
		int l = 0;
		for (int i = Math.max(slice - offset - 3, 0) ; i < slice - offset && i < roi ; ++i){
			l++;
			baseLeft += data[i];
		}
		baseLeft  = baseLeft / l;
		double baseRight = 0;
		l = 0;
		for (int i = Math.min(slice + offset, roi) ; i < Math.min(roi,slice + offset + 3 ); ++i){
			l++;
			baseRight += data[i];
		}
		baseRight  = baseRight / l;
		return (float) (baseRight- baseLeft);
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



	public double getThreshold() {
		return threshold;
	}
	@Parameter(required = false, description = "The threshold after which a jump should be removed. This is the difference in the baseline before and after the jump position")
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getLimit() {
		return limit;
	}
	@Parameter(required = false, description = "How many previous start and stopcells should be considered? AAHHHHH!")
	public void setLimit(int limit) {
		this.limit = limit;
	}

}
