package eu.openreq.mallikas.models.json;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
* A Project to store requirements
* 
*/
@Entity
public class Project implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	* The unique identifier of a project
	* (Required)
	* 
	*/
	@Id
	@SerializedName("id")
	@Expose
	private String id;
	/**
	* The name of the project
	* (Required)
	* 
	*/
	@SerializedName("name")
	@Expose
	private String name;
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
	* The requirements specified in a project
	* 
	*/
	@ElementCollection
	@SerializedName("specifiedRequirements")
	@Expose
	private Set<String> specifiedRequirements = null;
	
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
	
	public Set<String> getSpecifiedRequirements() {
		return specifiedRequirements;
	}
	
	public void setSpecifiedRequirements(Set<String> specifiedRequirements) {
		this.specifiedRequirements = specifiedRequirements;
	}
}