package fact.triggerAnalysis;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class PatchDataArray implements Processor {
	
	@Parameter(required=true)
	private String dataKey = null;
	@Parameter(required=true)
	private String outputKey = null;
	@Parameter(required=false,defaultValue="9")
	private int numberOfPixelPerPatch = 9;

	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys( input, dataKey, "NROI", "NPIX");
		
		int roi = (Integer) input.get("NROI");
		int nPixel = (Integer) input.get("NPIX");
		int nPatches = nPixel/numberOfPixelPerPatch;
		
		double[] data = (double[]) input.get(dataKey);
		
		double[] result = new double[nPatches*roi];
		double[] resultViewer = new double[nPixel*roi];
				
		for (int patch = 0 ; patch < nPatches ; patch++)
		{
			for (int pix = 0 ; pix < numberOfPixelPerPatch ; pix++)
			{
				for (int sl = 0 ; sl < roi ; sl++)
				{
					int slice = (patch*numberOfPixelPerPatch+pix)*roi+sl;
					result[patch*roi+sl] += data[slice];
				}
			}
		}
		
		for (int patch = 0 ; patch < nPatches ; patch++)
		{
			for (int pix = 0 ; pix < numberOfPixelPerPatch ; pix++)
			{
				for (int sl = 0 ; sl < roi ; sl++)
				{
					int slice = (patch*numberOfPixelPerPatch+pix)*roi+sl;
					resultViewer[slice] = result[patch*roi+sl];
				}
			}
		}
		
		input.put(outputKey, result);
		input.put(outputKey+"_viewer", resultViewer);
		
		
		return input;
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public int getNumberOfPixelPerPatch() {
		return numberOfPixelPerPatch;
	}

	public void setNumberOfPixelPerPatch(int numberOfPixelPerPatch) {
		this.numberOfPixelPerPatch = numberOfPixelPerPatch;
	}

}
