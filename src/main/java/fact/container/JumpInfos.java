package fact.container;

import java.io.Serializable;

import org.jfree.chart.plot.IntervalMarker;

import stream.Data;
import fact.hexmap.ui.overlays.PixelSetOverlay;

public class JumpInfos implements Serializable {

	private static final long serialVersionUID = -4430692866625609266L;
	
    private PixelSetOverlay pixelWithSpikes;
    private PixelSetOverlay pixelWithSignalFlanks;
    private PixelSetOverlay pixelWithRinging;
    private PixelSetOverlay pixelWithCorrectedJumps;
    private PixelSetOverlay pixelWithWrongTimeDepend;
	
    public double[] averJumpHeights;
	
    public double[] fftResults;
	
    public IntervalMarker[] posMarker;

	public JumpInfos(int numberOfPixel, int numberOfPatches, int roi) {
		averJumpHeights = new double[numberOfPatches];
		posMarker = new IntervalMarker[numberOfPixel];
		fftResults = new double[numberOfPixel*roi];
	}
	
	public void addPixelWithSpikes(int pixel){
		pixelWithSpikes.addById(pixel);
	}
	
	public void addPatchWithSignalFlanks(int patch){
		for (int px = 0 ; px < 9 ; px++)
		{
			pixelWithSignalFlanks.addById(patch*9+px);
		}
	}
	
	public void addPatchWithRinging(int patch){
		for (int px = 0 ; px < 9 ; px++)
		{
			pixelWithRinging.addById(patch*9+px);
		}
	}
	
	public void addPatchWithCorrectedJumps(int patch){
		for (int px = 0 ; px < 9 ; px++)
		{
			pixelWithCorrectedJumps.addById(patch*9+px);
		}
	}
	
	public void addPatchWithWrongTiming(int patch){
		for (int px = 0 ; px < 9 ; px++)
		{
			pixelWithWrongTimeDepend.addById(patch*9+px);
		}
	}
	
	public void addInfosToDataItem(Data input,int prevEvent, String name, double deltaT)
	{
		input.put(name+prevEvent+"Jumps", averJumpHeights);
		input.put(name+prevEvent+"Time", deltaT);
		input.put(name+prevEvent+"Spikes", pixelWithSpikes);
		input.put(name+prevEvent+"SignalFlanks", pixelWithSignalFlanks);
		input.put(name+prevEvent+"Ringing", pixelWithRinging);
		input.put(name+prevEvent+"JumpsSet", pixelWithCorrectedJumps);
		input.put(name+prevEvent+"TimeSet", pixelWithWrongTimeDepend);
		input.put(name+prevEvent+"Marker", posMarker);
		input.put(name+prevEvent+"fftResults", fftResults);
	}

	public void addPosMarkerForPatch(int patch, short pos) {
		for (int px = 0 ; px < 9 ; px++)
		{
			posMarker[patch*9+px] = new IntervalMarker(pos, pos+1);
		}
	}
    
    

//    
//	
////	input.put("@"+Constants.KEY_COLOR + "_" +outputJumpsKey+prevEvent+"fftResults", "#0ACF1B");
}
