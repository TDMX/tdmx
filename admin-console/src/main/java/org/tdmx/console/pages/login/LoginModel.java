package org.tdmx.console.pages.login;

import java.io.Serializable;

/**
 * Login page model
 * 
 */
public class LoginModel implements Serializable {

	private String userName;
	private String password;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String login) {
		this.userName = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
