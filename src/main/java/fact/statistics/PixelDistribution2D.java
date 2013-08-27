package fact.statistics;

import java.io.Serializable;

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
	private double eigenValueX;
	private double eigenValueY;
	private double angle;
	private double size;
	
	public PixelDistribution2D(double varianceX, double varianceY,
			double covariance, double centerX, double centerY, double eigenValueX,
			double eigenValueY, double angle, double size) {
				
				this.setVarianceX(varianceX);
				this.setVarianceY(varianceY);
				this.setCovariance(covariance);
				this.setCenterX(centerX);
				this.setCenterY(centerY);
				this.setEigenValueX(eigenValueX);
				this.setEigenValueY(eigenValueY);
				this.setAngle(angle);
				this.setSize(size);
				
	}
	public PixelDistribution2D(double varianceX, double varianceY,
			double covariance, double centerX, double centerY, double eigenValueX,
			double eigenValueY, double angle) {
				
				this.setVarianceX(varianceX);
				this.setVarianceY(varianceY);
				this.setCovariance(covariance);
				this.setCenterX(centerX);
				this.setCenterY(centerY);
				this.setEigenValueX(eigenValueX);
				this.setEigenValueY(eigenValueY);
				this.setAngle(angle);
				//assume 1 
				this.setSize(1);
				
	}
	
	//special setters for with and length
	public double getLength(){
		return Math.sqrt(eigenValueX/size);
	}
	public double getWidth(){
		return Math.sqrt(eigenValueY/size);
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

	public double getEigenValueX() {
		return eigenValueX;
	}
	public void setEigenValueX(double eigenValueX) {
		this.eigenValueX = eigenValueX;
	}

	public double getEigenValueY() {
		return eigenValueY;
	}
	public void setEigenValueY(double eigenValueY) {
		this.eigenValueY = eigenValueY;
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
	


	
	

}
