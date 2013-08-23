package fact.features;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.Constants;
import fact.viewer.ui.DefaultPixelMapping;


public class EllipseFromShower implements Processor {
	
	public double centerX, centerY;
	int mNumerberOfShowerPixel;
	double auxilaryDistance;
	private float[] mpGeomXCoord;
	private float[] mpGeomYCoord;
//	private float[] mpWeights;

	private int mNumberOfShowerPixel;
	private double mLineGradientAngle;
	private double mLineSlope;
	private double mSemiMajorAxis;
	private double mSemiMinorAxis;
//	private float[] photonCharge;
	public float mCenterOfGravityX;
	public float mCenterOfGravityY;
	private int mGeomCenterX;
	private int mGeomCenterY;
	public double length;
	public double width;
	public double area;
	public double mDelta;
	public double mAsymmetryLong;
	public double mAsymmetryTrans;
	public double mDistance;
	public float alpha;
	public float alphaOff1;
	public float alphaOff2;
	public float alphaOff3;
	private float sourcePosX;
	private float sourcePosY;
	
// A logger
static Logger log = LoggerFactory.getLogger(EllipseFromShower.class);

//what do we need to calculate the ellipse?
private String wheights;
private String showerIds;
private float[] wheightsArray;
private int[] showerPixel;



@Override
public Data process(Data input) {
	// TODO Auto-generated method stub
	//get the required stuff from the map
	//in case the map doesn't contain a shower return the original input. 

	try{
		showerPixel= (int[]) input.get(showerIds);
		if(showerPixel ==  null){
			log.info("No showerpixel in this event. Not calculating Ellipse");
			return input;
		}
	} catch (ClassCastException e){
		log.error("showerIds is not of type int[]. Aborting");
		return null;
	}
	
	try{
		wheightsArray = (float[]) input.get(wheights);
		if(wheightsArray ==  null){
			log.info("Wheights were null. Not calculating Ellipse");
			return input;
		}
	} catch (ClassCastException e){
		log.error("Wheights is not of type float[]. Aborting");
		return null;
	}
	
	//calculate the "size" of the shower
    double size = 0.0;
    for(int pix: showerPixel){
    	size += wheightsArray[pix];
    }
	
	//find wheighted center of the shower. assuming we have no islands this works.
    for (int pix: showerPixel)
    {
        centerX            += wheightsArray[pix] * mpGeomXCoord[pix];
        centerY            += wheightsArray[pix] * mpGeomYCoord[pix];
    }
    //divide the center coordinates by size. I'm not sure if this is correct. I checked it. It is.
    centerX                /= size;
    centerY                /= size;
	
    // These names are somewhat misleading. I think whats happening here is a linear regression on both axis.
    // See also http://de.wikipedia.org/wiki/Lineare_Regression#Berechnung_der_Regressionsgeraden
    // alpha here is S in the wikipedia article. See the equation for calculating b. This is whats happening here. In both directions.
    // this is the correct way to do this. Since we can't assume any correlation(that is direction in ur case) beforehand.
    // See also http://de.wikipedia.org/wiki/Lineare_Regression#Bildliche_Darstellung_und_Interpretation
    // the real difference here is the wheighting.  
    double alpha_xx         = 0;
    double alpha_yy         = 0;
    double alpha_xy         = 0;
    
    for (int pix: showerPixel )
    {
        alpha_xx            += wheightsArray[pix] * (mpGeomXCoord[pix] - centerX) * (mpGeomXCoord[pix] - centerX);
        alpha_yy            += wheightsArray[pix] * (mpGeomYCoord[pix] - centerY) * (mpGeomYCoord[pix] - centerY);
        alpha_xy            += wheightsArray[pix] * (mpGeomXCoord[pix] - centerX) * (mpGeomYCoord[pix] - centerY);
    }
	
    //At this point you usually want to get the regression coefficents. (The b in the wikipedia article)
    //What happens however is quite unusual. Which means I dont understand it :P
    //A variable called A. What does it do?? It has a sqrt. So lets look for equations with a sqrt. None fit. Lets find some other wikipedia article with a sqrt.
    //
    double A                = 0;
    A       = ( Math.sqrt( (alpha_yy - alpha_xx)*(alpha_yy - alpha_xx) + (2 * alpha_xy)*(2 * alpha_xy) ) + (alpha_yy - alpha_xx) )
              / (2 * alpha_xy);
    
	
	return null;
}
//
//public EllipseFromShower(int[] showerPixel, float[] photonCharge, float source, float source2) 
//{
//		this.sourcePosX = source;
//		this.sourcePosY = source2;
//
//		this.showerPixel          = showerPixel;
//		this.photonCharge = photonCharge;
//		mNumberOfShowerPixel    = showerPixel.length;
//		
////	    size = 0.0;
////	    for(int pix: showerPixel)	size += photonCharge[pix];
//	
//    /// @Todo remove 1 by the real value
//	    
//	    mLengthOfPixel          = Constants.PIXEL_SIZE;
//	    mpGeomXCoord            = DefaultPixelMapping.getGeomXArray();
//	    mpGeomYCoord            = DefaultPixelMapping.getGeomYArray();
//	    mpWeights               = photonCharge;
//	    if(mNumberOfShowerPixel > 0){
//		    calculateEllipseMars();
//		    calculateCenterOfGravity(showerPixel);
//		    calculateAsymmetry();
//		    calculateSourceParameter();
//	    }
//		    /// Loop over the Shower Pixel and extract the correct geometric coordinates
//		    
//}
	
