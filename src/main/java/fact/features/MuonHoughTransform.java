package fact.features;

import stream.Data;
import java.util.ArrayList;
import stream.Processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import fact.Constants;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;

/**
 * This processor delivers several features that can be used to seperate muon rings from
 * other data using the Hough Transform for circles.
 * 
 * @author MaxNoe
 * 
 */


public class MuonHoughTransform implements Processor {
	
	// OutputKeys
	
	// Peakness is a measure for how sharp the best circle is in parameter space
	private String peaknessKey; 
	//Distance is the Euklidian Distance between the three best circles in parameter space
	private String distanceKey;
	// Pixelset for the FactViewer, only returned when showRingKey=true
	private String bestCircleKey;
	// X-Value of the center of the best circle
	private String bestXKey;
	// Y-Value of the center of the best circle
	private String bestYKey;
	// Radius of the best Circle
	private String bestRadiusKey;
	// Number of octants of the best circle in which are HitPixels
	private String octantsHitKey;
	// Number of HitPixels on best Ring/ Total number of HitPixels
	private String percentageKey;
	// Pixel Chids of the best Ring
	private String bestRingPixelKey;
	
	//InputKeys
	private String ringKey; 
	private String photonChargeKey;
	//If showRingkey == true, the PixelSets for the three best circles are returned for the Viewer
	private boolean showRingKey;
	

	// Defining the parameterspace in which we look for circles:
	
	private double min_radius = 70; //minimal radius in mm
	private double max_radius = 130; //maximal radius in mm
	private double min_x = -300; //minimal center X in mm
	private double max_x = 300; //maximal center X in mm
	private double min_y = -300; //minimal center y in mm
	private double max_y = 300; //maximal center y in mm

	// resolution
	private int res_r = 18;
	private int res_x = 60;
	private int res_y = 60;
	
	
	final Logger log = LoggerFactory.getLogger(MuonHoughTransform.class);	
	
