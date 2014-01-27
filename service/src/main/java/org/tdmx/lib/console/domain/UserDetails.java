package org.tdmx.lib.console.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * UserDetails are detached information about the ConsoleUser.
 * 
 * @author Peter Klauser
 *
 */
public class UserDetails implements Serializable {

	private static final long serialVersionUID = -988419614813872556L;

	private final String loginName;

	private final boolean authorized;
	private final boolean temporarilyBlocked;
	
	private String firstName;
	private String lastName;
	private String email;
	private Date lastLogin;
	
	public UserDetails( String loginName, boolean authorized, boolean temporarilyBlocked ) {
		this.loginName = loginName;
		this.authorized = authorized; 
		this.temporarilyBlocked = temporarilyBlocked;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}