	/**
	 * original code copied from F. Temme's DoFact program.
	 * 
	 */
	
//public void calculateEllipseMars()
//	{
//	 	centerX = 0.0;
//	 	centerY = 0.0;
//	    double alpha_xx         = 0;
//	    double alpha_yy         = 0;
//	    double alpha_xy         = 0;
////	    double N = size;
//
////	    for (int pix: showerPixel)
////	    {
////	        centerX            += mpWeights[pix] * mpGeomXCoord[pix];
////	        centerY            += mpWeights[pix] * mpGeomYCoord[pix];
////	    }
////
////	    centerX                /= N;
////	    centerY                /= N;
//	
////	    for (int pix: showerPixel )
////	    {
////	        alpha_xx            += mpWeights[pix] * (mpGeomXCoord[pix] - centerX) * (mpGeomXCoord[pix] - centerX);
////	        alpha_yy            += mpWeights[pix] * (mpGeomYCoord[pix] - centerY) * (mpGeomYCoord[pix] - centerY);
////	        alpha_xy            += mpWeights[pix] * (mpGeomXCoord[pix] - centerX) * (mpGeomYCoord[pix] - centerY);
////	    }
//
//	    double A                = 0;
//	    A       = ( Math.sqrt( (alpha_yy - alpha_xx)*(alpha_yy - alpha_xx) + (2 * alpha_xy)*(2 * alpha_xy) ) + (alpha_yy - alpha_xx) )
//	              / (2 * alpha_xy);
//
//	    mLineGradientAngle      = Math.atan(A);
//	    mLineSlope              = Math.tan(mLineGradientAngle);
//	    mLineIntercept          = centerY - (mLineSlope * centerX);
//
//	    mCenterX                = centerX;
//	    mCenterY                = centerY;
//
//	    mSemiMajorAxis          = 1 / Math.sqrt(N * ( A*A + 1 ))
//	    * Math.sqrt( (A*A + 1)*alpha_yy
//	           + Math.sqrt( (alpha_yy - alpha_xx)*(alpha_yy - alpha_xx) + (2*alpha_xy)*(2*alpha_xy) )
//	       );
//	    
//	    mSemiMinorAxis          = 1 / Math.sqrt(N * ( A*A + 1 ))
//	    * Math.sqrt( (A*A + 1)*alpha_xx
//	           - Math.sqrt( (alpha_yy - alpha_xx)*(alpha_yy - alpha_xx) + (2*alpha_xy)*(2*alpha_xy) )
//	       );
//	    
//		length             = mSemiMajorAxis;
//	    width              = mSemiMinorAxis;
//	    area               = length * width * Math.PI;
//	    mDelta              = mLineGradientAngle / Math.PI * 180;
//	    
//	}
//
//
//
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

public void calculateAsymmetry()
{
 
    float geo_x             = 0;
    float geo_y             = 0;
    double ell_x             = 0;
    double ell_y             = 0;
    float auxiliary_angle   = 0;
    auxilaryDistance    = 0;

    for (int pix : showerPixel)
    {
        geo_x               = mpGeomXCoord[pix];
        geo_y               = mpGeomYCoord[pix];
        auxilaryDistance      = Math.sqrt( (geo_x-mCenterOfGravityX)*(geo_x-mCenterOfGravityX)
                            + (geo_y-mCenterOfGravityY)*(geo_y-mCenterOfGravityY) );
        auxiliary_angle = calculateAlpha(geo_x,geo_y);
//        auxiliary_angle     = atan( (geo_y-mCenterOfGravityY)/(geo_x-mCenterOfGravityX) );
//        auxiliary_angle     -= mDelta/180.0*M_PI;
        ell_x               = auxilaryDistance * Math.cos(auxiliary_angle/180*Math.PI);
        ell_y               = auxilaryDistance * Math.sin(auxiliary_angle/180*Math.PI);


//        mAsymmetryLong      += mpWeights[pix] * ell_x;
//        mAsymmetryTrans     += mpWeights[pix] * ell_y;
    }
//    mAsymmetryLong          /= size;
//    mAsymmetryTrans         /= size;
}

public float calculateAlpha(double source_x, double source_y)
{

	float alpha = 0.0f;
    double auxiliary_angle  = Math.atan( (source_y - mCenterOfGravityY)/(source_x - mCenterOfGravityX) );

    auxiliary_angle         = auxiliary_angle / Math.PI * 180;

    alpha                  = (float) (mDelta - auxiliary_angle);

    if (alpha > 90)
    {
        alpha              = alpha - 180;
    }
    if (alpha < -90)
    {
        alpha              = 180 + alpha;
    }
    return alpha;
}
	
public void calculateSourceParameter()
{
	mDistance               = Math.sqrt( (mCenterOfGravityY - sourcePosY) * (mCenterOfGravityY - sourcePosY)
        + (mCenterOfGravityX - sourcePosX) * (mCenterOfGravityX - sourcePosX) );

    alpha = calculateAlpha(sourcePosX,sourcePosY);
    alphaOff1 = calculateAlpha(sourcePosY,-sourcePosX);
    alphaOff2 = calculateAlpha(-sourcePosX,-sourcePosY);
    alphaOff3 = calculateAlpha(-sourcePosY,sourcePosX);
}



}
