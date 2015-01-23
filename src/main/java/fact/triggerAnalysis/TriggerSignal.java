package fact.triggerAnalysis;

import fact.Utils;
import fact.hexmap.ui.overlays.PixelSetOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class TriggerSignal implements Processor {
	@Parameter(required = true)
	private String triggerDataKey = null;
	@Parameter(required = true)
	private String outputKey = null;
	@Parameter(required = false, defaultValue = "210")
	private double threshold = 210;
	@Parameter(required = false, defaultValue = "30")
	private int searchWindowStart = 30;
	@Parameter(required = false, defaultValue = "100")
	private int searchWindowRange = 100;
	@Parameter(required = false, defaultValue = "9")
	private int numberPixelPerPatch = 9;

	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(input, triggerDataKey);
		
		double[] triggerData = (double[]) input.get(triggerDataKey);
		
		int roi = (Integer) input.get("NROI");
		int nPix = (Integer) input.get("NPIX");
		
		int nPatches = triggerData.length / roi;
		
		double[] triggerSignals = new double[nPatches];
		double[] triggerSignalsViewer = new double[nPix*roi];
		
		PixelSetOverlay triggeredPixel = new PixelSetOverlay();
		
		int[] window = Utils.getValidWindow(searchWindowStart, searchWindowRange, 0, roi);
		
		for (int patch = 0 ; patch < nPatches ; patch++)
		{
			int ToT = 0;
			for (int sl = window[0] ; sl < window[1] ; sl++)
			{
				int slice = patch*roi+sl;
				
				if (triggerData[slice] > threshold)
				{
					ToT += 1;
					for (int pix = 0 ; pix < numberPixelPerPatch ; pix++)
					{
						int viewerSlice = (patch*numberPixelPerPatch+pix)*roi+sl;
						triggerSignalsViewer[viewerSlice] = threshold;
					}
				}
			}
			triggerSignals[patch] = ToT;
		}
		for (int patch = 0 ; patch < nPatches ; patch++)
		{
			if (triggerSignals[patch] > 0)
			{
				for (int pix = 0 ; pix < numberPixelPerPatch ; pix++)
				{
					triggeredPixel.addById(patch*numberPixelPerPatch+pix);
				}
			}
		}
		
		input.put(outputKey, triggerSignals);
		input.put(outputKey+"_viewer", triggerSignalsViewer);
		input.put(outputKey+"_set", triggeredPixel);
		
		return input;
	}

	public String getTriggerDataKey() {
		return triggerDataKey;
	}

	public void setTriggerDataKey(String triggerDataKey) {
		this.triggerDataKey = triggerDataKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getSearchWindowStart() {
		return searchWindowStart;
	}

	public void setSearchWindowStart(int searchWindowStart) {
		this.searchWindowStart = searchWindowStart;
	}

	public int getSearchWindowRange() {
		return searchWindowRange;
	}

	public void setSearchWindowRange(int searchWindowRange) {
		this.searchWindowRange = searchWindowRange;
	}

	public int getNumberPixelPerPatch() {
		return numberPixelPerPatch;
	}

	public void setNumberPixelPerPatch(int numberPixelPerPatch) {
		this.numberPixelPerPatch = numberPixelPerPatch;
	}

}
