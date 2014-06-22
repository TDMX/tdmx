/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.lib.console.domain;

import java.io.Serializable;
import java.util.Date;

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

	public UserDetails(String loginName, boolean authorized, boolean temporarilyBlocked) {
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
