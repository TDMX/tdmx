package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

	private String firstName;
	private String lastName;
	private String loginName;
	private String email;
	private Date lastLogin;
	
	public User( String loginName, String firstName, String lastName, String email, Date lastLogin ) {
		this.loginName = loginName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.lastLogin = lastLogin;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getLoginName() {
		return loginName;
	}

	public String getEmail() {
		return email;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

}
