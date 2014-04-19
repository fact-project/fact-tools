package fact.features;

import stream.Data;
import stream.Processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import fact.Constants;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;



public class MyonHoughTransform implements Processor {
	
	private String peaknessKey;
	private String distanceKey;
	private String ringKey;
	private String bestCircleKey;
	private String photonChargeKey;
	private String showRingKey;

	
	private double min_radius = 80; //minimal radius in mm
	private double max_radius = 140; //maximal radius in mm
	private double min_x = -300; //minimal center X in mm
	private double max_x = 300; //maximal center X in mm
	private double min_y = -300; //minimal center y in mm
	private double max_y = 300; //maximal center y in mm
	private int res_r = 18;
	private int res_x = 60;
	private int res_y = 60;
	
	
	
	final Logger log = LoggerFactory.getLogger(MyonHoughTransform.class);	
	
	@Override
	public Data process(Data input) {
		
				
		int[] ring = (int[]) input.get(ringKey);
		double[] photonCharge = (double[]) input.get(photonChargeKey);
				
		//double[] charge = (double[]) input.get(photonchargeKey);
		
		double[] xPositions = new double[ring.length];
		double[] yPositions = new double[ring.length];
		
		// Get X and Y Positions of the Pixel that survived Cleaning
		
		for(int i=0; i<ring.length; i++){
			xPositions[i] = fact.viewer.ui.DefaultPixelMapping.getPosXinMM(ring[i]);
			yPositions[i] = fact.viewer.ui.DefaultPixelMapping.getPosYinMM(ring[i]);
		}
		
		// Hough-vote-Matrix erzeugen:
		
		int[][][] HoughMatrix = new int[res_r+1][res_x+1][res_y+1];
		
		//log.info("HoughMatrixErzeugt");
		
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
		
		//log.info("Parameter-Arrays befüllt");
		
		// HoughTransform:
		
		double peakness = 0;
		int NoneZeroElems = 0;
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		int[] highest_pos = {0, 0, 0};
		int[] second_highest_pos = {0, 0, 0};
		int[] third_highest_pos = {0, 0, 0};
		
				
		for (int r = 0; r < circle_radius.length; r++){
			for (int x = 0; x < circle_x.length; x++){
				for (int y = 0; y < circle_y.length; y++){
					for(int pix = 0; pix < ring.length; pix++){
						double distance = Math.sqrt(Math.pow((xPositions[pix] - circle_x[x]), 2.0) + Math.pow((yPositions[pix] - circle_y[y]), 2.0));
						if(Math.abs(distance - circle_radius[r]) < 1.1 * fact.Constants.PIXEL_SIZE ){
							HoughMatrix[r][x][y] += photonCharge[ring[pix]];
						}
						stats.addValue(HoughMatrix[r][x][y]);
						if (HoughMatrix[r][x][y] !=0){
							NoneZeroElems += 1;
						}
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
		
		
		
		
		
        //log.info("HoughTransform durchgefügt");
		
		

		//log.info("Suche nach Maxima durchgeführt");
		
		
		double radius_1 = circle_radius[highest_pos[0]];
		double center_x_1 = circle_x[highest_pos[1]];
		double center_y_1 = circle_y[highest_pos[2]];
				
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
		
		
		
		//if (showRingKey=="true"){
//			double PixelPosX;
//			double PixelPosY;
//			double distance;
//			PixelSet bestCirclePixelSet = new PixelSet();
//			
//			for (int pix=0; pix<Constants.NUMBEROFPIXEL; pix++){
//				PixelPosX = fact.viewer.ui.DefaultPixelMapping.getPosXinMM(pix);
//				PixelPosY = fact.viewer.ui.DefaultPixelMapping.getPosYinMM(pix);
//				distance = Math.sqrt(Math.pow((PixelPosX - center_x_1), 2.0) + Math.pow((PixelPosY - center_y_1), 2.0));
//				if(Math.abs(distance - radius_1) < 1.1 * fact.Constants.PIXEL_SIZE ){
//					bestCirclePixelSet.add(new Pixel(pix));
//				}
//			}
//			input.put(bestCircleKey, bestCirclePixelSet);
		//}
		
		input.put(peaknessKey, peakness);
		input.put(distanceKey, ParamDistanceSum);
		
		
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
	public String getShowRingKey() {
		return showRingKey;
	}
	public void setShowRing(String showRingKey) {
		this.showRingKey = showRingKey;
	}
	public String getPhotonChargeKey() {
		return photonChargeKey;
	}
	public void setPhotonChargeKey(String photonChargeKey) {
		this.photonChargeKey = photonChargeKey;
	}
}
