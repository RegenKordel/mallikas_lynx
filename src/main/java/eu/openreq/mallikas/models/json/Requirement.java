package eu.openreq.mallikas.models.json;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
* Requirement
* A requirement within the OpenReq framework
* 
*/
@Entity
public class Requirement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	private String projectId;
	
	/**
	* The unique identifier for a requirement
	* (Required)
	* 
	*/
	@Id
	@SerializedName("id")
	@Expose
	private String id;
	
	/**
	* The name of the requirement
	* 
	*/
	@SerializedName("name")
	@Expose
	private String name;
	/**
	* The textual description of the requirement
	* 
	*/
	@Type(type="text")
	@SerializedName("text")
	@Expose
	private String text;
	
	/**
	* The comments to the requirement
	* 
	*/
	@OneToMany(mappedBy = "requirement", cascade = CascadeType.ALL)
	@SerializedName("comments")
	@Expose
	private Set<Comment> comments = null;
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
	/**
	* The calculated priority of a requirement
	* 
	*/
	@SerializedName("priority")
	@Expose
	private int priority;
	/**
	* The type of a requirement
	* 
	*/
	@SerializedName("requirement_type")
	@Expose
	@Enumerated(EnumType.STRING)
	private Requirement_type requirement_type;
	/**
	* The current status of a requirement
	* (Required)
	* 
	*/
	@SerializedName("status")
	@Expose
	@Enumerated(EnumType.STRING)
	private Requirement_status status;
	
	/**
	* The requirements belonging to this requirement
	* 
	*/
	@ElementCollection
	@SerializedName("children")
	private Set<Requirement> children = null;
	
	
	/**
	* RequirementParts of a requirement
	* 
	*/
	@OneToMany(mappedBy = "requirement", cascade = CascadeType.ALL)
	@SerializedName("requirementParts")
	@Expose
	private Set<RequirementPart> requirementParts = null;
	

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public Set<Comment> getComments() {
		return comments;
	}
	
	public void setComments(Set<Comment> comments) {
		this.comments = comments;
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
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public Requirement_type getRequirement_type() {
		return requirement_type;
	}
	
	public void setRequirement_type(Requirement_type requirement_type) {
		this.requirement_type = requirement_type;
	}
	
	public Requirement_status getStatus() {
		return status;
	}
	
	public void setStatus(Requirement_status status) {
		this.status = status;
	}
	
	public Set<Requirement> getChildren() {
		return children;
	}
	
	public void setChildren(Set<Requirement> children) {
		this.children = children;
	}
	
	public Set<RequirementPart> getRequirementParts() {
		return requirementParts;
	}

	public void setRequirementParts(Set<RequirementPart> requirementParts) {
		this.requirementParts = requirementParts;
	}
}