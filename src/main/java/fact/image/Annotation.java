/**
 * 
 */
package fact.image;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author chris
 *
 */
public class Annotation implements Serializable {

	/** The unique class ID  */
	private static final long serialVersionUID = 4514032777513090857L;

	String objectId;
	
	Date createdAt;
	
	String user;

	Set<String> tags = new HashSet<String>();
	
	Set<Integer> selectedPixels = new HashSet<Integer>();
	
	
	public Annotation( String objectId ){
		this.objectId = objectId;
		this.createdAt = new Date();
	}


	/**
	 * @return the objectId
	 */
	public String getObjectId() {
		return objectId;
	}


	/**
	 * @param objectId the objectId to set
	 */
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}


	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}


	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}



	/**
	 * @return the tags
	 */
	public Set<String> getTags() {
		return tags;
	}


	/**
	 * @param tags the tags to set
	 */
	public void setTags(Set<String> tags) {
		this.tags = tags;
	}


	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}


	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}


	/**
	 * @return the selectedPixels
	 */
	public Set<Integer> getSelectedPixels() {
		return selectedPixels;
	}


	/**
	 * @param selectedPixels the selectedPixels to set
	 */
	public void setSelectedPixels(Set<Integer> selectedPixels) {
		this.selectedPixels = selectedPixels;
	}
}