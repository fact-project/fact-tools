package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.Constants;
import fact.viewer.ui.DefaultPixelMapping;

/**
 * This class calculates the concentration at the center of gavity
 * @param cog The key of the center of gravity (calculated by DistributionFromShower-processor, x and y will be added automatically)
 * @param PhotonCharge The photoncharge!
 *
 * @return ConcCOG The concentration of the 3 closest pixel to the COG 
 */
public class ConcentrationAtCenterOfGravity implements Processor
{
	
	
	@Override
	public Data process(Data input)
	{
		return input;
	}
	
	private float[] cogArray = null;
	private float[] photonChargeArray = null;
	
	private String photonCharge = "photoncharge";
	private String cog = "COG";
	private String outputKey = "ConcCOG";
	
}