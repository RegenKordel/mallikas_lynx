package eu.openreq.mallikas.models.json;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity
public class Person implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@SerializedName("username")
	@Expose
	private String username;
	
	@SerializedName("email")
	@Expose
	private String email;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
