package fact.container;

import org.jfree.chart.plot.IntervalMarker;
import stream.Data;

import java.io.Serializable;

public class JumpInfos implements Serializable {

    private static final long serialVersionUID = -4430692866625609266L;

    private PixelSet pixelWithSpikes;
    private PixelSet pixelWithSignalFlanks;
    private PixelSet pixelWithRinging;
    private PixelSet pixelWithCorrectedJumps;
    private PixelSet pixelWithWrongTimeDepend;

    public double[] averJumpHeights;

    public double[] fftResults;

    public IntervalMarker[] posMarkerUp;
    public IntervalMarker[] posMarkerDown;

    public JumpInfos(int numberOfPixel, int numberOfPatches, int roi) {
        averJumpHeights = new double[numberOfPatches];
        posMarkerUp = new IntervalMarker[numberOfPixel];
        posMarkerDown = new IntervalMarker[numberOfPixel];
        fftResults = new double[numberOfPixel * roi];

        pixelWithSpikes = new PixelSet();
        pixelWithSignalFlanks = new PixelSet();
        pixelWithRinging = new PixelSet();
        pixelWithCorrectedJumps = new PixelSet();
        pixelWithWrongTimeDepend = new PixelSet();
    }

    public void addPixelWithSpikes(int pixel) {
        pixelWithSpikes.addById(pixel);
    }

    public void addPatchWithSignalFlanks(int patch) {
        for (int px = 0; px < 9; px++) {
            pixelWithSignalFlanks.addById(patch * 9 + px);
        }
    }

    public void addPatchWithRinging(int patch) {
        for (int px = 0; px < 9; px++) {
            pixelWithRinging.addById(patch * 9 + px);
        }
    }

    public void addPatchWithCorrectedJumps(int patch) {
        for (int px = 0; px < 9; px++) {
            pixelWithCorrectedJumps.addById(patch * 9 + px);
        }
    }

    public void addPatchWithWrongTiming(int patch) {
        for (int px = 0; px < 9; px++) {
            pixelWithWrongTimeDepend.addById(patch * 9 + px);
        }
    }

    public void addInfosToDataItem(Data item, int prevEvent, String name, double deltaT) {
        item.put(name + prevEvent + "Jumps", averJumpHeights);
        item.put(name + prevEvent + "Time", deltaT);
        item.put(name + prevEvent + "Spikes", pixelWithSpikes);
        item.put(name + prevEvent + "SignalFlanks", pixelWithSignalFlanks);
        item.put(name + prevEvent + "Ringing", pixelWithRinging);
        item.put(name + prevEvent + "JumpsSet", pixelWithCorrectedJumps);
        item.put(name + prevEvent + "TimeSet", pixelWithWrongTimeDepend);
        item.put(name + prevEvent + "MarkerUp", posMarkerUp);
        item.put(name + prevEvent + "MarkerDown", posMarkerDown);
        item.put(name + prevEvent + "fftResults", fftResults);
    }

    public void addPosMarkerForPatch(int patch, short pos, boolean isStartCell) {
        for (int px = 0; px < 9; px++) {
            if (isStartCell == true) {
                posMarkerUp[patch * 9 + px] = new IntervalMarker(pos, pos + 1);
            } else {
                posMarkerDown[patch * 9 + px] = new IntervalMarker(pos, pos + 1);
            }
        }
    }


//
//
////	input.put("@"+Constants.KEY_COLOR + "_" +outputJumpsKey+prevEvent+"fftResults", "#0ACF1B");
}
