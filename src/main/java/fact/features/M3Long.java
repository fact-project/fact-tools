package fact.features;

import fact.Utils;
import fact.container.PixelDistribution2D;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


public class M3Long implements Processor {

    @Parameter(required = true)
    private String weightsKey =  null;
    @Parameter(required = true, description = "The key to the showerPixel. " +
            "That is some sort of int[] containing pixel chids.")
    private  String pixelSetKey =  null;
    
    @Parameter(required = true)
	private String m3lOutputKey = "m3l";
    @Parameter(required = true)
	private String m3tOutputKey = "m3t";

    @Parameter(required = true)
    private String distributionKey =  null;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    // A logger
    static Logger log = LoggerFactory.getLogger(M3Long.class);

@Override
public Data process(Data input) {
	//get the required stuff from the getColorFromValue
	//in case the getColorFromValue doesn't contain a shower return the original input.
    Utils.isKeyValid(input, pixelSetKey, PixelSet.class);
    Utils.isKeyValid(input, weightsKey, double[].class);
    Utils.isKeyValid(input, distributionKey, PixelDistribution2D.class);

    int[] showerPixel = ((PixelSet) input.get(pixelSetKey)).toIntArray();
    double[] showerWeights = createShowerWeights(showerPixel, (double[]) input.get(weightsKey));
    PixelDistribution2D dist = (PixelDistribution2D) input.get(distributionKey);
    //double[] showerCenter = getCenter(showerPixel);


	double size = 0;
    for(double v : showerWeights){
        size += v;
    }

    double[] cog = calculateCog(showerWeights, showerPixel, size);
    double[] sumd3w = calculateCubicSum(showerPixel, showerWeights, cog);
    double[] sumd2w = calculateSquaredSum(showerPixel, showerWeights, cog);

    double c = Math.cos(dist.getAngle());
    double s = Math.sin(dist.getAngle());
//    const Double_t m3l = c*c*c*sumdx3w + s*s*s*sumdy3w + 3*(s*c*c*sumdx2dyw + c*s*s*sumdxdy2w);
//    const Double_t m3t = c*c*c*sumdy3w - s*s*s*sumdx3w + 3*(s*s*c*sumdx2dyw - s*c*c*sumdxdy2w);
    double m3l = Math.pow(c,3) * sumd3w[0] +  Math.pow(s,3)*sumd3w[1] + 3*(s*c*c*sumd2w[0] + c*s*s*sumd2w[1]) ;
    double m3t = Math.pow(c,3) * sumd3w[1] -  Math.pow(s,3)*sumd3w[0] + 3*(s*s*c*sumd2w[0] - s*c*c*sumd2w[1]) ;



    input.put(m3lOutputKey,Math.cbrt(m3l));
    input.put(m3tOutputKey,Math.cbrt(m3t));

	return input;
}

    private double[] calculateSquaredSum(int[] showerPixel, double[] showerWeights, double[] cog) {
        double[] sum2w = {0,0};
        for (int i = 0; i < showerPixel.length; i++) {
            
            int pix = showerPixel[i];

            double dx = pixelMap.getPixelFromId(pix).getXPositionInMM() - cog[0];
            double dy = pixelMap.getPixelFromId(pix).getYPositionInMM() - cog[1];

            sum2w[0] += dx*dx*dy * showerWeights[i];
            sum2w[1] += dx*dy*dy * showerWeights[i];
        }
        return sum2w;
    }

    private double[] calculateCubicSum(int[] showerPixel, double[] showerWeights, double[] cog) {
        double[] sum3w = {0,0};
        for (int i = 0; i < showerPixel.length ; i++) {
            int pix = showerPixel[i];
            double dx = pixelMap.getPixelFromId(pix).getXPositionInMM() - cog[0];
            double dy = pixelMap.getPixelFromId(pix).getYPositionInMM() - cog[1];

            sum3w[0] += Math.pow(dx, 3) * showerWeights[i];
            sum3w[1] += Math.pow(dy, 3) * showerWeights[i];
        }
        return sum3w;
    }



    public double[] createShowerWeights(int[] shower, double[] pixelWeights){
        double[] weights = new double[shower.length];
        for (int i = 0; i < shower.length; i++) {
            weights[i] = pixelWeights[shower[i]];
        }
        return  weights;
    }


    public double[] calculateCog(double[] weights, int[] showerPixel, double size){

        double[] cog = {0, 0};
        //find weighted center of the shower pixels.
        int i = 0;
        for (int pix: showerPixel)
        {
            cog[0]            += weights[i] * pixelMap.getPixelFromId(pix).getXPositionInMM();
            cog[1]            += weights[i] * pixelMap.getPixelFromId(pix).getYPositionInMM();
            i++;
        }
        cog[0]                /= size;
        cog[1]                /= size;
        return cog;
    }

	public void setWeightsKey(String weights) {
		this.weightsKey = weights;
	}

    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }

    public String getDistributionKey() {
		return distributionKey;
	}

	public void setDistributionKey(String distributionKey) {
		this.distributionKey = distributionKey;
	}

	public String getM3lOutputKey() {
		return m3lOutputKey;
	}

	public void setM3lOutputKey(String m3lOutputKey) {
		this.m3lOutputKey = m3lOutputKey;
	}

	public String getM3tOutputKey() {
		return m3tOutputKey;
	}

	public void setM3tOutputKey(String m3tOutputKey) {
		this.m3tOutputKey = m3tOutputKey;
	}



}