	@Override
	public Data process(Data input) {
		
				
		int[] ring = (int[]) input.get(ringKey);
		double[] photonCharge = (double[]) input.get(photonChargeKey);
				
		double[] xPositions = new double[ring.length];
		double[] yPositions = new double[ring.length];
		
		// Get X and Y Positions of the Pixel that survived Cleaning
		
		for(int i=0; i<ring.length; i++){
			xPositions[i] = fact.viewer.ui.DefaultPixelMapping.getPosXinMM(ring[i]);
			yPositions[i] = fact.viewer.ui.DefaultPixelMapping.getPosYinMM(ring[i]);
		}
		
		// generate Hough-Voting-Matrix n:
		
		int[][][] HoughMatrix = new int[res_r+1][res_x+1][res_y+1];
		
		//Fill the parameter space
		
		double[] circle_radius = new double[res_r+1];
		double[] circle_x = new double[res_x+1];
		double[] circle_y = new double[res_y+1];
		
		for (int i=0; i<=res_r; i++){
			circle_radius[i] = (max_radius - min_radius) * i/res_r + min_radius;
		}
		for (int i=0; i<=res_x; i++){
			circle_x[i] = (max_x - min_x) * i/res_x + min_x;
		}
		for (int i=0; i<=res_y; i++){
			circle_y[i] = (max_y - min_y) * i/res_y + min_y;
		}
		
		// HoughTransform:
		
		double peakness = 0;
		int NoneZeroElems = 0;
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		//Position in parameter space of the three best circles
		int[] highest_pos = {0, 0, 0};
		int[] second_highest_pos = {0, 0, 0};
		int[] third_highest_pos = {0, 0, 0};
		double distance;
				
		for (int r = 0; r < circle_radius.length; r++){
			for (int x = 0; x < circle_x.length; x++){
				for (int y = 0; y < circle_y.length; y++){
					for(int pix = 0; pix < ring.length; pix++){
						distance = Math.sqrt(Math.pow((xPositions[pix] - circle_x[x]), 2.0) + Math.pow((yPositions[pix] - circle_y[y]), 2.0));
						if(Math.abs(distance - circle_radius[r]) < 1.1 * fact.Constants.PIXEL_SIZE ){
							HoughMatrix[r][x][y] += photonCharge[ring[pix]];
						}
					}
					stats.addValue(HoughMatrix[r][x][y]);
					if (HoughMatrix[r][x][y] !=0){
						NoneZeroElems += 1;
					}
					if (HoughMatrix[r][x][y] > HoughMatrix[highest_pos[0]][highest_pos[1]][highest_pos[2]]){
						third_highest_pos[0] = second_highest_pos[0];
						third_highest_pos[1] = second_highest_pos[1];
						third_highest_pos[2] = second_highest_pos[2];
						second_highest_pos[0] = highest_pos[0];
						second_highest_pos[1] = highest_pos[1];
						second_highest_pos[2] = highest_pos[2];
						highest_pos[0] = r;
						highest_pos[1] = x;
						highest_pos[2] = y;
					}
					
				}
			}
		}
		

		// Calculate the Features 

		// Hough-Distance and Peakness
		
		double radius_1 = circle_radius[highest_pos[0]];
		double center_x_1 = circle_x[highest_pos[1]];
		double center_y_1 = circle_y[highest_pos[2]];
		
		input.put(bestRadiusKey, radius_1);
		input.put(bestXKey, center_x_1);
		input.put(bestYKey, center_y_1);
				
		double radius_2 = circle_radius[second_highest_pos[0]];
		double center_x_2 = circle_x[second_highest_pos[1]];
		double center_y_2 = circle_y[second_highest_pos[2]];
		
		double radius_3 = circle_radius[third_highest_pos[0]];
		double center_x_3 = circle_x[third_highest_pos[1]];
		double center_y_3 = circle_y[third_highest_pos[2]];
		
		double ParamDistance1 = Math.sqrt(Math.pow(radius_1 - radius_2, 2) + Math.pow(center_x_1 - center_x_2, 2) + Math.pow(center_y_1 - center_y_2 , 2));
		double ParamDistance2 = Math.sqrt(Math.pow(radius_1 - radius_3, 2) + Math.pow(center_x_1 - center_x_3, 2) + Math.pow(center_y_1 - center_y_3 , 2));
		double ParamDistance3 = Math.sqrt(Math.pow(radius_3 - radius_2, 2) + Math.pow(center_x_3 - center_x_2, 2) + Math.pow(center_y_3 - center_y_2 , 2));
		
		
		double ParamDistanceSum = ParamDistance1 + ParamDistance2 + ParamDistance3; 
		double HoughMaximum = stats.getMax();
		double HoughSum = stats.getSum();
		
		peakness = HoughMaximum/(HoughSum/NoneZeroElems);
		input.put(peaknessKey, peakness);
		input.put(distanceKey, ParamDistanceSum);

		
		// Pixels belonging to the best ring:
		
		ArrayList<Integer> bestRingPixelList = new ArrayList<Integer>();
		
		double PixelPosX;
		double PixelPosY;		
		
		for(int pix=0; pix<fact.Constants.NUMBEROFPIXEL; pix++){
			PixelPosX = fact.viewer.ui.DefaultPixelMapping.getPosXinMM(pix);
			PixelPosY = fact.viewer.ui.DefaultPixelMapping.getPosYinMM(pix);
			distance = Math.sqrt(Math.pow((PixelPosX - center_x_1), 2.0) + Math.pow((PixelPosY - center_y_1), 2.0));
			if(Math.abs(distance - radius_1) < 1.1 * fact.Constants.PIXEL_SIZE){
				bestRingPixelList.add(pix);		
			}
		}
		
		int[] bestRingPixel = new int[bestRingPixelList.size()];
		
		for(int i=0; i < bestRingPixelList.size(); i++){
			bestRingPixel[i] = bestRingPixelList.get(i);
		}
		
		input.put(bestRingPixelKey, bestRingPixel);
		
		
		// percentage and octantshit

		double percentage;
		double onRingPixel=0;
		double phi=0;
		int octantsHit=0;
		boolean[] octants = {false,false,false,false,false,false,false,false};
		
		for (int pix=0; pix<ring.length;pix++){
			distance = Math.sqrt(Math.pow((xPositions[pix] - center_x_1), 2.0) + Math.pow((yPositions[pix] - center_y_1), 2.0));
			if(Math.abs(distance - radius_1) < 1.1 * fact.Constants.PIXEL_SIZE){
				onRingPixel+=1;
				
				phi = Math.atan2(xPositions[pix] - center_x_1, yPositions[pix] - center_y_1);
				for(int i=0; i<8; i++){
					if(phi>i*Math.PI/4 - Math.PI && phi<=(i+1)*Math.PI/4 - Math.PI ){
						octants[i]=true;
					}
				}
			}
		}		
				
		
		for(int i=0; i<8; i++){
			if(octants[i]){
				octantsHit+=1;
			}
		}
		
		input.put(octantsHitKey, octantsHit);
		
		percentage = onRingPixel/ring.length;
		
		input.put(percentageKey, percentage);
		

		// Creating the Pixelsets for the Viewer
		
		if (showRingKey){
			double distance1;
			double distance2;
			double distance3;
			PixelSet bestCirclePixelSet = new PixelSet();
			PixelSet secondBestCirclePixelSet = new PixelSet();
			PixelSet thirdBestCirclePixelSet = new PixelSet();
			
			for (int pix=0; pix<Constants.NUMBEROFPIXEL; pix++){
				PixelPosX = fact.viewer.ui.DefaultPixelMapping.getPosXinMM(pix);
				PixelPosY = fact.viewer.ui.DefaultPixelMapping.getPosYinMM(pix);
				distance1 = Math.sqrt(Math.pow((PixelPosX - center_x_1), 2.0) + Math.pow((PixelPosY - center_y_1), 2.0));
				distance2 = Math.sqrt(Math.pow((PixelPosX - center_x_2), 2.0) + Math.pow((PixelPosY - center_y_2), 2.0));
				distance3 = Math.sqrt(Math.pow((PixelPosX - center_x_3), 2.0) + Math.pow((PixelPosY - center_y_3), 2.0));
				if(Math.abs(distance1 - radius_1) < 1.1 * fact.Constants.PIXEL_SIZE ){
					bestCirclePixelSet.add(new Pixel(pix));
				}
				if(Math.abs(distance2 - radius_2) < 1.1 * fact.Constants.PIXEL_SIZE ){
					secondBestCirclePixelSet.add(new Pixel(pix));
				}
				if(Math.abs(distance3 - radius_3) < 1.1 * fact.Constants.PIXEL_SIZE ){
					thirdBestCirclePixelSet.add(new Pixel(pix));
				}
			}
			input.put(bestCircleKey, bestCirclePixelSet);
			input.put("second"+bestCircleKey, secondBestCirclePixelSet);
			input.put("third"+bestCircleKey, thirdBestCirclePixelSet);
		}
		
		
		return input;
	
	}
	
	
	public String getDistanceKey() {
		return distanceKey;
	}
	public void setDistanceKey(String distanceKey) {
		this.distanceKey = distanceKey;
	}
	public String getPeaknessKey() {
		return peaknessKey;
	}
	public void setPeaknessKey(String peaknessKey) {
		this.peaknessKey = peaknessKey;
	}
	public String getBestCircleKey() {
		return bestCircleKey;
	}
	public void setBestCircleKey(String bestCircleKey) {
		this.bestCircleKey = bestCircleKey;
	}
	public String getRingKey() {
		return ringKey;
	}
	public void setRingKey(String ringKey) {
		this.ringKey = ringKey;
	}
	public String getPhotonChargeKey() {
		return photonChargeKey;
	}
	public void setPhotonChargeKey(String photonChargeKey) {
		this.photonChargeKey = photonChargeKey;
	}


	public String getPercentageKey() {
		return percentageKey;
	}


	public void setPercentageKey(String percentageKey) {
		this.percentageKey = percentageKey;
	}


	public String getoctantsHitKey() {
		return octantsHitKey;
	}


	public void setoctantsHitKey(String octantsHitKey) {
		this.octantsHitKey = octantsHitKey;
	}


	public String getBestRingPixelKey() {
		return bestRingPixelKey;
	}


	public void setBestRingPixelKey(String bestRingPixelKey) {
		this.bestRingPixelKey = bestRingPixelKey;
	}


	public boolean isShowRingKey() {
		return showRingKey;
	}


	public void setShowRingKey(boolean showRingKey) {
		this.showRingKey = showRingKey;
	}


	public String getBestXKey() {
		return bestXKey;
	}


	public void setBestXKey(String bestXKey) {
		this.bestXKey = bestXKey;
	}


	public String getBestYKey() {
		return bestYKey;
	}


	public void setBestYKey(String bestYKey) {
		this.bestYKey = bestYKey;
	}


	public String getBestRadiusKey() {
		return bestRadiusKey;
	}


	public void setBestRadiusKey(String bestRadiusKey) {
		this.bestRadiusKey = bestRadiusKey;
	}
}
