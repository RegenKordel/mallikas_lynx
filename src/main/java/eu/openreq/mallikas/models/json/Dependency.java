package eu.openreq.mallikas.models.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
* Dependency
* The dependency between requirements
* 
*/
@Entity
//@Table(name = "Dependency")
public class Dependency implements Serializable {
	public final static long serialVersionUID = 1L;
	/**
	* The unique identifier of a dependency
	* (Required)
	* 
	*/
	@Id
	@SerializedName("id")
	@Expose
	private String id;
//	
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
	
	
	/**
	* The type of dependency between requirements
	* (Required)
	* 
	*/
	@SerializedName("dependency_type")
	@Expose
	private Dependency_type dependency_type;
	/**
	* NLP engine's estimation of the reliability of a proposed dependency
	* 
	*/
	@SerializedName("dependency_score")
	@Expose
	private double dependency_score;
	/**
	* Status of dependency that has been detected by NLP and whether or not the dependency has been approved
	* 
	*/
	@SerializedName("status")
	@Expose
	private Dependency_status status;
	
	/**
	* The requirement having a dependency
	* (Required)
	* 
	*/
	@SerializedName("fromid")
	@Expose
	private String fromid;
	
	/**
	* The requirement dependent on another
	* (Required)
	* 
	*/
	@SerializedName("toid")
	@Expose
	private String toid;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@SerializedName("description")
	@Expose
	private Set<String> description;
	
	/**
	* Creation timestamp
	* (Required)
	* 
	*/
	@SerializedName("created_at")
	@Expose
	private long created_at;

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id=id;
	}
	
	public Dependency_type getDependency_type() {
		return dependency_type;
	}
	
	public void setDependency_type(Dependency_type dependency_type) {
		this.dependency_type = dependency_type;
	}
	
	public double getDependency_score() {
		return dependency_score;
	}
	
	public void setDependency_score(double dependency_score) {
		this.dependency_score = dependency_score;
	}
	
	public Dependency_status getStatus() {
		return status;
	}
	
	public void setStatus(Dependency_status status) {
		this.status = status;
	}
	
	public String getFromid() {
		return fromid;
	}
	
	public void setFromid(String fromid) {
		this.fromid = fromid;
	}
	
	public String getToid() {
		return toid;
	}
	
	public void setToid(String toid) {
		this.toid = toid;
	}
	
	public Set<String> getDescription() {
		return description;
	}

	public void setDescription(Set<String> description) {
		this.description = description;
	}

	public long getCreated_at() {
		return created_at;
	}
	
	public void setCreated_at(long created_at) {
		this.created_at = created_at;
	}
}