package fact.container;

import java.io.Serializable;

import org.jfree.chart.plot.IntervalMarker;

import fact.hexmap.ui.overlays.PixelSetOverlay;

public class JumpInfos implements Serializable {

	private static final long serialVersionUID = -4430692866625609266L;
    public PixelSetOverlay pixelWithSpikes;
    public PixelSetOverlay pixelWithSignalFlanks;
    public PixelSetOverlay pixelWithRinging;
    public PixelSetOverlay pixelWithCorrectedJumps;
    public PixelSetOverlay pixelWithWrongTimeDepend;
	
    public double[] averJumpHeights = null;
	
    public double[] fftResults = null;
	
    public IntervalMarker[] posMarker;

//    
//	input.put(outputJumpsKey+prevEvent+"Jumps", averJumpHeights);
//	input.put(outputJumpsKey+prevEvent+"Time", deltaT);
//////	input.put(outputJumpsKey+prevEvent+"Spikes", pixelWithSpikes);
//////	input.put(outputJumpsKey+prevEvent+"SignalFlanks", pixelWithSignalFlanks);
//////	input.put(outputJumpsKey+prevEvent+"Ringing", pixelWithRinging);
////	input.put(outputJumpsKey+prevEvent+"JumpsSet", pixelWithCorrectedJumps);
////	if (pixelWithWrongTimeDepend.size() > 0)
////	{
////		input.put(outputJumpsKey+prevEvent+"TimeSet", pixelWithWrongTimeDepend);
////	}
////	input.put(outputJumpsKey+prevEvent+"Marker", posMarker);
////	input.put(outputJumpsKey+prevEvent+"patchAverage", patchAverageCamera);
////	input.put(outputJumpsKey+prevEvent+"patchAverageDeriv", patchAverageDerivCamera);
////	input.put(outputJumpsKey+prevEvent+"fftResults", fftResults);
////	input.put("@"+Constants.KEY_COLOR + "_" +outputJumpsKey+prevEvent+"fftResults", "#0ACF1B");
}
