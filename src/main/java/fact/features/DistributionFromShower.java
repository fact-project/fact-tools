package fact.features;

import java.awt.Color;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.image.overlays.LineOverlay;
import fact.statistics.PixelDistribution2D;
import fact.viewer.ui.DefaultPixelMapping;


public class DistributionFromShower implements StatefulProcessor {
	
	private float[] mpGeomXCoord;
	private float[] mpGeomYCoord;
	
	private float[] mpEigenGeomXCoord;
	private float[] mpEigenGeomYCoord;

//	private float[] photonCharge;
	public float mCenterOfGravityX;
	public float mCenterOfGravityY;

	
// A logger
static Logger log = LoggerFactory.getLogger(DistributionFromShower.class);

//what do we need to calculate the ellipse?
private String weights =  null;
private String pixel =  null;
private double[] wheightsArray;
private int[] showerPixel;

//hte in and outputkeys
private String outputKey ="Hillas_";
private String key = null;


@Override
public void init(ProcessContext context) throws Exception {
    mpGeomXCoord            = DefaultPixelMapping.getGeomXArray();
    mpGeomYCoord            = DefaultPixelMapping.getGeomYArray();
    if(key == null && pixel != null){
    	key = pixel;
    }
}

@Override
public Data process(Data input) {
	//get the required stuff from the map
	//in case the map doesn't contain a shower return the original input. 

	try{
		showerPixel= (int[]) input.get(key);
		if(showerPixel ==  null){
			log.info("No showerpixel in this event. Not calculating Ellipse");
			return input;
		}
	} catch (ClassCastException e){
		log.error("showerIds is not of type int[]. Aborting");
		return null;
	}

	if(weights == null){
		log.info("Wheights were null. Setting all weights to 1");

		wheightsArray = new double[Constants.NUMBEROFPIXEL];
		for(int i = 0; i < wheightsArray.length; i++){
			wheightsArray[i] = 1.0f;
		}
	} else {
		try{
			wheightsArray = (double[]) input.get(weights);
			if(wheightsArray ==  null){
				log.error("The values for weight were not found in the map. Aborting");
				return null;
			}
		} catch (ClassCastException e){
			log.error("Wheights is not of type double[]. Aborting");
			return null;
		}
	}
	
	
	
	//calculate the "size" of the shower
    double size = 0.0;
    for(int pix: showerPixel){
    	size += wheightsArray[pix];
    }
	double cogX = 0;
	double cogY = 0;
	//find wheighted center of the shower. assuming we have no islands this works.
    for (int pix: showerPixel)
    {
        cogX            += wheightsArray[pix] * mpGeomXCoord[pix];
        cogY            += wheightsArray[pix] * mpGeomYCoord[pix];
    }
    //divide the center coordinates by size. I'm not sure if this is correct. I checked it. It is.
    cogX                /= size;
    cogY                /= size;
	

    // Calculate the weighted Empirical variance along the x and y axis.
    double variance_xx         = 0;
    double variance_yy         = 0;
    double covariance_xy         = 0;
    
    for (int pix: showerPixel )
    {
        variance_xx            += wheightsArray[pix] * (mpGeomXCoord[pix] - cogX) * (mpGeomXCoord[pix] - cogX);
        variance_yy            += wheightsArray[pix] * (mpGeomYCoord[pix] - cogY) * (mpGeomYCoord[pix] - cogY);
        covariance_xy          += wheightsArray[pix] * (mpGeomXCoord[pix] - cogX) * (mpGeomYCoord[pix] - cogY);
    }
	
    //create a covariance matrix
//    Covariance cov = new Covariance(data, true);
    double[][] matrixData = {   {variance_xx, covariance_xy}, 
    							{covariance_xy,variance_yy }
    						};
    RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
    
    //get the eigenvalues and eigenvectors of the matrix and weigh them accordingly. (size is the sum of weights)
    EigenDecomposition eig = new EigenDecomposition(m);
    double eigenValue1 = eig.getRealEigenvalue(0);
    double eigenValue2 = eig.getRealEigenvalue(1);
    //turns out the eigenvalues describe the variance in the eigenbasis of the covariance matrix
    double eigenVarianceX =  eigenValue1/size;
    double eigenVarianceY =  eigenValue2/size;
//    System.out.println("width: " + Math.sqrt(eigenValue2/size) );
    
    //calculate the angle between the eigenvector and the camera axis. So basicly the angle between the major-axis of the ellipse and the camrera axis.
    //this will be written in radians.
    double x = eig.getEigenvector(0).getEntry(0);
    double y = eig.getEigenvector(0).getEntry(1);
    double delta = Math.atan2(y,x);
//    System.out.println("angle of eigenvectors in radians: " + delta + " in degrees: " + delta*180/Math.PI);
//    System.out.println("--------");
    
    //Calculation of the showers statistical moments (Variance, Skewness, Kurtosis)
    //=======================================
    // Rotate the shower by the angle delta in order to have the ellipse main axis in parallel to the Camera-Coordinates X-Axis
    
    //rotation Matirix
    double[][] rotMatrixZ = {	{Math.cos(-delta),-Math.sin(-delta)},
    							{Math.sin(-delta), Math.cos(-delta)}
    						};
    RealMatrix rotZ = MatrixUtils.createRealMatrix(rotMatrixZ);
    
    //allocate variables for rotated coordinates    
    mpEigenGeomXCoord = new float[mpGeomXCoord.length];
    mpEigenGeomYCoord = new float[mpGeomXCoord.length];

    //Loop over pixel in Order to calculate the rotated coordinates
    for (int pix: showerPixel )
    {
    	// set pixel coordinates to be a vector
    	RealVector pixCoordinates 		= new ArrayRealVector(new double[] {mpGeomXCoord[pix], mpGeomYCoord[pix]}, false );
    	
    	// rotate coordinates vector in a system parallel to the cartesic camera coordinates
    	RealVector eigenPixCoordinates 	= rotZ.operate(pixCoordinates);
    	
    	// fill array of new pixel coordinates
    	mpEigenGeomXCoord[pix]			= (float) eigenPixCoordinates.getEntry(0);
    	mpEigenGeomYCoord[pix]			= (float) eigenPixCoordinates.getEntry(1);
    }
    
    //create COG coordinates vector
    RealVector cogVec 		= new ArrayRealVector(new double[] {cogX, cogY}, false );
    // rotate COG coordinates vector in a system parallel to the cartesic camera coordinates
    RealVector eigenCogVec 	= rotZ.operate(cogVec);
    
    //allocate variables for x and s coordinate of COG in the rotated shower system
    float eigenCogX = (float) eigenCogVec.getEntry(0);
    float eigenCogY = (float) eigenCogVec.getEntry(1);
    
    // allocate variables for moments of longitudenal and transversal shower distribution 
    double[] distMoment_xx = new double[4];
    double[] distMoment_yy = new double[4];
     
    //loop over shower pixel to calculate statistical moments of the shower distributions
    for (int pix: showerPixel )
    {
    	//loop over moments in x direction
    	for (int moment=0; moment < distMoment_xx.length; moment++){
    		distMoment_xx[moment] += wheightsArray[pix] * Math.pow((mpEigenGeomXCoord[pix] - eigenCogX), moment + 1);
    	}
    	//loop over moments in y direction
    	for (int moment=0; moment < distMoment_xx.length; moment++){
    		distMoment_yy[moment] += wheightsArray[pix] * Math.pow((mpEigenGeomYCoord[pix] - eigenCogY), moment + 1);
    	}
    }
    
    //loop over moments in both directions an normalize with size
    for (int moment=0; moment < distMoment_xx.length; moment++){
    	distMoment_xx[moment] /= size;
    	distMoment_yy[moment] /= size;
    }
    
    PixelDistribution2D dist = new PixelDistribution2D(variance_xx, variance_yy, covariance_xy, cogX, cogY, eigenVarianceX, eigenVarianceY, distMoment_xx[2], 
    		distMoment_yy[2], distMoment_xx[3], distMoment_yy[3], delta, size);
    
    //add calculated shower parameters to data item
    input.put(outputKey , 		dist);
    input.put("varianceLong",	eigenVarianceX );
    input.put("varianceTrans", 	eigenVarianceY );
    input.put("M3Long",			distMoment_xx[2] );
    input.put("M3trans", 		distMoment_yy[2] );
    input.put("M4Long", 		distMoment_xx[3] );
    input.put("M4Trans", 		distMoment_yy[3] );
    input.put("COGx", 			cogX );
    input.put("COGy", 			cogY );
//    input.put(outputKey+"_width", Math.sqrt(eigenValue1/size) );
//    input.put(outputKey+"_length", Math.sqrt(eigenValue2/size) );
//    input.put(outputKey+"_delta", delta );
    
    input.put(Constants.ELLIPSE_OVERLAY, new LineOverlay(cogX, cogY, delta, Color.green));
    
	//look at what i found
	//V=cov(x,y);
	//[vec,val]=eig(V);
	//angles=atan2( vec(2,:),vec(1,:) ); 
    
    
	return input;
}


public String getWeights() {
	return weights;
}
public void setWeights(String wheights) {
	this.weights = wheights;
}



public String getPixel() {
	return pixel;
}
@Parameter(required = true, description = "The key to the showerPixel. That is some sort of int[] containing pixel chids.  ", defaultValue = "MaxAmplitudePositons")
public void setPixel(String pixel) {
	this.pixel = pixel;
}




@Override
public void resetState() throws Exception {
	// TODO Auto-generated method stub
}
@Override
public void finish() throws Exception {
	// TODO Auto-generated method stub
}


public String getKey() {
	return key;
}
@Parameter(required = false, description = "This is just an alias for the \"Pixel\" key")
public void setKey(String key) {
	this.key = key;
}

public String getOutputKey() {
	return outputKey;
}
public void setOutputKey(String outputKey) {
	this.outputKey = outputKey;
}

}
