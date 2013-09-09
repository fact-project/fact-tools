package fact.features;

import java.awt.Color;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import fact.Constants;
import fact.image.overlays.LineOverlay;
import fact.statistics.PixelDistribution2D;
import fact.viewer.ui.DefaultPixelMapping;


public class DistributionFromShower implements StatefulProcessor {
	
	private float[] mpGeomXCoord;
	private float[] mpGeomYCoord;

//	private float[] photonCharge;
	public float mCenterOfGravityX;
	public float mCenterOfGravityY;
//	public float alpha;
//	public float alphaOff1;
//	public float alphaOff2;
//	public float alphaOff3;
//	private float sourcePosX;
//	private float sourcePosY;
	
// A logger
static Logger log = LoggerFactory.getLogger(DistributionFromShower.class);

//what do we need to calculate the ellipse?
private String weights =  null;
private String pixel =  null;
private float[] wheightsArray;
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

		wheightsArray = new float[showerPixel.length];
		for(int i = 0; i < wheightsArray.length; i++){
			wheightsArray[i] = 1.0f;
		}
	} else {
		try{
			wheightsArray = (float[]) input.get(weights);
			if(wheightsArray ==  null){
				log.error("The values for weight were not found in the map. Aborting");
				return null;
			}
		} catch (ClassCastException e){
			log.error("Wheights is not of type float[]. Aborting");
			return null;
		}
	}
	
	
	
	//calculate the "size" of the shower
    double size = 0.0;
    for(int pix: showerPixel){
    	size += wheightsArray[pix];
    }
	double centerX = 0;
	double centerY = 0;
	//find wheighted center of the shower. assuming we have no islands this works.
    for (int pix: showerPixel)
    {
        centerX            += wheightsArray[pix] * mpGeomXCoord[pix];
        centerY            += wheightsArray[pix] * mpGeomYCoord[pix];
    }
    //divide the center coordinates by size. I'm not sure if this is correct. I checked it. It is.
    centerX                /= size;
    centerY                /= size;
	
    // These names are somewhat misleading. I think what's happening here is a linear regression on both axis.
    // See also http://de.wikipedia.org/wiki/Lineare_Regression#Berechnung_der_Regressionsgeraden
    // alpha here is S in the wikipedia article. See the equation for calculating b. This is whats happening here. In both directions.
    // this is the correct way to do this. Since we can't assume any correlation(that is direction in our case) beforehand.
    // See also http://de.wikipedia.org/wiki/Lineare_Regression#Bildliche_Darstellung_und_Interpretation
    // the real difference here is the weighting.  
    // The alpha is actually the empricial variance between the datapoints.
    double variance_xx         = 0;
    double variance_yy         = 0;
    double covariance_xy         = 0;
    
    for (int pix: showerPixel )
    {
        variance_xx            += wheightsArray[pix] * (mpGeomXCoord[pix] - centerX) * (mpGeomXCoord[pix] - centerX);
        variance_yy            += wheightsArray[pix] * (mpGeomYCoord[pix] - centerY) * (mpGeomYCoord[pix] - centerY);
        covariance_xy          += wheightsArray[pix] * (mpGeomXCoord[pix] - centerX) * (mpGeomYCoord[pix] - centerY);
    }
	
//    Covariance cov = new Covariance(data, true);
    double[][] matrixData = {   {variance_xx, covariance_xy}, 
    							{covariance_xy,variance_yy }
    						};
    RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
    EigenDecomposition eig = new EigenDecomposition(m);
    double eigenValue1 = eig.getRealEigenvalue(0);
    double eigenValue2 = eig.getRealEigenvalue(1);
    double eigenVarianceX =  eigenValue1/size;
    double eigenVarianceY =  eigenValue2/size;
//    System.out.println("width: " + Math.sqrt(eigenValue2/size) );
    
    double x = eig.getEigenvector(0).getEntry(0);
    double y = eig.getEigenvector(0).getEntry(1);
    double delta = Math.atan2(y,x);
