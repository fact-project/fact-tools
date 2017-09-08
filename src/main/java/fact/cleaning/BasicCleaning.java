package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.calibrationservice.CalibrationService;
import fact.container.PixelSet;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import stream.Data;
import stream.annotations.Parameter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


public class BasicCleaning {

	FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Parameter(required=true)
	CalibrationService calibService;


    protected PixelSet notUsablePixelSet = null;



	/**
	 * Add all pixel with a weight > corePixelThreshold to the showerpixel list.
	 * @param showerPixel
	 * @param photonCharge
	 * @param corePixelThreshold
	 * @return
	 */
	public ArrayList<Integer> addCorePixel(ArrayList<Integer> showerPixel, double[] photonCharge, double corePixelThreshold, ZonedDateTime eventTimeStamp) {
		int[] notUsablePixel = calibService.getNotUsablePixels(eventTimeStamp);
		if (notUsablePixel != null)
		{
			notUsablePixelSet = new PixelSet();
			for (int pix: notUsablePixel){
				notUsablePixelSet.addById(pix);
			}
		}
		for(int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++)
		{
			if (notUsablePixel != null){
				if (ArrayUtils.contains(notUsablePixel,pix) == true){
					continue;
				}
			}
			if (photonCharge[pix] > corePixelThreshold){
				showerPixel.add(pix);
			}
		}
		return showerPixel;
	}



	/**
	 * add all neighboring pixels of the core pixels, with a weight > neighborPixelThreshold to the showerpixellist
	 * @param showerPixel
	 * @param photonCharge
	 * @return
	 */
	public ArrayList<Integer> addNeighboringPixels(ArrayList<Integer> showerPixel, double[] photonCharge, double neighborPixelThreshold, ZonedDateTime eventTimeStamp)
	{
		int[] notUsablePixel = calibService.getNotUsablePixels(eventTimeStamp);
		ArrayList<Integer> newList = new ArrayList<>();
		for (int pix: showerPixel){
			FactCameraPixel[] currentNeighbors = pixelMap.getNeighboursFromID(pix);
			for (FactCameraPixel nPix:currentNeighbors){
				if (notUsablePixel != null){
					if (ArrayUtils.contains(notUsablePixel,nPix.chid) == true){
						continue;
					}
				}
				if(photonCharge[nPix.id] > neighborPixelThreshold && !newList.contains(nPix.id) && !showerPixel.contains(nPix.id)){
					newList.add(nPix.id);
				}
			}
		}
		showerPixel.addAll(newList);
		return showerPixel;
	}


	/**
	 * Remove all clusters of pixels with less than minNumberOfPixel pixels in the cluster
	 * @param list
	 * @param minNumberOfPixel
	 * @return
	 */
	public ArrayList<Integer> removeSmallCluster(ArrayList<Integer> list, int minNumberOfPixel)
	{
		ArrayList<ArrayList<Integer>> listOfLists = Utils.breadthFirstSearch(list);
		ArrayList<Integer> newList = new ArrayList<>();
		for (ArrayList<Integer> l: listOfLists){
			if(l.size() >= minNumberOfPixel){
				newList.addAll(l);
			}
		}
		return newList;
	}

	/**
	 * Remove pixel clusters which contains only pixels around a star
	 * @param showerPixel
	 * @param starPosition
	 * @param starSet PixelOverlay which contains the pixels around the star
	 * @param starRadiusInCamera Radius around the star position, which defines, which pixels are declared as star pixel
	 * @param log
	 * @return
	 */
	public ArrayList<Integer> removeStarIslands(ArrayList<Integer> showerPixel, double[] starPosition, PixelSet starSet, double starRadiusInCamera, Logger log) {

        FactCameraPixel pixel =  pixelMap.getPixelBelowCoordinatesInMM(starPosition[0], starPosition[1]);
        if (pixel == null){
			log.debug("Star not in camera window. No star islands are removed");
			return showerPixel;
        }
        int chidOfPixelOfStar = pixel.chid;
		List<Integer> starChidList = new ArrayList<>();

		starChidList.add(chidOfPixelOfStar);

		starSet.addById(chidOfPixelOfStar);

		for (FactCameraPixel px: pixelMap.getNeighboursFromID(chidOfPixelOfStar))
		{
				if (calculateDistance(px.id, starPosition[0], starPosition[1]) < starRadiusInCamera)
				{
					starSet.add(px);
					starChidList.add(px.id);
				}
		}

		ArrayList<ArrayList<Integer>> listOfLists = Utils.breadthFirstSearch(showerPixel);
		ArrayList<Integer> newList = new ArrayList<Integer>();
		for (ArrayList<Integer> l: listOfLists){
			if ((l.size() <= starChidList.size() && starChidList.containsAll(l)) == false)
			{
				newList.addAll(l);
			}
		}
		return newList;
	}

	public void addLevelToDataItem(ArrayList<Integer> showerPixel, String name, Data input){
		Integer[] level = new Integer[showerPixel.size()];
		showerPixel.toArray(level);

		if (level.length > 0)
		{
            PixelSet overlay = new PixelSet();
    		for(int pix : level){
    			overlay.addById(pix);
    		}
    		input.put(name, overlay);
		}
	}

	/**
	 * Calculates the Distance between a pixel and a given position
	 * @param chid
	 * @param x
	 * @param y
	 * @return
	 */
	private double calculateDistance(int chid, double x, double y)
	{
		double xdist = pixelMap.getPixelFromId(chid).getXPositionInMM() - x;
		double ydist = pixelMap.getPixelFromId(chid).getYPositionInMM() - y;

		return Math.sqrt((xdist*xdist)+(ydist*ydist));
	}




	public void setCalibService(CalibrationService calibService) {
		this.calibService = calibService;
	}


}
