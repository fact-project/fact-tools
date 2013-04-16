/**
 * 
 */
package fact.image;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.viewer.ui.DefaultPixelMapping;

/**
 * @author chris
 * 
 */
public class Pixel implements Serializable, Comparable<Pixel> {

	/** The unique class ID */
	private static final long serialVersionUID = -2835016779288598332L;
	final static DefaultPixelMapping mapping = new DefaultPixelMapping();

	static Logger log = LoggerFactory.getLogger(Pixel.class);

	Integer softId;
	final Integer chid;
	public final int x;
	public final int y;
	Double weight = 1.0;

	/*
	 * public Pixel(int chid) { this.chid = chid; this.softId =
	 * mapping.getHardwareID(chid); x = mapping.getGeomX(softId) + 22; y =
	 * mapping.getGeomY(softId) + 19; log.info("Pixel " + chid +
	 * " is at ( {}, {} )", x, y); }
	 */

	public Pixel(Integer softId) {
		chid = 0;
		this.softId = softId;
		x = mapping.getGeomX(softId) + 22;
		y = mapping.getGeomY(softId) + 19;
	}

	public Pixel(int x, int y) {
		softId = 0;
		chid = 0;
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the softId
	 */
	public Integer getSoftId() {
		return softId;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the weight
	 */
	public Double getWeight() {
		return weight;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(Double weight) {
		this.weight = weight;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Pixel arg0) {
		return softId.compareTo(arg0.softId);
	}
}