//    System.out.println("angle of eigenvectors: " + delta );
//    System.out.println("--------");
    PixelDistribution2D dist = new PixelDistribution2D(variance_xx, variance_yy, covariance_xy, centerX, centerY, eigenVarianceX, eigenVarianceY,  delta, size);
    input.put(outputKey , dist);
//    input.put(outputKey+"_width", Math.sqrt(eigenValue1/size) );
//    input.put(outputKey+"_length", Math.sqrt(eigenValue2/size) );
//    input.put(outputKey+"_delta", delta );
    
    input.put(Constants.ELLIPSE_OVERLAY, new LineOverlay(centerX, centerY, delta, Color.green));

    //At this point you usually want to get the regression coefficents. (The b in the wikipedia article)
    //What happens however is quite unusual. Which means I dont understand it :P
    //A variable called A. What does it do?? It has a sqrt. So lets look for equations with a sqrt. None fit. Lets find some other wikipedia article with a sqrt.
    //Aha. Some 2d-Gaussian stuff is going on. See the Blobel book. This equation  an also be found in the bretzens dissertation
    
    //Apparently if one assumes that we have an underlying 2d gaussian distribution and we assume the original data points are in some sort of euclidian system 
    //then you can define an angle between the axis and the "rotation" of the gaussian distribution
//    double A                = 0;
//    A       = ( Math.sqrt( (variance_yy - variance_xx)*(variance_yy - variance_xx) + (2 * covariance_xy)*(2 * covariance_xy) ) + (variance_yy - variance_xx) )
//              / (2 * covariance_xy);
//    
//    //lets get an angle. Most people (like the blobel fella) call this phi. But this is called the hillasparameter delta!
//	double delta      = Math.atan(A);
	
	
	//now lets do it again. like blobel this time
//	double B = 0;
//	B = (2*covariance_xy)/(variance_xx - variance_yy);
//	double delta = Math.atan2(2*covariance_xy,(variance_xx - variance_yy) ) * 0.5;
	
	//now lets rotate and calculate all that stuff again.
	
//	double centerXrotated = 0;
//	double centerYrotated = 0;
//	//find wheighted center of the shower. assuming we have no islands this works.
//    for (int pix: showerPixel)
//    {
//    	double[] c = DefaultPixelMapping.rotate(pix, delta);
//    	centerXrotated            += wheightsArray[pix] * c[0];
//    	centerYrotated            += wheightsArray[pix] * c[1];
//    }
//	centerXrotated /= size;
//	centerYrotated /= size;
	
	
//	double width = 0;
//	double length = 0;
//    for (int pix: showerPixel )
//    {
//    	double[] c = DefaultPixelMapping.rotate(pix, delta);
//        width             += wheightsArray[pix] * (c[0] - centerXrotated) * (c[0] - centerXrotated);
//        length            += wheightsArray[pix] * (c[1] - centerYrotated) * (c[1] - centerYrotated);
//        covariance_xy          += wheightsArray[pix] * (c[0] - centerXrotated) * (c[1] - centerYrotated);
//    }
//    
//    double testAngle = Math.atan2(2*covariance_xy,(width - length) ) * 0.5;
    
//    input.put(Constants.ELLIPSE_OVERLAY + "_bla", new LineOverlay(0, 0, 3 , Color.blue) );
//    System.out.println("---- width: " + width +  "  ");
//    System.out.println("---- length: " + length +  "  ");
    

	
//	double mLineSlope              = Math.tan(delta);
//	double mLineIntercept          = centerY - (mLineSlope * centerX);
	
//	double N = size;
//    mSemiMajorAxis          = 1 / Math.sqrt(N * ( A*A + 1 ))
//    * Math.sqrt( (A*A + 1)*variance_yy
//           + Math.sqrt( (variance_yy - variance_xx)*(variance_yy - variance_xx) + (2*covariance_xy)*(2*covariance_xy) )
//       );
//    
//    mSemiMinorAxis          = 1 / Math.sqrt(N * ( A*A + 1 ))
//    * Math.sqrt( (A*A + 1)*variance_xx
//           - Math.sqrt( (variance_yy - variance_xx)*(variance_yy - variance_xx) + (2*covariance_xy)*(2*covariance_xy) )
//       );
    
