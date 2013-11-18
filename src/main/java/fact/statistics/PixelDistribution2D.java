package fact.statistics;

import java.io.Serializable;

/**
 * <PRE format="md">
 * At the moment this is simply a container class holding some useful information about a shower. 
 * A shower can be described as a 2 dimensional distribution with weights. By transforming the camera coordinates 
 * into the eigenspace of the distribution (ie. you basicly rotate the shower) you can easily calculate the parameters
 * also known as **Hillas Parameter** 
 * 
 * @author kaibrugge
 *
 */
public class PixelDistribution2D implements Serializable {
	private static final long serialVersionUID = 3815070753769798637L;
	//	Double variance_x = null;
//	Double variance_y = null;
//	Double covariance = null;
//	Double angle =  null;
//	int[] pixel =  null;
	private double varianceX;
	private double varianceY;
	private double covariance;
	private double centerX; 
	private double centerY;
	private double angle;
	private double size;
	private double eigenVarianceX, eigenVarianceY;
	private double eigenSkewnessX, eigenSkewnessY;
	private double eigenKurtosisX, eigenKurtosisY;
	
	public PixelDistribution2D(double varianceX, double varianceY,
			double covariance, double centerX, double centerY, double eigenVarianceX, double eigenVarianceY, double angle, double sumOfWeights) {
				
				this.setVarianceX(varianceX);
				this.setVarianceY(varianceY);
				this.setCovariance(covariance);
				this.setCenterX(centerX);
				this.setCenterY(centerY);
				this.setAngle(angle);
				this.setSize(sumOfWeights);
				this.setEigenVarianceX(eigenVarianceX);
				this.setEigenVarianceY(eigenVarianceY);
				
	}
	
	public PixelDistribution2D(double varianceX, double varianceY,
			double covariance, double centerX, double centerY, double eigenVarianceX, double eigenVarianceY, 
			double eigenSkewnessX, double eigenSkewnessY, double eigenKurtosisX, double eigenKurtosisY, double angle, double sumOfWeights) {
				
				this.setVarianceX(varianceX);
				this.setVarianceY(varianceY);
				this.setCovariance(covariance);
				this.setCenterX(centerX);
				this.setCenterY(centerY);
				this.setAngle(angle);
				this.setSize(sumOfWeights);
				this.setEigenVarianceX(eigenVarianceX);
				this.setEigenVarianceY(eigenVarianceY);
				
	}
	

	//special setters for with and length which are just other names for the standardeviation of the distribution in its eigenspace
	public double getLength(){
		return getEigenDeviationX();
	}
	public double getWidth(){
		return getEigenDeviationY();
	}
	
	public double getEigenDeviationX(){
		return Math.sqrt(eigenVarianceX);
	}
	public double getEigenDeviationY(){
		return Math.sqrt(eigenVarianceY);
	}

	public double getVarianceX() {
		return varianceX;
	}
	public void setVarianceX(double varianceX) {
		this.varianceX = varianceX;
	}

	public double getVarianceY() {
		return varianceY;
	}
	public void setVarianceY(double varianceY) {
		this.varianceY = varianceY;
	}

	public double getCovariance() {
		return covariance;
	}
	public void setCovariance(double covariance) {
		this.covariance = covariance;
	}

	public double getCenterX() {
		return centerX;
	}
	public void setCenterX(double centerX) {
		this.centerX = centerX;
	}

	public double getCenterY() {
		return centerY;
	}
	public void setCenterY(double centerY) {
		this.centerY = centerY;
	}

	public double getAngle() {
		return angle;
	}
	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getSize() {
		return size;
	}
	public void setSize(double size) {
		this.size = size;
	}


	public double getEigenVarianceX() {
		return eigenVarianceX;
	}
	public void setEigenVarianceX(double eigenVarianceX) {
		this.eigenVarianceX = eigenVarianceX;
	}


	public double getEigenVarianceY() {
		return eigenVarianceY;
	}
	public void setEigenVarianceY(double eigenVarianceY) {
		this.eigenVarianceY = eigenVarianceY;
	}


	public double getEigenSkewnessX() {
		return eigenSkewnessX;
	}


	public void setEigenSkewnessX(double eigenSkewnessX) {
		this.eigenSkewnessX = eigenSkewnessX;
	}


	public double getEigenSkewnessY() {
		return eigenSkewnessY;
	}


	public void setEigenSkewnessY(double eigenSkewnessY) {
		this.eigenSkewnessY = eigenSkewnessY;
	}


	public double getEigenKurtosisX() {
		return eigenKurtosisX;
	}


	public void setEigenKurtosisX(double eigenKurtosisX) {
		this.eigenKurtosisX = eigenKurtosisX;
	}


	public double getEigenKurtosisY() {
		return eigenKurtosisY;
	}


	public void setEigenKurtosisY(double eigenKurtosisY) {
		this.eigenKurtosisY = eigenKurtosisY;
	}
	


	
	

}
