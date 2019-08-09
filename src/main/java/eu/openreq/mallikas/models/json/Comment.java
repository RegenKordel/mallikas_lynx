package eu.openreq.mallikas.models.json;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
* Comment
* The comment that will be referenced from one entity
* 
*/
@Entity
public class Comment implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requirement_id")
	@SerializedName("requirement")
	private Requirement requirement;
	
//	private String requirementId;
//	
//	public String getRequirementId() {
//		return requirementId;
//	}
//
//	public void setRequirementId(String requirementId) {
//		this.requirementId = requirementId;
//	}
	
	/**
	* The unique identifier of a comment
	* (Required)
	* 
	*/
	@Id
	@SerializedName("id")
	@Expose
	private String id;
	/**
	* The textual description of the comment
	* (Required)
	* 
	*/
	@Type(type="text")
	@SerializedName("text")
	@Expose
	private String text;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="person_username")
	@SerializedName("commentDoneBy")
	@Expose
	private Person commentDoneBy;
	/**
	* Creation timestamp
	* (Required)
	* 
	*/
	@SerializedName("created_at")
	@Expose
	private long created_at;
	/**
	* Last modification time
	* 
	*/
	@SerializedName("modified_at")
	@Expose
	private long modified_at;
	

	public Requirement getRequirement() {
		return requirement;
	}

	public void setRequirement(Requirement requirement) {
		this.requirement = requirement;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public long getCreated_at() {
		return created_at;
	}
	
	public void setCreated_at(long created_at) {
		this.created_at = created_at;
	}
	
	public long getModified_at() {
		return modified_at;
	}
	
	public void setModified_at(long modified_at) {
		this.modified_at = modified_at;
	}

	public Person getCommentDoneBy() {
		return commentDoneBy;
	}

	public void setCommentDoneBy(Person commentDoneBy) {
		this.commentDoneBy = commentDoneBy;
	}
}