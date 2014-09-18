package fact.features;

import fact.Utils;
import fact.container.PixelDistribution2D;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.EllipseOverlay;

import org.apache.commons.math3.linear.*;
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
    private  String showerKey =  null;

    //the in and outputkeys
    @Parameter(required = true)
    private String outputKey =null;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    // A logger
    static Logger log = LoggerFactory.getLogger(M3Long.class);

@Override
public Data process(Data input) {
	//get the required stuff from the getColorFromValue
	//in case the getColorFromValue doesn't contain a shower return the original input.
    Utils.isKeyValid(input, showerKey, int[].class);
    Utils.isKeyValid(input, weightsKey, double[].class);

    int[] showerPixel = (int[]) input.get(showerKey);
    double[] showerWeights = createShowerWeights(showerPixel, (double[]) input.get(weightsKey));


	double size = 0;
    for(double v : showerWeights){
        size += v;
    }


	double[] cog = calculateCog(showerWeights, showerPixel, size);


    // Calculate the weighted Empirical variance along the x and y axis.
    RealMatrix covarianceMatrix = calculateCovarianceMatrix(showerPixel, showerWeights, cog);

    //get the eigenvalues and eigenvectors of the matrix and weigh them accordingly.
    EigenDecomposition eig = new EigenDecomposition(covarianceMatrix);
    //turns out the eigenvalues describe the variance in the eigenbasis of the covariance matrix
    double varianceLong =  eig.getRealEigenvalue(0)/size;
    double varianceTrans =  eig.getRealEigenvalue(1)/size;

    double length = Math.sqrt(varianceLong);
    double width = Math.sqrt(varianceTrans);


    double delta = calculateDelta(eig);


    //Calculation of the showers statistical moments (Variance, Skewness, Kurtosis)
    // Rotate the shower by the angle delta in order to have the ellipse main axis in parallel to the Camera-Coordinates X-Axis
    //allocate variables for rotated coordinates
    double[] longitudinalCoords = new double[showerPixel.length];
    double[] transversalCoords = new double[showerPixel.length];

    for (int i = 0; i < showerPixel.length; i++){
        //translate to center
        double posx = pixelMap.getPixelFromId(showerPixel[i]).getXPositionInMM();
        double posy = pixelMap.getPixelFromId(showerPixel[i]).getYPositionInMM();
        //rotate
        double[] c = Utils.transformToEllipseCoordinates(posx, posy, cog[0], cog[1], delta);

        // fill array of new showerKey coordinates
        longitudinalCoords[i]			= c[0];
        transversalCoords[i]			= c[1];
    }

    double m3Long = calculateMoment(3,0, longitudinalCoords, showerWeights);
    m3Long /= Math.pow(length,3);
    double m3Trans = calculateMoment(3, 0, transversalCoords, showerWeights);
    m3Trans /= Math.pow(width, 3);

    double m4Long = calculateMoment(4, 0, longitudinalCoords, showerWeights);
    m4Long /= Math.pow(length,4);
    double m4Trans = calculateMoment(4, 0, transversalCoords, showerWeights);
    m4Trans /= Math.pow(width,4);

    PixelDistribution2D dist = new PixelDistribution2D(covarianceMatrix.getEntry(0, 0), covarianceMatrix.getEntry(1,1),
            covarianceMatrix.getEntry(0,1), cog[0], cog[1], varianceLong, varianceTrans, m3Long,
    		m3Trans, m4Long, m4Trans, delta, size);

    //add calculated shower parameters to data item
    input.put(outputKey , 		dist);
    input.put("varianceLong",	varianceLong );
    input.put("varianceTrans", 	varianceTrans );
    input.put("M3Long",			m3Long );
    input.put("M3Trans", 		m3Trans );
    input.put("M4Long", 		m4Long );
    input.put("M4Trans", 		m4Trans );
    input.put("COGx", 			cog[0] );
    input.put("COGy", 			cog[1] );
    input.put("Length", length );
    input.put("Width", width );
    input.put("Delta", delta );


    double[] center = calculateCenter(showerPixel);
    input.put("Ellipse", new EllipseOverlay(center[0] , center[1], width , length  , delta));

    input.put("@width", width );
    input.put("@length", length );

	//look at what i found
	//V=cov(x,y);
	//[vec,val]=eig(V);
	//angles=atan2( vec(2,:),vec(1,:) ); 
    
	return input;
}


    public double[] createShowerWeights(int[] shower, double[] pixelWeights){
        double[] weights = new double[shower.length];
        for (int i = 0; i < shower.length; i++) {
            weights[i] = pixelWeights[shower[i]];
        }
        return  weights;
    }

    public double calculateDelta(EigenDecomposition eig) {
        //calculate the angle between the eigenvector and the camera axis.
        //So basicly the angle between the major-axis of the ellipse and the camrera axis.
        //this will be written in radians.
        double longitudinalComponent = eig.getEigenvector(0).getEntry(0);
        double transversalComponent = eig.getEigenvector(0).getEntry(1);
        return Math.atan(transversalComponent/longitudinalComponent);
    }

    public double calculateMoment(int moment, double mean, double[] values, double[] weights){
        double sumWeights = 0;
        double m = 0;
        for (int i = 0; i < values.length; i++) {
            sumWeights +=weights[i];
            m += weights[i] * Math.pow(values[i] - mean ,moment);
        }
        return m/sumWeights;
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

    public double[] calculateCenter(int[] showerPixel){

        double[] cog = {0, 0};
        //find center of the shower pixels.
        for (int pix: showerPixel)
        {
            cog[0]            += pixelMap.getPixelFromId(pix).getXPositionInMM();
            cog[1]            += pixelMap.getPixelFromId(pix).getYPositionInMM();
        }
        cog[0]                /= showerPixel.length;
        cog[1]                /= showerPixel.length;
        return cog;
    }


    public RealMatrix calculateCovarianceMatrix(int[] showerPixel, double[] showerWeights, double[] cog) {
        double variance_xx = 0;
        double variance_yy = 0;
        double covariance_xy = 0;
        int i = 0;
        for (int pix: showerPixel )
        {
            double weight = showerWeights[i];
            double posx = pixelMap.getPixelFromId(pix).getXPositionInMM();
            double posy = pixelMap.getPixelFromId(pix).getYPositionInMM();

            variance_xx            += weight * (posx - cog[0]) * (posx - cog[0]);
            variance_yy            += weight * (posy - cog[1]) * (posy - cog[1]);
            covariance_xy          += weight * (posx - cog[0]) * (posy - cog[1]);

            i++;
        }

        double[][] matrixData = {   {variance_xx, covariance_xy},
                                    {covariance_xy,variance_yy }
                                };
        return MatrixUtils.createRealMatrix(matrixData);
    }


public void setWeightsKey(String wheights) {
	this.weightsKey = wheights;
}



public void setShowerKey(String showerKey) {
	this.showerKey = showerKey;
}



public void setOutputKey(String outputKey) {
	this.outputKey = outputKey;
}



}