//	length             = mSemiMajorAxis;
//    width              = mSemiMinorAxis;
//    
//    area               = length * width * Math.PI;
//    mDelta             = mLineGradientAngle / Math.PI * 180;
	
    
//    System.out.println("---- delta: " + delta/ Math.PI * 180 + "  ");

    
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
public void setKey(String key) {
	this.key = key;
}

public String getOutputKey() {
	return outputKey;
}
public void setOutputKey(String outputKey) {
	this.outputKey = outputKey;
}
	
//public void calculateCenterOfGravity(int[] showerPixel)
//	{
//	
//	    mCenterOfGravityX           = 0;
//	    mCenterOfGravityY           = 0;
//	    mGeomCenterX                = 0;
//	    mGeomCenterY                = 0;
//	    for (int pix : showerPixel)
//	    {
//	    	//assuming the ids in the showerpixelArray are chids. otherwise map this to chids first;
//	        mGeomCenterX            += mpGeomXCoord[pix];
//	        mGeomCenterY            += mpGeomYCoord[pix];
//	        mCenterOfGravityX       += mpGeomXCoord[pix] * mpWeights[pix];
//	        mCenterOfGravityY       += mpGeomYCoord[pix] * mpWeights[pix];
//	    }
//	    mCenterOfGravityX           /= size;
//	    mCenterOfGravityY           /= size;
//	    mGeomCenterX                /= mNumberOfShowerPixel;
//	    mGeomCenterY                /= mNumberOfShowerPixel;
//	}

//public void calculateAsymmetry()
//{
// 
//    float geo_x             = 0;
//    float geo_y             = 0;
//    double ell_x             = 0;
//    double ell_y             = 0;
//    float auxiliary_angle   = 0;
//    auxilaryDistance    = 0;
//
//    for (int pix : showerPixel)
//    {
//        geo_x               = mpGeomXCoord[pix];
//        geo_y               = mpGeomYCoord[pix];
//        auxilaryDistance      = Math.sqrt( (geo_x-mCenterOfGravityX)*(geo_x-mCenterOfGravityX)
//                            + (geo_y-mCenterOfGravityY)*(geo_y-mCenterOfGravityY) );
//        auxiliary_angle = calculateAlpha(geo_x,geo_y);
////        auxiliary_angle     = atan( (geo_y-mCenterOfGravityY)/(geo_x-mCenterOfGravityX) );
////        auxiliary_angle     -= mDelta/180.0*M_PI;
//        ell_x               = auxilaryDistance * Math.cos(auxiliary_angle/180*Math.PI);
//        ell_y               = auxilaryDistance * Math.sin(auxiliary_angle/180*Math.PI);
//
//
////        mAsymmetryLong      += mpWeights[pix] * ell_x;
////        mAsymmetryTrans     += mpWeights[pix] * ell_y;
//    }
////    mAsymmetryLong          /= size;
////    mAsymmetryTrans         /= size;
//}
//
//public float calculateAlpha(double source_x, double source_y)
//{
//
//	float alpha = 0.0f;
//    double auxiliary_angle  = Math.atan( (source_y - mCenterOfGravityY)/(source_x - mCenterOfGravityX) );
//
//    auxiliary_angle         = auxiliary_angle / Math.PI * 180;
//
//    alpha                  = (float) (mDelta - auxiliary_angle);
//
//    if (alpha > 90)
//    {
//        alpha              = alpha - 180;
//    }
//    if (alpha < -90)
//    {
//        alpha              = 180 + alpha;
//    }
//    return alpha;
//}
	
//public void calculateSourceParameter()
//{
//	mDistance               = Math.sqrt( (mCenterOfGravityY - sourcePosY) * (mCenterOfGravityY - sourcePosY)
//        + (mCenterOfGravityX - sourcePosX) * (mCenterOfGravityX - sourcePosX) );
//
//    alpha = calculateAlpha(sourcePosX,sourcePosY);
//    alphaOff1 = calculateAlpha(sourcePosY,-sourcePosX);
//    alphaOff2 = calculateAlpha(-sourcePosX,-sourcePosY);
//    alphaOff3 = calculateAlpha(-sourcePosY,sourcePosX);
//}



}
